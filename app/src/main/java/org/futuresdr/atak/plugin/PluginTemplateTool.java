
package org.futuresdr.atak.plugin;

import org.futuresdr.atak.R;
import com.atak.plugins.impl.AbstractPluginTool;

import org.futuresdr.atak.PluginTemplateDropDownReceiver;
import gov.tak.api.util.Disposable;

import android.content.Context;

/**
 * Please note:
 *     Support for versions prior to 4.5.1 can make use of a copy of AbstractPluginTool shipped with
 *     the plugin.
 */
public class PluginTemplateTool extends AbstractPluginTool implements Disposable {

    public PluginTemplateTool(Context context) {
        super(context,
                context.getString(R.string.app_name),
                context.getString(R.string.app_name),
                context.getResources().getDrawable(R.drawable.ic_launcher),
                PluginTemplateDropDownReceiver.SHOW_PLUGIN);
    }

    @Override
    public void dispose() {
    }
}
