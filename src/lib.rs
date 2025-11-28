use anyhow::Result;
use futuresdr::blocks::seify::Builder;
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

    connect!(fg,
        source.outputs[0] > frame_sync > fft_demod > gray_mapping > deinterleaver > hamming_dec > header_decoder;
        header_decoder.frame_info | frame_info.frame_sync;
        header_decoder | decoder;
    );

    let _ = rt.run(fg)?;

    // debug!("[FG] Starting flowgraph termination...");
    //
    // rt.block_on(async move {
    //     if let Err(e) = fg_task.await {
    //         error!("[FG] Error during flowgraph termination: {}", e);
    //     }
    // });

    debug!("[FG] Flowgraph cleanup completed");
    Ok(())
}

#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::JNIEnv;
    use jni::objects::JClass;
    use jni::objects::JString;

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
