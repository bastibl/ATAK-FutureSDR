use anyhow::Result;
use futuresdr::blocks::MessagePipe;
use futuresdr::blocks::seify::Builder;
use futuresdr::crossfire::mpsc;
use futuresdr::prelude::*;
use lora::Decoder;
use lora::Deinterleaver;
use lora::FftDemod;
use lora::FrameSync;
use lora::GrayMapping;
use lora::HammingDec;
use lora::HeaderDecoder;
use lora::HeaderMode;

const IMPLICIT_HEADER: bool = false;

pub fn run_fg(device_args: String) -> Result<()> {
    let rt = Runtime::new();
    let mut fg = Flowgraph::new();

    let oversampling = 20;
    let bandwidth = 125_000;
    let soft_decoding = false;
    let spreading_factor = 7;

    let source = Builder::new(device_args)?
        .sample_rate((bandwidth * oversampling) as f64)
        .frequency(868_100_000.0)
        .gain(10.0)
        .build_source()?;

    let frame_sync: FrameSync = FrameSync::new(
        868_100_000,
        bandwidth,
        spreading_factor,
        IMPLICIT_HEADER,
        vec![],
        oversampling,
        None,
        Some("header_crc_ok"),
        false,
        None,
    );
    let fft_demod: FftDemod = FftDemod::new(soft_decoding, spreading_factor);
    let gray_mapping: GrayMapping = GrayMapping::new(soft_decoding);
    let deinterleaver: Deinterleaver = Deinterleaver::new(soft_decoding);
    let hamming_dec: HammingDec = HammingDec::new(soft_decoding);
    let header_decoder = HeaderDecoder::new(HeaderMode::Explicit, false);
    let decoder = Decoder::new();
    let (tx_frame, rx_frame) = mpsc::bounded_async::<Pmt>(100);
    let message_pipe = MessagePipe::new(tx_frame);

    connect!(fg,
        source.outputs[0] > frame_sync > fft_demod > gray_mapping > deinterleaver > hamming_dec > header_decoder;
        header_decoder.frame_info | frame_info.frame_sync;
        header_decoder | decoder;
        decoder | message_pipe;
    );

    let (_fg, _handle) = rt.start_sync(fg)?;
    rt.block_on(async move {
        while let Ok(x) = rx_frame.recv().await {
            match x {
                Pmt::Blob(data) => {
                    println!("received frame ({:?} bytes)", data.len());
                    #[cfg(target_os = "android")]
                    android::send_to_bridge(&data);
                }
                _ => break,
            }
        }
    });

    debug!("[FG] Flowgraph cleanup completed");
    Ok(())
}

#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::JNIEnv;
    use jni::JavaVM;
    use jni::objects::GlobalRef;
    use jni::objects::JByteArray;
    use jni::objects::JClass;
    use jni::objects::JObject;
    use jni::objects::JString;
    use jni::objects::JValue;
    use jni::sys::jbyte;
    use jni::sys::jint;
    use std::sync::OnceLock;

    static JVM: OnceLock<JavaVM> = OnceLock::new();
    static BRIDGE: OnceLock<GlobalRef> = OnceLock::new();

    #[allow(non_snake_case)]
    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_atakmap_android_futuresdr_FutureSDRMapComponent_setBridge(
        env: JNIEnv,
        _class: JClass,
        bridge: JObject,
    ) {
        let jvm = env.get_java_vm().expect("get_java_vm failed");
        let _ = JVM.set(jvm);

        let global = env.new_global_ref(bridge).expect("new_global_ref failed");
        let _ = BRIDGE.set(global);
    }

    pub fn send_to_bridge(data: &[u8]) {
        let jvm = JVM.get().expect("JVM not set");
        let bridge = BRIDGE.get().expect("bridge not registered");

        let mut env = jvm.attach_current_thread().expect("attach_current_thread");

        let arr: JByteArray = env
            .new_byte_array(data.len() as jint)
            .expect("new_byte_array failed");

        let jbytes: Vec<jbyte> = data.iter().map(|&b| b as jbyte).collect();
        env.set_byte_array_region(&arr, 0, &jbytes)
            .expect("set_byte_array_region failed");

        env.call_method(
            bridge.as_obj(),
            "onReceive",
            "([B)V", // (byte[]) -> void
            &[JValue::Object(&JObject::from(arr))],
        )
        .expect("call_method failed");
    }

    #[allow(non_snake_case)]
    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_atakmap_android_futuresdr_FlowgraphManager_runFg(
        mut env: JNIEnv,
        _class: JClass,
        mut device_args: JString,
    ) {
        futuresdr::runtime::init();
        unsafe {
            std::env::set_var("FUTURESDR_ctrlport_enable", "true");
            std::env::set_var("FUTURESDR_ctrlport_bind", "0.0.0.0:1337");
        }

        let device_args: String = if let Ok(s) = env.get_string(&mut device_args) {
            s.into()
        } else {
            error!("failed to get Java string for device args");
            panic!("failed to get Java string for device args");
        };

        info!("calling run_fg with args {device_args}");
        std::thread::spawn(move || {
            let ret = run_fg(device_args);
            info!("run_fg returned {:?}", ret);
        });
    }
}
