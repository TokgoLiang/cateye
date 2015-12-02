package com.konka.cateye.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.konka.cateye.R;
import com.konka.cateye.bean.MonitorTime;

import java.util.List;


/**
 * Created by Tokgo on 2015-11-13.
 * 监控时间段列表适配器
 */
public class MonitorTimesAdapter extends BaseAdapter {

    public List<MonitorTime> monitorTimes;
    public Context context;

    public MonitorTimesAdapter(List<MonitorTime> monitorTimes, Context context) {
        this.monitorTimes = monitorTimes;
        this.context = context;
    }

    @Override
    public int getCount() {
        return monitorTimes.size();
    }

    @Override
    public Object getItem(int position) {
        return monitorTimes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            view = LinearLayout.inflate(context, R.layout.monitortime,null);
        } else {
            view = convertView;
        }
        MonitorTime monitorTime = monitorTimes.get(position);
        TextView beginHour = (TextView) view.findViewById(R.id.tv_beginTime_hour);
        if(monitorTime.getBeginHour() < 10) {
            beginHour.setText("0"+monitorTime.getBeginHour());
        }else {
            beginHour.setText(""+monitorTime.getBeginHour());
        }

        TextView beginMinute = (TextView) view.findViewById(R.id.tv_beginTime_minute);
        if(monitorTime.getBeginMinute() < 10) {
            beginMinute.setText("0"+monitorTime.getBeginMinute());
        }else {
            beginMinute.setText(""+monitorTime.getBeginMinute());
        }

        TextView endHour = (TextView) view.findViewById(R.id.tv_endTime_hour);
        if(monitorTime.getEndHour() < 10) {
            endHour.setText("0"+monitorTime.getEndHour());
        }else {
            endHour.setText(""+monitorTime.getEndHour());
        }

        TextView endMinute = (TextView) view.findViewById(R.id.tv_endTime_minute);
        if(monitorTime.getEndMinute() < 10) {
            endMinute.setText("0"+monitorTime.getEndMinute());
        }else {
            endMinute.setText(""+monitorTime.getEndMinute());
        }

        return view;
    }
}

