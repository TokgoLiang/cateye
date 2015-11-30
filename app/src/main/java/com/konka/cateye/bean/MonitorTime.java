package com.konka.cateye.bean;

/**
 * Created by Morning on 2015-11-12.
 * 监控时间段类
 */
public class MonitorTime {
    private int beginHour;
    private int beginMinute;
    private int endHour;
    private int endMinute;

    public MonitorTime(){}


    public MonitorTime(int beginHour, int beginMinute, int endHour, int endMinute) {
        this.beginHour = beginHour;
        this.beginMinute = beginMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    public int getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(int beginHour) {
        this.beginHour = beginHour;
    }

    public int getBeginMinute() {
        return beginMinute;
    }

    public void setBeginMinute(int beginMinute) {
        this.beginMinute = beginMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    /**
     * 将开始时间转换成数字用于比较
     * */
    public int getBeginTime() {
        return beginHour * 100 + beginMinute;
    }

    /**
     * 将结束时间转换成数字用于比较
     * */
    public int getEndTime() {
        return endHour * 100 + endMinute;
    }

    /**
     * 将监控时间段转换成字符串，用于显示
     * */
    public String toString() {
        return beginHour+" : "+beginMinute+"  ------------  "+endHour+" : "+endMinute;
    }

}
