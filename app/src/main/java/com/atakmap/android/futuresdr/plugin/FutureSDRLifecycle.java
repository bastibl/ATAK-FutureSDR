
package com.atakmap.android.futuresdr.plugin;

import com.atak.plugins.impl.AbstractPlugin;
import gov.tak.api.plugin.IServiceController;
import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.futuresdr.FutureSDRMapComponent;

/**
 *
 * AbstractPluginLifeCycle shipped with
 *     the plugin.
 */
public class FutureSDRLifecycle extends AbstractPlugin {
    private final static String TAG = "FutureSDRLifecycle";

    public FutureSDRLifecycle(IServiceController serviceController) {
        super(serviceController, new FutureSDRTool(serviceController.getService(PluginContextProvider.class).getPluginContext()), new FutureSDRMapComponent());
        PluginNativeLoader.init(serviceController.getService(PluginContextProvider.class).getPluginContext());
    }
}
