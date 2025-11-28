package com.atakmap.android.futuresdr.plugin

import com.atak.plugins.impl.AbstractPlugin
import com.atak.plugins.impl.PluginContextProvider
import com.atakmap.android.futuresdr.FutureSDRMapComponent
import gov.tak.api.plugin.IServiceController

class FutureSDRLifecycle(serviceController: IServiceController) : AbstractPlugin(
    serviceController,
    FutureSDRTool(
        serviceController.getService(PluginContextProvider::class.java)
            .pluginContext
    ),
    FutureSDRMapComponent()
)
