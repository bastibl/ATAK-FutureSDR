use anyhow::Result;
use futuresdr::blocks::NullSink;
use futuresdr::blocks::seify::Builder;
use futuresdr::prelude::*;

pub fn run_fg(fd: u32) -> Result<()> {
    let mut fg = Flowgraph::new();

    let args = format!("fd={fd}");
    info!("device args {}", &args);

    let src = Builder::new(args)?
        .frequency(105.3e6 - 0.3e6)
        .sample_rate(3.2e6)
        .gain(40.0)
        .build_source()?;

    let snk = NullSink::<Complex32>::new();

    connect!(fg, src.outputs[0] > snk);

    Runtime::new().run(fg)?;
    Ok(())
}

#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::JNIEnv;
    use jni::objects::JClass;
    use jni::sys::jint;

    #[allow(non_snake_case)]
    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_atakmap_android_futuresdr_plugin_FutureSDRTool_runFg(
        _env: JNIEnv,
        _class: JClass,
        fd: jint,
    ) {
        futuresdr::runtime::init();
        unsafe {
            std::env::set_var("FUTURESDR_ctrlport_enable", "true");
            std::env::set_var("FUTURESDR_ctrlport_bind", "0.0.0.0:1337");
        }

        info!("calling run_fg");
        // let ret = run_fg(fd as u32);
        // info!("run_fg returned {:?}", ret);
    }
}
