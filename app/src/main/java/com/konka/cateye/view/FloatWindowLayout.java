package com.konka.cateye.view;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.konka.cateye.R;

/**
 * Created by Morning on 2015-11-20.
 */
public class FloatWindowLayout extends LinearLayout implements View.OnClickListener{

    private TextView mTvMessage = null;
    private Button mBtnOK = null;
    private Context mContext = null;

    /**
     * 悬浮窗初始化
     * @param context
     */
    public FloatWindowLayout(Context context) {
        super(context);
        mContext = context;
        setOrientation(LinearLayout.VERTICAL);
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(context).inflate(R.layout.view_float_window, this);
        mTvMessage = (TextView) findViewById(R.id.tv_window_message);
        mBtnOK = (Button) findViewById(R.id.bt_ok);
        mBtnOK.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.e("click","ok");
        WindowManager windowManager = (WindowManager) mContext.getApplicationContext().
                getSystemService(Application.WINDOW_SERVICE);
        windowManager.removeView(this);
    }

    public void setMessage(String message){
        mTvMessage.setText(message);
    }
}
