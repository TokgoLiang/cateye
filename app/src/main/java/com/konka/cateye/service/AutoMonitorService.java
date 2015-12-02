package com.konka.cateye.service;


import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.konka.cateye.StaticFinal;
import com.konka.cateye.activity.MainActivity;
import com.konka.cateye.bean.HistoryRecord;
import com.konka.cateye.bean.MonitorTime;
import com.konka.cateye.bean.RealTimeMessage;
import com.konka.cateye.bean.RealTimeRecord;
import com.konka.cateye.bean.Television;
import com.konka.cateye.util.ServerDatabaseUtils;
import com.konka.cateye.util.SystemDataUtils;
import com.konka.cateye.view.FloatWindowLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.ValueEventListener;

public class AutoMonitorService extends Service {
    private static final int THREAD_NUMBER = 10;
    private static final int TIME_INTERVAL_OF_MINUTE = 15;

    private RealTimeRecord mRealTimeRecord = null;
    private BmobRealTimeData mRealTimeRecordListener = null;

    private RealTimeMessage mRealTimeMessage = null;
    private BmobRealTimeData mRealTimeMessageListener = null;

    private Television mTelevision = null;
    private ArrayList<MonitorTime> mOldMonitorTime = null;

    private FloatWindowLayout mFloatWindowLayout = null;
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mLayoutParams = null;

    private ServiceHandler mServiceHandler = null;
    private MainActivity.MainActivityHandler mActivityHandler = null;
    private ExecutorService mExecutorService = null;
    private SystemDataUtils mSystemDataUtils = null;
    private ServerDatabaseUtils mServerDatabaseUtils = null;
    private boolean mIsRunning = false;
    private AutoMonitorBinder mAutoMonitorBinder = new AutoMonitorBinder();


    /**
     * 自己的binder，这个是返回给Activity的，供activity调用通信用
     */
    public class AutoMonitorBinder extends Binder {
        //初始化Service各个对象，并开始各种监控
        public void setDataAndStartMonitor(Television television, RealTimeRecord realTimeRecord,
                                           RealTimeMessage realTimeMessage, MainActivity.MainActivityHandler handler) {
            mTelevision = television;
            mRealTimeRecord = realTimeRecord;
            mRealTimeMessage = realTimeMessage;
            mActivityHandler = handler;

            mIsRunning = true;
            mExecutorService.submit(new AutoMonitorThread());
            startRealTimeMonitor();
            startRealTimeMessage();
        }

        //更新监控时间
        public void updateMonitorTime(ArrayList<MonitorTime> monitorTime, Handler handler) {
            mOldMonitorTime = mTelevision.getMonitorTime();//备份原有对象
            mTelevision.setMonitorTime(monitorTime);
            mServerDatabaseUtils.updateTelevision(mTelevision, handler);
        }

        //判断Service是否已开始工作
        public boolean isRunning() {
            return mIsRunning;
        }
    }

    public class ServiceHandler extends Handler {
        public static final int UPDATE_TELEVISION_FAILURE = 1;
        public static final int UPDATE_REAL_TIME_RECORD_SUCCESS = 2;
        public static final int UPDATE_REAL_TIME_MESSAGE_SUCCESS = 3;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TELEVISION_FAILURE:
                    mTelevision.setMonitorTime(mOldMonitorTime);//网络通信失败则恢复原有监控时间
                    Message message = new Message();
                    message.what = StaticFinal.INTERNET_ERROR;
                    mActivityHandler.sendMessage(message);
                    break;
                case UPDATE_REAL_TIME_RECORD_SUCCESS://恢复监听，下同
                    mRealTimeRecordListener.subRowUpdate("RealTimeRecord", mRealTimeRecord.getObjectId());
                    break;
                case UPDATE_REAL_TIME_MESSAGE_SUCCESS:
                    mRealTimeMessageListener.subRowUpdate("RealTimeMessage", mRealTimeMessage.getObjectId());
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 历史记录，定时获取截图与信息上传并保存
     */
    private class AutoMonitorThread implements Runnable {
        @Override
        public void run() {
            while (mIsRunning) {
                if (mSystemDataUtils.isTimeNmow(mTelevision)) {
                    HistoryRecord historyRecord = new HistoryRecord();
                    historyRecord.setTelevisionId(mTelevision);
                    historyRecord.setInformation(mSystemDataUtils.getAppName());
                    mServerDatabaseUtils.screenShot(historyRecord);
                }
                try {
                    TimeUnit.MINUTES.sleep(TIME_INTERVAL_OF_MINUTE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 开始即时内容监控
     */
    private void startRealTimeMonitor() {
        mRealTimeRecordListener = new BmobRealTimeData();
        mRealTimeRecordListener.start(AutoMonitorService.this, new ValueEventListener() {
            @Override
            public void onConnectCompleted() {
                if (mRealTimeRecordListener.isConnected()) {//连接成功则开始监听
                    mRealTimeRecordListener.subRowUpdate("RealTimeRecord", mRealTimeRecord.getObjectId());
                    Log.d("cateye", "record start:" + mRealTimeRecord.getObjectId());
                }
            }

            @Override
            public void onDataChange(JSONObject jsonObject) {
                Log.e("cateye", "record: find the change:" + jsonObject.toString());
                mRealTimeRecordListener.unsubRowUpdate("RealTimeRecord", mRealTimeRecord.getObjectId());//先接触监听再更新，防止更新触发监听
                if (BmobRealTimeData.ACTION_UPDATEROW.equals(jsonObject.optString("action"))) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    mRealTimeRecord.setIsRequest(!data.optBoolean("isRequest"));
                    mServerDatabaseUtils.screenShot(mRealTimeRecord);//获取截图并上传更新
                }
            }
        });
    }

    /**
     * 监听发送过来的消息
     */
    private void startRealTimeMessage() {
        mRealTimeMessageListener = new BmobRealTimeData();
        mRealTimeMessageListener.start(AutoMonitorService.this, new ValueEventListener() {
            @Override
            public void onConnectCompleted() {
                if (mRealTimeMessageListener.isConnected()) {
                    mRealTimeMessageListener.subRowUpdate("RealTimeMessage", mRealTimeMessage.getObjectId());
                    Log.d("cateye", "message start:" + mRealTimeMessage.getObjectId());
                }
            }

            @Override
            public void onDataChange(JSONObject jsonObject) {
                Log.e("cateye", "message: find the change:" + jsonObject.toString());

                mRealTimeMessageListener.unsubRowUpdate("RealTimeMessage", mRealTimeMessage.getObjectId());
                if (BmobRealTimeData.ACTION_UPDATEROW.equals(jsonObject.optString("action"))) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    mRealTimeMessage.setMessage(data.optString("message"));
                    mRealTimeMessage.setIsRequest(!data.optBoolean("isRequest"));
                    mServerDatabaseUtils.updateRealTimeMessage(mRealTimeMessage);


                    showFloatWindow();//显示消息框
                }
            }
        });
    }

    /**
     * 在各个电视界面显示消息框
     */
    private void showFloatWindow() {
        mWindowManager = (WindowManager) getApplication().getSystemService(Application.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.gravity = Gravity.END | Gravity.BOTTOM;
        mLayoutParams.width = 350;
        mLayoutParams.height = 200;
        Log.e("cateye", "windowManager:" + mWindowManager);
        mFloatWindowLayout = new FloatWindowLayout(this);
        Log.e("cateye", "layout create");
        mFloatWindowLayout.setMessage(mRealTimeMessage.getMessage());
        mWindowManager.addView(mFloatWindowLayout, mLayoutParams);
        Log.e("cateye", "view add");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("cateye", "monitor service start");
        mSystemDataUtils = new SystemDataUtils(this);
        mServiceHandler = new ServiceHandler();
        mServerDatabaseUtils = new ServerDatabaseUtils(this, mServiceHandler);
        mExecutorService = Executors.newFixedThreadPool(THREAD_NUMBER);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.e("cateye", "monitor service bind");
        return mAutoMonitorBinder;
    }

    /**
     * 接触各种监听绑定，关闭线程
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("cateye", "monitor service destroy");
        mIsRunning = false;
        mExecutorService.shutdown();
        mRealTimeRecordListener.unsubRowUpdate("RealTimeRecord", mRealTimeRecord.getObjectId());
        mRealTimeMessageListener.unsubRowUpdate("RealTimeMessage", mRealTimeMessage.getObjectId());
    }
}
