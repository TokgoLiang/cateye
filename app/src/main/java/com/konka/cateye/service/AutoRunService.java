package com.konka.cateye.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.konka.cateye.activity.MainActivity;
import com.konka.cateye.bean.HistoryRecord;
import com.konka.cateye.bean.MonitorTime;
import com.konka.cateye.bean.OnOrOff;
import com.konka.cateye.bean.Television;
import com.konka.cateye.util.ExitApplication;
import com.konka.cateye.util.SystemDataUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.ValueEventListener;

public class AutoRunService extends Service implements Runnable {
    private static Television mTelevision;
    private static OnOrOff mOnOrOff;
    private static BmobRealTimeData mOnOrOffListener;
    private static ExecutorService mExecutorService;

    public AutoRunService() {
    }

    private static boolean isConnect() {
        if (mOnOrOffListener != null) {
            return mOnOrOffListener.isConnected();
        }
        return false;
    }

    public static void startListen() {
        if (isConnect()) {
            mOnOrOffListener.subRowUpdate("OnOrOff", mOnOrOff.getObjectId());
            Log.d("cateye", "start listen");
        }
    }

    public static void stopListen() {
        if (isConnect()) {
            mOnOrOffListener.unsubRowUpdate("OnOrOff", mOnOrOff.getObjectId());
            Log.d("cateye", "stop listen");
        }
    }


    private void findTelevision() {
        String mac = SystemDataUtils.getLocalMacAddressFromIp();
        BmobQuery<Television> query = new BmobQuery<Television>();
        query.addWhereEqualTo("mac", mac);
        query.findObjects(this, new FindListener<Television>() {
            @Override
            public void onSuccess(List<Television> list) {
                if (list.size() > 0) {
                    mTelevision = list.get(0);
                    Log.d("cateye", "tv is:" + mTelevision.getObjectId());
                    findOnOrOff();
                    mExecutorService.submit(AutoRunService.this);
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
        query.findObjects(this, new FindListener<OnOrOff>() {
            @Override
            public void onSuccess(List<OnOrOff> list) {
                mOnOrOff = list.get(0);
                Log.d("cateye", "OOO is:" + mOnOrOff.getObjectId());
                if (mOnOrOff.getState()) {
                    mOnOrOff.setState(false);
                    mOnOrOff.update(AutoRunService.this, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("cateye", "恢复ooo");
                            listen();
                        }

                        @Override
                        public void onFailure(int i, String s) {

                        }
                    });
                } else {
                    listen();
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private void listen() {
        mOnOrOffListener = new BmobRealTimeData();
        mOnOrOffListener.start(this, new ValueEventListener() {
            @Override
            public void onConnectCompleted() {
                if (mOnOrOffListener.isConnected()) {
                    Log.d("cateye", "begin monitor ooo");
                    mOnOrOffListener.subRowUpdate("OnOrOff", mOnOrOff.getObjectId());
                }
            }

            @Override
            public void onDataChange(JSONObject jsonObject) {
                Log.d("cateye", "find ooo change");
                mOnOrOffListener.unsubRowUpdate("OnOrOff", mOnOrOff.getObjectId());
                try {
                    JSONObject data = jsonObject.getJSONObject("data");
                    mOnOrOff.setState(data.getBoolean("state"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mOnOrOff.getState()) {
                    Log.d("cateye", "close");
                    updateOnOrOff(false);
                    Intent intent = new Intent(AutoRunService.this, AutoMonitorService.class);
                    stopService(intent);
                    ExitApplication.getInstance().exit(AutoRunService.this, mTelevision,1);
                } else {
                    Log.d("cateye", "open");
                    updateOnOrOff(true);
                    Intent intent = new Intent(AutoRunService.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    private void updateOnOrOff(boolean isOn) {
        mOnOrOff.setState(isOn);
        //mOnOrOff.setIsRequest(!mOnOrOff.getIsRequest());
        mOnOrOff.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.d("TAG", "on or off update success");
                mOnOrOffListener.subRowUpdate("OnOrOff", mOnOrOff.getObjectId());
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }

    private Date getDate() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -7);
        date = calendar.getTime();
        return date;
    }

    @Override
    public void run() {
        List<BmobQuery<HistoryRecord>> and = new ArrayList<BmobQuery<HistoryRecord>>();
        BmobQuery<HistoryRecord> queryTime = new BmobQuery<HistoryRecord>();
        queryTime.addWhereLessThan("createdAt", new BmobDate(getDate()));
        and.add(queryTime);

        BmobQuery<HistoryRecord> queryTelevision = new BmobQuery<HistoryRecord>();
        queryTelevision.addWhereEqualTo("televisionId", mTelevision);
        and.add(queryTelevision);

        BmobQuery<HistoryRecord> queryAnd = new BmobQuery<HistoryRecord>();
        queryAnd.and(and);

        queryAnd.findObjects(this, new FindListener<HistoryRecord>() {
            @Override
            public void onSuccess(List<HistoryRecord> list) {
                Log.d("cateye","delete history");
                for (HistoryRecord historyRecord : list) {
                    historyRecord.delete(AutoRunService.this);
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("cateye", "run service on start");
        findTelevision();
        mExecutorService = Executors.newCachedThreadPool();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("cateye", "run service on destroy");
    }
}
