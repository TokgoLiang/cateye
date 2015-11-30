package com.konka.cateye;

/**
 * Created by Tokgo on 2015-11-23.
 * 用于存放程序用到的所有常量
 */
public class StaticFinal {

    //用户绑定TV相关常量
    public static final int UNBOUNDED = 0;                                                          //尚未绑定（状态）
    public static final int BOUNDED = 1;                                                            //已经绑定（状态）
    public static final int BOUND = 2;                                                              //绑定（动作）
    public static final int UNBOUNDING = 3;                                                         //解除绑定（动作）

    public static final int INTERNET_ERROR = 12;                                                    //网络异常

    public static final int LEADDING_TIME = 15;                                                     //引导等待时间

    //设置监控时间相关常量
    public static final int ADD_MONITOR_TIMES = 5;                                                  //添加监控时间
    public static final int DEL_MONITOR_TIMES = 6;                                                  //删除监控时间
    public static final int UPDATE_MONITOR_TIMES = 7;                                               //修改监控时间
    public static final int SHOW_MONITOR_TIMES = 8;                                                 //显示监控时间列表

    public static final int SWITCH_USER = 9;
    public static final int EXIT = 10;
    public static final int GOTO_SETTING= 11;

    //应用APPID
    public static final String APPID3 = "e60063a62dd76ed0f8346cb3eae41d71";                         //赵启亮的APPID
    public static final String APPID2 = "6bbb89920bb59a24bff31e1ea2991637";                         //陈景灏的APPID
    public static final String APPID = "a55fa39985345f74cc7b869a9878f514";
}
