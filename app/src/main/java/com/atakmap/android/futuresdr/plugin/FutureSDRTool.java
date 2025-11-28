package com.atakmap.android.futuresdr.plugin;

import com.atakmap.android.futuresdr.R;
import com.atak.plugins.impl.AbstractPluginTool;
import com.atakmap.android.futuresdr.FutureSDRDropDownReceiver;
import gov.tak.api.util.Disposable;
import android.content.Context;

public class FutureSDRTool extends AbstractPluginTool implements Disposable {

    public FutureSDRTool(Context context) {
        super(context,
                context.getString(R.string.app_name),
                context.getString(R.string.app_name),
                context.getResources().getDrawable(R.drawable.ic_launcher),
                FutureSDRDropDownReceiver.SHOW_PLUGIN);
    }

    @Override
    public void dispose() {
    }
}
