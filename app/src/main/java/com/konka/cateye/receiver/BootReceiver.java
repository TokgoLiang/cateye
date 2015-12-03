package com.konka.cateye.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konka.cateye.activity.MainActivity;
import com.konka.cateye.bean.OnOrOff;
import com.konka.cateye.bean.Television;
import com.konka.cateye.service.AutoMonitorService;
import com.konka.cateye.service.AutoRunService;
import com.konka.cateye.util.SystemDataUtils;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;


/**
 * Created by Tokgo on 2015-11-18.
 */
public class BootReceiver extends BroadcastReceiver {

    private Television mTelevision;
    private OnOrOff mOnOrOff;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        if((intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))) {
            Log.d("cateye", "on!!!");
            mContext = context;
            findTelevision();
        }
    }

    private void findTelevision() {
        String mac = SystemDataUtils.getLocalMacAddressFromIp();
        BmobQuery<Television> query = new BmobQuery<Television>();
        query.addWhereEqualTo("mac", mac);
        query.findObjects(mContext, new FindListener<Television>() {
            @Override
            public void onSuccess(List<Television> list) {
                if (list.size() > 0) {
                    mTelevision = list.get(0);
                    findOnOrOff();
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private void findOnOrOff() {
        BmobQuery<OnOrOff> query = new BmobQuery<OnOrOff>();
        query.addWhereEqualTo("televisionId", mTelevision);
        query.findObjects(mContext, new FindListener<OnOrOff>() {
            @Override
            public void onSuccess(List<OnOrOff> list) {
                mOnOrOff = list.get(0);
                if (mOnOrOff.getState()) {
                    mOnOrOff.setState(false);
                    mOnOrOff.update(mContext, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("cateye", "恢复ooo");
                            Intent start = new Intent(mContext, AutoRunService.class);
                            mContext.startService(start);
                        }

                        @Override
                        public void onFailure(int i, String s) {

                        }
                    });
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }
}
