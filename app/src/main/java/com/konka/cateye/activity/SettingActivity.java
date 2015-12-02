package com.konka.cateye.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.konka.cateye.R;
import com.konka.cateye.StaticFinal;
import com.konka.cateye.adapter.MonitorTimesAdapter;
import com.konka.cateye.bean.MonitorTime;
import com.konka.cateye.util.ExitApplication;
import com.konka.cateye.util.MonitorTimeUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;

/**
 * Created by Tokgo on 2015-11-16.
 * 应用程序设置界面
 */

public class SettingActivity extends Activity {


    private volatile MyThread thread;
    private EditText[] ets;
    private Button mBtOK;
    private Button mBtCancel;
    private Button mBtUpda;
    private Button mBtDelete;

    private boolean flag;

    private View mMonitortimeLayout;

    int mPsition;
    private String mTelevisionId;

    private ImageButton mBtsetmonitortime;

    private MonitorTime mMonitorTime;
    private MonitorTimesAdapter mtAdapter;
    private ArrayList<MonitorTime> mMonitorTimes;

    private SettingActivityHandler mHandler;

    Dialog dialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMonitortimeLayout= LayoutInflater.from(SettingActivity.this).inflate(R.layout.monitortimeadd, null);

        flag = true;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mTelevisionId = bundle.getString("televisionId");
        Log.d("TAG","跳转到setting"+mTelevisionId);

        setContentView(R.layout.activity_setting);
        ExitApplication.getInstance().addActivity(this);

        //初始化mHandle
        mHandler = new SettingActivityHandler();
        // 初始化BmobSDK
        Bmob.initialize(this, StaticFinal.APPID);
        //显示监控时间段
        MonitorTimeUtils.getMonitorTimes(SettingActivity.this, mHandler, StaticFinal.SHOW_MONITOR_TIMES, mTelevisionId);
        //初始化添加监控时间段按钮
        initButtons();
    }


    /**
     * 实例化按钮并注册监听
     * */
    private void initButtons() {
        mBtsetmonitortime = (ImageButton) findViewById(R.id.bt_addmonitortime);
        mBtsetmonitortime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("setting", "添加时间段");
                mMonitortimeLayout= LayoutInflater.from(SettingActivity.this).inflate(R.layout.monitortimeadd, null);
                //弹出添加时段对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this).setView(mMonitortimeLayout);
                dialog = builder.show();

                initViews();
                for(int i = 0; i < ets.length; i++) {
                    final int curIndex = i;
                    listenFocusChange(curIndex);
                    listenTextChanged(curIndex, StaticFinal.TYPE_ADD);
                }

        }
    });
    }


    public class SettingActivityHandler extends Handler {

        private int index;

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(null != msg.obj) {
                mMonitorTimes = (ArrayList<MonitorTime>) msg.obj;
            }
            switch (msg.what) {
                //设置监控时间闪烁效果
                case StaticFinal.CHANGE_INDEX :
                    index = msg.arg1;
                    break;
                case StaticFinal.CHANGE_TO_WHITE :
                    ets[index].setTextColor(Color.WHITE);
                    break;
                case StaticFinal.CHANGE_TO_BLACK :
                    ets[index].setTextColor(Color.BLACK);
                    break;

                //添加监控时间段
                case StaticFinal.ADD_MONITOR_TIMES :
                    boolean result = MonitorTimeUtils.notoverlap(mMonitorTimes, mMonitorTime);
                    if (result) {
                        Toast.makeText(SettingActivity.this, "添加时间段成功", Toast.LENGTH_SHORT).show();
                        MonitorTimeUtils.addMonitorTime(SettingActivity.this, mHandler, mMonitorTimes, mMonitorTime);
                        Log.d("TAG", mMonitorTime.getBeginTime() + "");
                    } else {
                        Toast.makeText(SettingActivity.this, "时间重叠", Toast.LENGTH_SHORT).show();
                    }
                    break;
                //删除监控时间段
                case StaticFinal.DEL_MONITOR_TIMES :
                    MonitorTimeUtils.delMonitorTime(SettingActivity.this, mHandler, mMonitorTimes, mPsition);
                    Log.d("TAG","我是handler里面的cDEL_MONITOR_TIMES");
                    break;
                //修改监控时间
                case StaticFinal.UPDATE_MONITOR_TIMES :
                    MonitorTimeUtils.updateMonitorTime(SettingActivity.this, mHandler, mMonitorTimes, mTelevisionId);
                    break;
                //显示监控时间段
                case StaticFinal.SHOW_MONITOR_TIMES :
                    if(null != mMonitorTimes) {
                        //初始化控件
                        ListView monitorTimesLv = (ListView) findViewById(R.id.lv_monitortimes);
                        //初始化适配器
                        mtAdapter = new MonitorTimesAdapter(mMonitorTimes, SettingActivity.this);
                        //为ListView控件添加适配器
                        monitorTimesLv.setAdapter(mtAdapter);
                        //添加ListView控件中的每个Item的点击事件
                        monitorTimesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                SettingActivity.this.mPsition = position;
                                //获取选中监控时间段
                                mMonitorTime = mMonitorTimes.get(position);
                                //Log.d("TAG","mMonitorTimes"+mMonitorTimes.size() + "\nposition" + position);
                                //显示选中监控时间段
                                mMonitortimeLayout = LayoutInflater.from(SettingActivity.this).inflate(R.layout.monitortimesetting, null);
                                //弹出添加时段对话框
                                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this).setView(mMonitortimeLayout);
                                dialog = builder.show();
                                //将监控时间段显示到monitortimesetting.xml
                                monitorTimeToView(mMonitorTimes,position);
                            }
                        });
                    }
                    break;
            }
        }
    }

    public void initViews() {

        mBtOK = (Button) mMonitortimeLayout.findViewById(R.id.bt_OK);
        //2.初始化组件
        mBtOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG","确定按钮");
                mMonitorTime = MonitorTimeUtils.viewToMonitorTime(mMonitortimeLayout);
                //检查是否有空输入
                if (null == mMonitorTime) {
                    Toast.makeText(SettingActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                }
                //检测输入时间是否合法
                else if (MonitorTimeUtils.legal(mMonitorTime)) {
                    MonitorTimeUtils.getMonitorTimes(SettingActivity.this, mHandler, StaticFinal.ADD_MONITOR_TIMES, mTelevisionId);
                    //关闭窗口
                    dialog.dismiss();
                } else {
                    Toast.makeText(SettingActivity.this, "请输入合法的时间段", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtCancel = (Button) mMonitortimeLayout.findViewById(R.id.bt_cancel);
        mBtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭窗口
                dialog.dismiss();
            }
        });

        ets = new EditText[8];
        ets[0] = (EditText) mMonitortimeLayout.findViewById(R.id.et_beginTime_hour0);
        ets[1] = (EditText) mMonitortimeLayout.findViewById(R.id.et_beginTime_hour1);
        ets[2] = (EditText) mMonitortimeLayout.findViewById(R.id.et_beginTime_minute0);
        ets[3] = (EditText) mMonitortimeLayout.findViewById(R.id.et_beginTime_minute1);
        ets[4] = (EditText) mMonitortimeLayout.findViewById(R.id.et_endTime_hour0);
        ets[5] = (EditText) mMonitortimeLayout.findViewById(R.id.et_endTime_hour1);
        ets[6] = (EditText) mMonitortimeLayout.findViewById(R.id.et_endTime_minute0);
        ets[7] = (EditText) mMonitortimeLayout.findViewById(R.id.et_endTime_minute1);
    }

    /**
     * 监听焦点改变事件
     * */
    public void listenFocusChange(final int index) {
        //3.监听焦点改变事件
        ets[index].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //如果拥有焦点则内容闪烁
                if (hasFocus) {
                    //设置光标位置
                    ets[index].setSelection(ets[index].getText().length());
                    Message message = new Message();
                    message.what = StaticFinal.CHANGE_INDEX;
                    message.arg1 = index;
                    mHandler.sendMessage(message);
                    //开启线程
                    thread = new MyThread();
                    thread.start();
                } else {
                    //Log.d("TAG", "焦点移出" + index);
                    Message message = new Message();
                    message.what = StaticFinal.CHANGE_TO_BLACK;
                    mHandler.sendMessage(message);
                }

            }
        });

    }

    public void listenTextChanged(final int index, final int type) {
        //4.监听输入，焦点后移
        ets[index].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int nextIndex = index + 1;

                //如果不是最后一个,而且有输入
                if ((!TextUtils.isEmpty(ets[index].getText())) && (index < ets.length - 1)) {
                    //焦点后移
                    ets[nextIndex].requestFocus();

                } else if ((!TextUtils.isEmpty(ets[index].getText())) && (index == ets.length - 1)) {
                    //关闭线程
                    flag = false;
                    Log.d("TAG", "" + ets[7].getText().toString());
                    //焦点给确定按钮
                    if(StaticFinal.TYPE_ADD == type){
                        mBtOK.requestFocus();
                    } else if(StaticFinal.TYPE_UPDATE == type) {
                        mBtUpda.requestFocus();
                    }
                }
            }
        });
    }

    public void monitorTimeToView(final ArrayList<MonitorTime> monitorTimes, final int position){

        int[] numbers = new int[8];
        numbers[0] = mMonitorTime.getBeginHour() / 10;
        numbers[1] = mMonitorTime.getBeginHour() % 10;
        numbers[2] = mMonitorTime.getBeginMinute() / 10;
        numbers[3] = mMonitorTime.getBeginMinute() % 10;
        numbers[4] = mMonitorTime.getEndHour() / 10;
        numbers[5] = mMonitorTime.getEndHour() % 10;
        numbers[6] = mMonitorTime.getEndMinute() / 10;
        numbers[7] = mMonitorTime.getEndMinute() % 10;

        ets = new EditText[8];
        ets[0] = (EditText) mMonitortimeLayout.findViewById(R.id.et_beginTime_hour0);
        ets[1] = (EditText) mMonitortimeLayout.findViewById(R.id.et_beginTime_hour1);
        ets[2] = (EditText) mMonitortimeLayout.findViewById(R.id.et_beginTime_minute0);
        ets[3] = (EditText) mMonitortimeLayout.findViewById(R.id.et_beginTime_minute1);
        ets[4] = (EditText) mMonitortimeLayout.findViewById(R.id.et_endTime_hour0);
        ets[5] = (EditText) mMonitortimeLayout.findViewById(R.id.et_endTime_hour1);
        ets[6] = (EditText) mMonitortimeLayout.findViewById(R.id.et_endTime_minute0);
        ets[7] = (EditText) mMonitortimeLayout.findViewById(R.id.et_endTime_minute1);

        for(int i=0; i<ets.length; i++) {
            ets[i].setText(numbers[i]+"");
        }


        //initViews();
        for(int i = 0; i < ets.length; i++) {
            final int curIndex = i;
            listenFocusChange(curIndex);
            listenTextChanged(curIndex, StaticFinal.TYPE_UPDATE);
        }


        mBtUpda = (Button) mMonitortimeLayout.findViewById(R.id.bt_update);
        mBtUpda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MonitorTime> monitorTimes2 = monitorTimes;
                mMonitorTime = MonitorTimeUtils.viewToMonitorTime(mMonitortimeLayout);
                //检查是否有空输入
                if (null == mMonitorTime) {
                    Toast.makeText(SettingActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                }
                //检测输入时间是否合法
                else if (MonitorTimeUtils.legal(mMonitorTime)) {
                    //MonitorTimeUtils.getMonitorTimes(SettingActivity.this, mHandler, StaticFinal.ADD_MONITOR_TIMES, mTelevisionId);
                    //Log.d("TAG","mMonitorTimes"+monitorTimes2.size() + "\nposition" + position);

                    monitorTimes2.remove(position);
                    boolean result = MonitorTimeUtils.notoverlap(monitorTimes2, mMonitorTime);
                    if (result) {
                        Toast.makeText(SettingActivity.this, "添加时间段成功", Toast.LENGTH_SHORT).show();
                        MonitorTimeUtils.addMonitorTime(SettingActivity.this, mHandler, monitorTimes2, mMonitorTime);
                        Log.d("TAG", mMonitorTime.getBeginTime() + "");
                        //关闭窗口
                        dialog.dismiss();
                    } else {
                        Toast.makeText(SettingActivity.this, "时间重叠", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SettingActivity.this, "请输入合法的时间段", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mBtDelete = (Button) mMonitortimeLayout.findViewById(R.id.bt_delete);
        mBtDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonitorTimeUtils.getMonitorTimes(SettingActivity.this, mHandler, StaticFinal.DEL_MONITOR_TIMES, mTelevisionId);;
                //关闭窗口
                dialog.dismiss();
            }
        });

    }

    public class MyThread extends Thread implements Runnable{

        @Override
        public void run() {
            try {
                while (flag) {

                    Message message2 = new Message();
                    message2.what = StaticFinal.CHANGE_TO_WHITE;
                    mHandler.sendMessage(message2);
                    thread.sleep(500);

                    Message message = new Message();
                    message.what = StaticFinal.CHANGE_TO_BLACK;
                    mHandler.sendMessage(message);
                    thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
