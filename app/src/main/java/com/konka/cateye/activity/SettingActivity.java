package com.konka.cateye.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.konka.cateye.R;
import com.konka.cateye.StaticFinal;
import com.konka.cateye.adapter.MonitorTimesAdapter;
import com.konka.cateye.bean.MonitorTime;
import com.konka.cateye.bean.Television;
import com.konka.cateye.bean.User;
import com.konka.cateye.service.AutoMonitorService;
import com.konka.cateye.util.ExitApplication;
import com.konka.cateye.util.MonitorTimeUtils;
import com.konka.cateye.util.TVListenerUtils;

import java.util.ArrayList;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.listener.UpdateListener;
/**
 * Created by Tokgo on 2015-11-16.
 * 应用程序设置界面
 */

public class SettingActivity extends Activity {

    int mPsition;
    private String mTelevisionId;

    private Button mBtsetmonitortime;

    private MonitorTime mMonitorTime;
    private MonitorTimesAdapter mtAdapter;
    private ArrayList<MonitorTime> mMonitorTimes;

    private View mViewMonitortime;
    private EditText mEtbeginTime_hour;
    private EditText mEtbeginTime_minute;
    private EditText mEtendTime_hour;
    private EditText mEtendTime_minute;

    private SettingActivityHandler mHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
     * 输入的监控时间是否包含空
     * */
    private  boolean isNull() {
        return "".equals(mEtbeginTime_hour.getText().toString().trim()) || "".equals(mEtbeginTime_minute.getText().toString().trim()) ||
                "".equals(mEtendTime_hour.getText().toString().trim()) || "".equals(mEtendTime_minute.getText().toString().trim());
    }

    /**
     * 实例化三个按钮并注册监听
     * */
    private void initButtons() {
        mBtsetmonitortime = (Button) findViewById(R.id.bt_addmonitortime);
        mBtsetmonitortime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("setting", "添加时间段");
                final View monitortimeLayout = LayoutInflater.from(SettingActivity.this).inflate(R.layout.monitortimesetting, null);
                //弹出添加时段对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this).setTitle("添加自动监控时间段：").
                        setView(monitortimeLayout).setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMonitorTime = MonitorTimeUtils.viewToMonitorTime(SettingActivity.this, monitortimeLayout);
                        //检查是否有空输入
                        if(null == mMonitorTime) {
                            Toast.makeText(SettingActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                        }
                        //检测输入时间是否合法
                        else if (MonitorTimeUtils.legal(mMonitorTime)) {
                            MonitorTimeUtils.getMonitorTimes(SettingActivity.this, mHandler, StaticFinal.ADD_MONITOR_TIMES, mTelevisionId);
                        } else {
                            Toast.makeText(SettingActivity.this, "请输入合法的时间段", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                builder.show();
            }
        });
    }


    public class SettingActivityHandler extends Handler {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mMonitorTimes = (ArrayList<MonitorTime>) msg.obj;
            switch (msg.what) {
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
                                //显示选中监控时间段
                                mViewMonitortime = LayoutInflater.from(SettingActivity.this).inflate(R.layout.monitortimesetting, null);
                                //monitorTimeToView();
                                mEtbeginTime_hour = (EditText) mViewMonitortime.findViewById(R.id.et_beginTime_hour);
                                mEtbeginTime_minute = (EditText) mViewMonitortime.findViewById(R.id.et_beginTime_minute);
                                mEtendTime_hour = (EditText) mViewMonitortime.findViewById(R.id.et_endTime_hour);
                                mEtendTime_minute = (EditText) mViewMonitortime.findViewById(R.id.et_endTime_minute);

                                mEtbeginTime_hour.setText(mMonitorTime.getBeginHour() + "");
                                mEtbeginTime_minute.setText(mMonitorTime.getBeginMinute() + "");
                                mEtendTime_hour.setText(mMonitorTime.getEndHour() + "");
                                mEtendTime_minute.setText(mMonitorTime.getEndMinute() + "");
                                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this).setView(mViewMonitortime).
                                        setTitle("修改自动监控时间段：").setNegativeButton("删除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MonitorTimeUtils.getMonitorTimes(SettingActivity.this, mHandler, StaticFinal.DEL_MONITOR_TIMES, mTelevisionId);
                                    }
                                }).setPositiveButton("修改", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //检查是否有空输入isNull
                                        if (isNull()) {
                                            Log.d("TAG","输入不可以为空");
                                            Toast.makeText(SettingActivity.this, "输入不可以为空！", Toast.LENGTH_SHORT).show();
                                        } else {
                                            int beginHour = Integer.parseInt(mEtbeginTime_hour.getText().toString().trim());
                                            int beginMinute = Integer.parseInt(mEtbeginTime_minute.getText().toString().trim());
                                            int endHour = Integer.parseInt(mEtendTime_hour.getText().toString().trim());
                                            int endMinute = Integer.parseInt(mEtendTime_minute.getText().toString().trim());
                                            mMonitorTime = new MonitorTime(beginHour, beginMinute, endHour, endMinute);
                                            //检测输入时间是否合法
                                            if (MonitorTimeUtils.legal(mMonitorTime)) {
                                                mMonitorTimes.remove(position);
                                                boolean result = MonitorTimeUtils.notoverlap(mMonitorTimes, mMonitorTime);
                                                if (result) {
                                                    Toast.makeText(SettingActivity.this, "添加时间段成功", Toast.LENGTH_SHORT).show();
                                                    //Log.d("TAG", monitorTime.getBeginTime() + "");
                                                    MonitorTimeUtils.addMonitorTime(SettingActivity.this, mHandler, mMonitorTimes, mMonitorTime);
                                                    Log.d("TAG", mMonitorTime.getBeginTime() + "");
                                                } else {
                                                    Toast.makeText(SettingActivity.this, "时间重叠", Toast.LENGTH_SHORT).show();
                                                }

                                            } else {
                                                Toast.makeText(SettingActivity.this, "请输入合法的时间段", Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                    break;
            }
        }
    }

}
