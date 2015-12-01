package com.konka.cateye.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.konka.cateye.StaticFinal;
import com.konka.cateye.activity.MainActivity;
import com.konka.cateye.bean.MonitorTime;
import com.konka.cateye.bean.Television;
import com.konka.cateye.bean.User;

import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.ValueEventListener;

/**
 * Created by Tokgo on 2015-11-18.
 */
public class TVListenerUtils {

    static BmobRealTimeData rtdUser;
    public static BmobRealTimeData rtdTV;
    static String objectId;


    /**
     * 监听是否连接成功
     */
    public static boolean isConnect() {
        if (rtdTV != null) {
            return rtdTV.isConnected();
        }
        return false;
    }

    /**
     * 关闭监听
     */
    public static void stopListen() {
        if (isConnect()) {
            rtdTV.unsubRowUpdate("Television", objectId);
        }
    }

    /**
     * 打开监听
     */
    public static void startListen() {
        if (isConnect()) {
            Log.d("TAG", "isConnect");
            rtdTV.subRowUpdate("Television", objectId);
        }
    }

    /**
     * 通过物理地址查询到TVID，并开启监听TV表更新
     */
    public static void listenTV(final Context context, final Handler handler) {
        String mac = SystemDataUtils.getLocalMacAddressFromIp();
        BmobQuery<Television> query = new BmobQuery<>();
        query.addWhereEqualTo("mac", mac);
        query.findObjects(context, new FindListener<Television>() {
            @Override
            public void onSuccess(List<Television> list) {
                objectId = list.get(0).getObjectId();
                listen(context, handler);
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    /**
     * 开启对Television表的监听
     */
    private static void listen(final Context context, final Handler handler) {
        //监听TV表绑定事件
        Log.d("TAG", "监听tv is:" + objectId);
        rtdTV = new BmobRealTimeData();
        rtdTV.start(context, new ValueEventListener() {
            @Override
            public void onDataChange(JSONObject data) {
                // TODO Auto-generated method stub
                Log.d("TAG", "data:" + data + "");
                Message message = new Message();
                message.what = StaticFinal.BOUNDED;
                handler.sendMessage(message);
            }

            @Override
            public void onConnectCompleted() {
                // TODO Auto-generated method stub
                if (rtdTV.isConnected()) {
                    rtdTV.subRowUpdate("Television", objectId);
                    Log.i("TAG", "监听TV表成功" + objectId);
                } else {
                    Message message = new Message();
                    message.what = StaticFinal.INTERNET_ERROR;
                    handler.sendMessage(message);
                }
            }
        });
    }

}
