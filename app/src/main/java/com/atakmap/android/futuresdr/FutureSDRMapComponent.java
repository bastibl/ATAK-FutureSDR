package com.atakmap.android.futuresdr;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.futuresdr.plugin.FutureSDRTool;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.futuresdr.plugin.PluginNativeLoader;

public class FutureSDRMapComponent extends DropDownMapComponent {

    private static final String TAG = "FutureSDRMapComponent";

    private Context pluginContext;
    private FutureSDRDropDownReceiver ddr;
    private HardwareManager hw;

    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        context.setTheme(R.style.ATAKPluginTheme);
        super.onCreate(context, intent, view);
        pluginContext = context;

        ddr = new FutureSDRDropDownReceiver(
                view, context);

        Log.d(TAG, "registering the plugin filter");
        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(FutureSDRDropDownReceiver.SHOW_PLUGIN);
        registerDropDownReceiver(ddr, ddFilter);

        PluginNativeLoader.init(pluginContext);
        PluginNativeLoader.loadLibrary("atak_futuresdr");
        PluginNativeLoader.loadLibrary("SoapySDR");
        PluginNativeLoader.loadLibrary("SoapyHydraSDR");

        hw = new HardwareManager(view.getContext());
        hw.start();
    }

    @Override
    public void onStart(final Context context, final MapView view) {
        Log.d(TAG, "onStart");
        FutureSDRTool.runFg("device args string");
    }

    @Override
    public void onPause(final Context context, final MapView view) {
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume(final Context context,
                         final MapView view) {
        Log.d(TAG, "onResume");
    }

    @Override
    public void onStop(final Context context,
                       final MapView view) {
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
    }
}
