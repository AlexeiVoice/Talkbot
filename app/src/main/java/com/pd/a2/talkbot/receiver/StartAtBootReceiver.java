package com.pd.a2.talkbot.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pd.a2.talkbot.activity.MainActivity;

public class StartAtBootReceiver extends  BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("onReceive", "Received" + intent.getAction());
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}