package com.konka.cateye.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.konka.cateye.activity.MainActivity;
import com.konka.cateye.service.AutoMonitorService;


/**
 * Created by Tokgo on 2015-11-18.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if((intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))) {
            Intent i = new Intent(context, AutoMonitorService.class);
            context.startService(i);
        }
    }
}
