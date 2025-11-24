
package org.futuresdr.atak.plugin;

import com.atak.plugins.impl.AbstractPlugin;
import gov.tak.api.plugin.IServiceController;
import com.atak.plugins.impl.PluginContextProvider;
import org.futuresdr.atak.PluginTemplateMapComponent;

/**
 *
 * AbstractPluginLifeCycle shipped with
 *     the plugin.
 */
public class PluginTemplateLifecycle extends AbstractPlugin {
    public PluginTemplateLifecycle(IServiceController serviceController) {
        super(serviceController, new PluginTemplateTool(serviceController.getService(PluginContextProvider.class).getPluginContext()), new PluginTemplateMapComponent());
        PluginNativeLoader.init(serviceController.getService(PluginContextProvider.class).getPluginContext());
    }
}
