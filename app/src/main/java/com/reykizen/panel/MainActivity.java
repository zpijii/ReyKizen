package com.reykizen.panel;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this,
                    "Allow overlay permission!",
                    Toast.LENGTH_LONG).show();
                startActivityForResult(new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())), 1001);
                return;
            }
        }
        launchPanel();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == 1001) {
            if (Build.VERSION.SDK_INT >= 23 &&
                Settings.canDrawOverlays(this)) {
                launchPanel();
            } else {
                Toast.makeText(this,
                    "Permission denied!",
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void launchPanel() {
        Intent s = new Intent(this, FloatingPanelService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(s);
        } else {
            startService(s);
        }
        finish();
    }
}
