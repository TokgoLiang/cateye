package com.konka.cateye.util;

/**
 * Created by Tokgo on 2015-11-18.
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konka.cateye.bean.OnOrOff;
import com.konka.cateye.bean.Television;
import com.konka.cateye.service.AutoMonitorService;
import com.konka.cateye.service.AutoRunService;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class ExitApplication extends Application {

    private List<Activity> list = new ArrayList<>();
    private static ExitApplication instance;
    private ExitApplication() {

    }
    /**
     * 单例模式中获取唯一的ExitApplication实例
     * */
    public static ExitApplication getInstance() {
        if(null == instance) {
            instance = new ExitApplication();
        }
        return instance;
    }

    /**
     * 添加Activity到容器中
     * */
    public void addActivity(Activity activity) {
        list.add(activity);
    }

    /**
     * 遍历所有Activity并finish
     * */
    public void exit(Context context,Television television) {
        updateOnOrOff(context,television);
        for(Activity activity:list) {
            activity.finish();
        }
    }


    private static void updateOnOrOff(final Context context,Television television){
        BmobQuery<OnOrOff> query = new BmobQuery<>();
        query.addWhereEqualTo("televisionId",television);
        query.findObjects(context, new FindListener<OnOrOff>() {
            @Override
            public void onSuccess(List<OnOrOff> list) {
                OnOrOff onOrOff = list.get(0);
                onOrOff.setState(false);
                //onOrOff.setIsRequest(!onOrOff.getIsRequest());
                AutoRunService.stopListen();
                onOrOff.update(context, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("TAG", "on or off update false success");
                        AutoRunService.startListen();
                    }

                    @Override
                    public void onFailure(int i, String s) {

                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }
}