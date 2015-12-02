package com.konka.cateye.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.konka.cateye.R;
import com.konka.cateye.StaticFinal;
import com.konka.cateye.activity.MainActivity;
import com.konka.cateye.activity.SettingActivity;
import com.konka.cateye.bean.MonitorTime;
import com.konka.cateye.bean.Television;

import java.util.ArrayList;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by Tokgo on 2015-11-12.
 */
public class MonitorTimeUtils {

    /**
     * 检测接收到的Monitor类型对象是否合法
     *
     * @param monitorTime 监控时间段对象
     * */
    public static boolean legal(MonitorTime monitorTime) {
        boolean result = true;
        if (monitorTime.getBeginHour() > 23 || monitorTime.getBeginMinute() > 59 ||
                monitorTime.getEndHour() > 23 || monitorTime.getEndMinute() > 59) {
            result = false;
        } else if (monitorTime.getBeginTime() >= monitorTime.getEndTime()) {
            result = false;
        }
        return result;
    }

    /**
     * 从view获取监控时间段，并封装成MonitorTime类型对象
     * */
    public static  MonitorTime viewToMonitorTime (final View monitortimeLayout) {
        Log.d("TAG","viewToMonitorTime");

        EditText[] ets = new EditText[8];
        int[] numbers = new int[8];
        ets[0] = (EditText) monitortimeLayout.findViewById(R.id.et_beginTime_hour0);
        ets[1] = (EditText) monitortimeLayout.findViewById(R.id.et_beginTime_hour1);
        ets[2] = (EditText) monitortimeLayout.findViewById(R.id.et_beginTime_minute0);
        ets[3] = (EditText) monitortimeLayout.findViewById(R.id.et_beginTime_minute1);
        ets[4] = (EditText) monitortimeLayout.findViewById(R.id.et_endTime_hour0);
        ets[5] = (EditText) monitortimeLayout.findViewById(R.id.et_endTime_hour1);
        ets[6] = (EditText) monitortimeLayout.findViewById(R.id.et_endTime_minute0);
        ets[7] = (EditText) monitortimeLayout.findViewById(R.id.et_endTime_minute1);

        //判断输入是否为空
        for(int i=0; i<ets.length; i++) {
            if(TextUtils.isEmpty(ets[i].getText())) {
                return null;
            }
            numbers[i] = Integer.parseInt(ets[i].getText().toString().trim());
            Log.d("TAG","for i numbers[i] = " + numbers[i]);
        }

        int beginHour = numbers[0] * 10 + numbers[1];
        int beginMinute = numbers[2] * 10 + numbers[3];
        int endHour = numbers[4] * 10 + numbers[5];
        int endMinute = numbers[6] * 10 + numbers[7];

        return new MonitorTime(beginHour, beginMinute, endHour, endMinute);
    }



    /**
     * 查询监控时间段
     * */
    public static void getMonitorTimes(final Context context, final Handler handler, final int what, final String televisionId) {
        BmobQuery<Television> query = new BmobQuery<>();
        query.getObject(context, televisionId, new GetListener<Television>() {
            @Override
            public void onSuccess(Television television) {
                //Log.d("TAG", "查询成功！" + television.getMonitorTime().size());
                //monitorTimes = television.getMonitorTime();
                Message msg = Message.obtain();
                msg.what = what;
                msg.obj = television.getMonitorTime();
                handler.sendMessage(msg);
            }
            @Override
            public void onFailure(int i, String s) {
                Log.d("TAG", "查询失败！"+s);
            }
        });
    }

    /**
     * 检测时间段是否重叠
     *
     * @param monitorTimes 已添加的监控时间段集合
     *
     * @param monitorTime 要添加的监控时间段
     * */
    public static boolean notoverlap(ArrayList<MonitorTime> monitorTimes, MonitorTime monitorTime) {
        boolean result = true;
        if(null == monitorTimes) {
            return  true;
        }
        for (MonitorTime temp : monitorTimes) {
            if ((temp.getEndTime() >= monitorTime.getBeginTime() && temp.getBeginTime() <= monitorTime.getBeginTime()) ||
                    (temp.getEndTime() >= monitorTime.getEndTime() && temp.getBeginTime() <= monitorTime.getEndTime())) {
                result = false;
                break;
            }
        }
        return  result;
    }

    /**
     * 更新数据库
     *
     * @param monitorTimes 要被更新的数据（监控时间段集合）
     * */
    public static void updateMonitorTime(final Context context, final Handler handler, ArrayList<MonitorTime> monitorTimes, final String televisionId) {

        Television television = new Television();
        television.setMonitorTime(monitorTimes);
        television.update(context, televisionId, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.d("TAG", "数据库更新成功");
                //在这里查询数据库，
                getMonitorTimes(context, handler, StaticFinal.SHOW_MONITOR_TIMES,televisionId);

            }
            @Override
            public void onFailure(int i, String s) {
                Log.d("TAG", "数据库更新失败");
            }
        });
    }


    /**
     * 删除监控时间段
     *
     * @param monitorTimes 原监控时间段集合
     *
     * @param position 要删除的监控时间段所在位置
     *
     * */
    public static void delMonitorTime(final Context context, final Handler handler, final ArrayList<MonitorTime> monitorTimes, final int position) {
        //更新数据库
        //updateMonitorTime(context, handler, monitorTimes);

        ArrayList<MonitorTime> monitorTimeList = (ArrayList<MonitorTime>) monitorTimes.clone();//克隆对象
        monitorTimeList.remove(position);

        if(null != MainActivity.mBinder) {
            MainActivity.mBinder.updateMonitorTime(monitorTimeList, handler);
        }
    }

    /**
     * 添加监控时间段
     *
     * @param monitorTimes 已添加的监控时间段集合
     *
     * @param monitorTime 要添加的监控时间段
     * */
    public static void addMonitorTime(final Context context, final Handler handler, ArrayList<MonitorTime> monitorTimes, final MonitorTime monitorTime) {
        if(null == monitorTimes) {
            monitorTimes = new ArrayList<MonitorTime>();
        }
        //updateMonitorTime(context, handler,monitorTimes);
        ArrayList<MonitorTime> monitorTimeList;
        if(null != monitorTimes) {
            monitorTimeList = (ArrayList<MonitorTime>) monitorTimes.clone();//克隆对象
        }else{
            monitorTimeList = new ArrayList<MonitorTime>();
        }
        monitorTimeList.add(monitorTime);

        if(null != MainActivity.mBinder) {
            MainActivity.mBinder.updateMonitorTime(monitorTimeList, handler);
        } else {
            Log.d("TAG","null");
        }
    }
}
