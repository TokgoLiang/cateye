package com.konka.cateye.bean;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

/**
 * Created by Tokgo on 2015-11-10.
 */
public class Television extends BmobObject {

    private String mac;
    private ArrayList<MonitorTime> monitorTime;
    private User userId;
    private String tvName;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public ArrayList<MonitorTime> getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(ArrayList<MonitorTime> monitorTime) {
        this.monitorTime = monitorTime;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public String getTvName() {
        return tvName;
    }

    public void setTvName(String tvName) {
        this.tvName = tvName;
    }
}
