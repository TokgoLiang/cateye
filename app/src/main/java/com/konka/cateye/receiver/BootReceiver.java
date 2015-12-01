package com.konka.cateye.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konka.cateye.activity.MainActivity;
import com.konka.cateye.service.AutoMonitorService;
import com.konka.cateye.service.AutoRunService;


/**
 * Created by Tokgo on 2015-11-18.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if((intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))) {
            Log.d("cateye","on!!!");
            Intent start = new Intent(context, AutoRunService.class);
            context.startService(start);
        }
    }
}
