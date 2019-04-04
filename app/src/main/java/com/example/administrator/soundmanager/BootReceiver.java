package com.example.administrator.soundmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.administrator.soundmanager.util.LOG;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED"))
        {
            LOG.d("BootReceiver","..............startService");
            Intent i = new Intent(context, SoundSetService.class);
            context.startService(i);
        }
    }
}
