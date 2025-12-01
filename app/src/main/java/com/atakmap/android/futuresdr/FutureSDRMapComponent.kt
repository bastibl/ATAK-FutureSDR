package com.atakmap.android.futuresdr

import android.content.Context
import android.content.Intent
import com.atakmap.android.dropdown.DropDownMapComponent
import com.atakmap.android.futuresdr.plugin.PluginNativeLoader
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter
import com.atakmap.android.maps.MapView
import com.atakmap.coremap.log.Log

class FutureSDRMapComponent : DropDownMapComponent() {
    private lateinit var pluginContext: Context
    private lateinit var ddr: FutureSDRDropDownReceiver
    private lateinit var hw: HardwareManager
    private lateinit var fg: FlowgraphManager
    private lateinit var bridge: AtakBridge

    override fun onCreate(
        context: Context, intent: Intent?,
        view: MapView
    ) {
        context.setTheme(R.style.ATAKPluginTheme)
        super.onCreate(context, intent, view)
        pluginContext = context

        ddr = FutureSDRDropDownReceiver(
            view, context
        )

        Log.d(TAG, "registering the plugin filter")
        val ddFilter = DocumentedIntentFilter()
        ddFilter.addAction(FutureSDRDropDownReceiver.SHOW_PLUGIN)
        registerDropDownReceiver(ddr, ddFilter)

        PluginNativeLoader.init(pluginContext)
        PluginNativeLoader.loadLibrary("atak_futuresdr")
        PluginNativeLoader.loadLibrary("SoapySDR")
        PluginNativeLoader.loadLibrary("SoapyHydraSDR")

        bridge = AtakBridge()
        setBridge(bridge)
        fg = FlowgraphManager()
        hw = HardwareManager(view.context)
        hw.setListener(fg)
        hw.start()
    }

    override fun onStart(context: Context?, view: MapView?) {
        Log.d(TAG, "onStart")
    }

    override fun onPause(context: Context?, view: MapView?) {
        Log.d(TAG, "onPause")
    }

    override fun onResume(
        context: Context?,
        view: MapView?
    ) {
        Log.d(TAG, "onResume")
    }

    override fun onStop(
        context: Context?,
        view: MapView?
    ) {
        Log.d(TAG, "onStop")
    }

    override fun onDestroyImpl(context: Context?, view: MapView?) {
        super.onDestroyImpl(context, view)
    }

    companion object {
        private const val TAG = "FutureSDRMapComponent"

        @JvmStatic
        external fun setBridge(bridge: AtakBridge)
    }
}
