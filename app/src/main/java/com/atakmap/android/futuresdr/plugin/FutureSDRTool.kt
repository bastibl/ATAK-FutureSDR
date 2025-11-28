package com.atakmap.android.futuresdr.plugin

import android.content.Context
import com.atak.plugins.impl.AbstractPluginTool
import com.atakmap.android.futuresdr.FutureSDRDropDownReceiver
import com.atakmap.android.futuresdr.R
import com.atakmap.android.futuresdr.plugin.PluginNativeLoader.init
import gov.tak.api.util.Disposable

class FutureSDRTool(context: Context) : AbstractPluginTool(
    context,
    context.getString(R.string.app_name),
    context.getString(R.string.app_name),
    context.resources.getDrawable(R.drawable.ic_launcher, context.theme),
    FutureSDRDropDownReceiver.SHOW_PLUGIN
), Disposable {
    init {
        init(context)
    }

    override fun dispose() {
    }
}
