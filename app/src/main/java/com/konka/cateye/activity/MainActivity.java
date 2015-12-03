package com.konka.cateye.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v17.leanback.animation.LogDecelerateInterpolator;
import android.text.InputType;
import android.text.LoginFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.konka.cateye.R;
import com.konka.cateye.StaticFinal;
import com.konka.cateye.bean.MonitorTime;
import com.konka.cateye.bean.RealTimeMessage;
import com.konka.cateye.bean.RealTimeRecord;
import com.konka.cateye.bean.Television;
import com.konka.cateye.bean.User;
import com.konka.cateye.service.AutoMonitorService;
import com.konka.cateye.service.AutoRunService;
import com.konka.cateye.util.ExitApplication;
import com.konka.cateye.util.FirstOpen;
import com.konka.cateye.util.MonitorTimeUtils;
import com.konka.cateye.util.SystemDataUtils;
import com.konka.cateye.util.TVListenerUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.ValueEventListener;

/**
 * Created by Tokgo on 2015-11-16.
 * 应用程序主界面
 * 合适的时间关闭对TV的监听
 */
public class MainActivity extends Activity {

    public static AutoMonitorService.AutoMonitorBinder mBinder = null;

    private ImageView mIvQrCode;
    private Bitmap mBitmap;
    private String mMac;
    private boolean mIsUnbound = false;

    private User mUser = null;
    private Television mTelevision = null;
    private RealTimeRecord mRealTimeRecord = null;
    private RealTimeMessage mRealTimeMessage = null;
    private boolean mIsBind = false;

    private ImageButton mIbSwitchUser;
    private ImageButton mIbExit;
    private ImageButton mIbSetting;

    private MainActivityHandler mHandler = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leadding);
        ExitApplication.getInstance().addActivity(this);

        // 初始化BmobSDK
        Bmob.initialize(this, StaticFinal.APPID);


        //初始化mHandler
        mHandler = new MainActivityHandler();

        Timer timer = new Timer();
        TimerTask tast = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = StaticFinal.LEADDING_TIME;
                mHandler.sendMessage(msg);
            }
        };
        timer.schedule(tast, 1500);

        Intent intent = new Intent(this, AutoMonitorService.class);
        //先start再bind，这样在应用切到后台的时候service才不会关闭
        startService(intent);
    }

    /**
     * 消息处理器
     * 异常消息——弹框提示
     * 绑定消息——启动对TV表的监听
     * 已经被帮顶消息——显示绑定后界面
     */
    public class MainActivityHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            //处理消息
            switch (msg.what) {
                //引导等待结束
                case StaticFinal.LEADDING_TIME:
                    FirstOpen.isFirstOpen(MainActivity.this, mHandler);
                    //setContentView(R.layout.activity_unbounded);
                    break;
                //异常消息处理
                case StaticFinal.INTERNET_ERROR:
                    Toast.makeText(MainActivity.this, "网络连接失败，请检查网络", Toast.LENGTH_LONG).show();
                    break;
                //处理绑定
                case StaticFinal.BOUND:
                    //启动对TV表的监听
                    TVListenerUtils.listenTV(MainActivity.this, mHandler);
                    break;
                //已绑定状态
                case StaticFinal.BOUNDED:
                    Intent intent = new Intent(MainActivity.this,AutoRunService.class);
                    startService(intent);
                    //开启监控
                    initTelevision();
                    TVListenerUtils.stopListen();
                    //显示已绑定界面
                    setContentView(R.layout.activity_bounded);
                    //如果首次启动，提醒设置监控时间段
                    if (mIsUnbound) {
                        setMonitorTime();
                        mIsUnbound = false;
                    }
                    //初始化三个按钮
                    initImageButtons();
                    break;
                //未绑定状态
                case StaticFinal.UNBOUNDED:
                    //启动绑定监听
                    Message msg2 = new Message();
                    msg2.what = StaticFinal.BOUND;
                    mHandler.sendMessage(msg2);
                    mIsUnbound = true;
                    //显示未绑定界面
                    setContentView(R.layout.activity_unbounded);
                    mBitmap = FirstOpen.createQrCode();
                    mIvQrCode = (ImageView) findViewById(R.id.iv_QrCode);
                    mIvQrCode.setImageBitmap(mBitmap);
                    Log.d("TAG", "尚未绑定");
                    //完成初始化之后去监听TV表
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 初始化三个按钮注册监听
     */
    private void initImageButtons() {
        initIbSwitchUser();
        initIbSetting();
        initIbExit();
    }

    /**
     * 检测输入密码是否正确
     */
    private void checkPassword(final int flag) {
        final EditText inputServer = new EditText(MainActivity.this);
        inputServer.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputServer.setFocusable(true);
        //弹出验证密码对话框
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("输入密码：").setView(inputServer).setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final String input = inputServer.getText().toString().trim();
                //从数据库获取密码
                String userId = mUser.getObjectId();
                Log.d("TAG", "userId" + userId);
                BmobQuery<User> query = new BmobQuery<User>();
                query.getObject(MainActivity.this, userId, new GetListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        Log.d("TAG", user.getRealPassword() + input);
                        if (user.getRealPassword().equals(input)) {
                            switch (flag) {
                                //切换用户
                                case StaticFinal.SWITCH_USER:
                                    Log.d("TAG", "switch user");
                                    Message message = new Message();
                                    message.what = StaticFinal.UNBOUNDED;
                                    mHandler.sendMessage(message);
                                    break;
                                //退出监控
                                case StaticFinal.EXIT:
                                    //关闭service
                                    Intent intent = new Intent(MainActivity.this, AutoMonitorService.class);
                                    stopService(intent);
                                    //关闭所有Activity
                                    ExitApplication.getInstance().exit(MainActivity.this, mTelevision,0);
                                    break;
                                //设置监控时间段
                                case StaticFinal.GOTO_SETTING:
                                    Intent intent2 = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("televisionId", mTelevision.getObjectId());
                                    intent2.putExtras(bundle);
                                    intent2.setClass(MainActivity.this, SettingActivity.class);
                                    startActivity(intent2);
                                    break;

                            }
                        } else {
                            Toast.makeText(MainActivity.this, "密码错误请重新输入！", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int i, String s) {

                    }
                });
            }
        });
        builder.show();
    }

    /**
     * 初始化切换用户按钮，并监听点击事件
     */
    private void initIbSwitchUser() {
        mIbSwitchUser = (ImageButton) findViewById(R.id.ib_switchuser);
        mIbSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPassword(StaticFinal.SWITCH_USER);
            }
        });

    }

    /**
     * 初始化退出监控按钮，并监听点击事件
     */
    private void initIbExit() {
        mIbExit = (ImageButton) findViewById(R.id.ib_exit);
        mIbExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPassword(StaticFinal.EXIT);
            }
        });
    }

    /**
     * 初始化设置监控时间段按钮，并监听点击事件
     */
    private void initIbSetting() {
        mIbSetting = (ImageButton) findViewById(R.id.ib_setting);
        mIbSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPassword(StaticFinal.GOTO_SETTING);
            }
        });
    }

    /**
     * 获取binder，binder可以作为service的通信接口，用来调用service里面binder的方法
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获得binder
            mBinder = (AutoMonitorService.AutoMonitorBinder) service;
            if (!mBinder.isRunning()) {
                //设置service里的数据，使service正式开始工作
                mBinder.setDataAndStartMonitor(mTelevision, mRealTimeRecord, mRealTimeMessage, mHandler);
                mIsBind = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("cateye", "bind failed");
        }
    };


    /**
     * 在确定拿到数据后调用
     * 绑定SERVICE，bind完才能与、service通信。
     */
    private void serviceBind() {
        Intent bindIntent = new Intent(this, AutoMonitorService.class);
        bindService(bindIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 获取Service所需要的数据，由你提供啦，这里只是测试用的
     */
    private void initTelevision() {
        String mac = SystemDataUtils.getLocalMacAddressFromIp();
        //初始化mTelevision
        BmobQuery<Television> televisionQuery = new BmobQuery<Television>();
        televisionQuery.addWhereEqualTo("mac", mac);
        televisionQuery.include("userId");
        televisionQuery.findObjects(MainActivity.this, new FindListener<Television>() {
            @Override
            public void onSuccess(List<Television> list) {
                if (list.size() != 0) {
                    mTelevision = list.get(0);
                    mUser = mTelevision.getUserId();
                    initRealTimeRecord();
                } else {
                    Log.d("TAG", "TV list is empty");
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private void initRealTimeRecord() {
        //初始化mRealTimeRecord
        BmobQuery<RealTimeRecord> realTimeRecordQuery = new BmobQuery<RealTimeRecord>();
        realTimeRecordQuery.addWhereEqualTo("televisionId", mTelevision.getObjectId());
        realTimeRecordQuery.findObjects(MainActivity.this, new FindListener<RealTimeRecord>() {
            @Override
            public void onSuccess(List<RealTimeRecord> list) {
                if (list.size() != 0) {
                    mRealTimeRecord = list.get(0);
                    initRealTimeMessage();
                } else {
                    Log.d("TAG", "Record list is empty");
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private void initRealTimeMessage() {
        //初始化mRealTimeMessage
        BmobQuery<RealTimeMessage> realTimeMessageQuery = new BmobQuery<RealTimeMessage>();
        realTimeMessageQuery.addWhereEqualTo("televisionId", mTelevision.getObjectId());
        realTimeMessageQuery.findObjects(MainActivity.this, new FindListener<RealTimeMessage>() {

            @Override
            public void onSuccess(List<RealTimeMessage> list) {
                if (list.size() != 0) {
                    mRealTimeMessage = list.get(0);
                    serviceBind();
                } else {
                    Log.d("TAG", "message list is empty");
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    /**
     * 记得unbind一下，还有，在整个应用退出的时候stop下service，不然service就停不下来了
     */
    @Override
    protected void onDestroy() {
        Log.d("TAG", "main activity on destroy");
        super.onDestroy();
        if (mIsBind)
            unbindService(mServiceConnection);
    }

    {
        TVListenerUtils.stopListen();
    }


    /**
     * 如果是否第一次启动，提示设置监控时间段，完成之后设置第一次启动为false
     */
    private void setMonitorTime() {
        //判读是否第一次启动
        //弹框提示设置监控时间段
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle("是否去设置监控时间段？").
                setNegativeButton("跳过", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("TAG", "这里应该去跳转");
                //跳转到设置监控时间段页面
                Intent intent2 = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("televisionId", mTelevision.getObjectId());
                intent2.putExtras(bundle);
                intent2.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent2);
            }
        });
        builder.show();
    }


}
