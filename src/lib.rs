use anyhow::Result;
use futuresdr::blocks::NullSink;
use futuresdr::blocks::seify::Builder;
use futuresdr::prelude::*;

pub fn run_fg(device_args: String) -> Result<()> {
    let mut fg = Flowgraph::new();

    info!("device args {}", &device_args);

    let src = Builder::new(device_args)?
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
    use jni::objects::JString;

    #[allow(non_snake_case)]
    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_atakmap_android_futuresdr_plugin_FutureSDRTool_runFg(
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
        // let ret = run_fg(device_args);
        // info!("run_fg returned {:?}", ret);
    }
}
