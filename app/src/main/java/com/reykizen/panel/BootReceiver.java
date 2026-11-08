package com.reykizen.panel;
import android.content.*;
import android.os.Build;
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent i) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
            Intent s = new Intent(ctx, FloatingPanelService.class);
            if (Build.VERSION.SDK_INT >= 26)
                ctx.startForegroundService(s);
            else ctx.startService(s);
        }
    }
}
