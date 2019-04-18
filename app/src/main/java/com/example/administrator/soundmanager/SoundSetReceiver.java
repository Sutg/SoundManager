package com.example.administrator.soundmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SoundSetReceiver extends BroadcastReceiver {
    public SoundSetReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent i=new Intent(context,SoundSetService.class);
        i.putExtra("fromMySelf",true);
        context.startService(i);
    }
}
