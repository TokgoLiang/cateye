package com.konka.cateye.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.konka.android.tv.KKCommonManager;
import com.konka.android.tv.common.KKTVCamera;
import com.konka.cateye.R;
import com.konka.cateye.bean.MonitorTime;
import com.konka.cateye.bean.Television;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Morning on 2015-11-11.
 */
public final class SystemDataUtils {

    private Context mContext = null;

    public SystemDataUtils(Context context) {
        this.mContext = context;
    }

    public String getAppIcon(){
        PackageManager packageManager = mContext.getPackageManager();
        Bitmap icon = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getProcessName(), 0);
            //appName = (String) applicationInfo.loadLabel(packageManager);
            icon = ((BitmapDrawable)packageManager.getApplicationIcon(applicationInfo)).getBitmap();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return saveScreenShot(icon);
    }

    /**
     * 获取当前栈顶APP的名字
     * @return
     */
    public String getAppName() {
        PackageManager packageManager = mContext.getPackageManager();
        String appName = "no find";
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getProcessName(), 0);
            //appName = (String) applicationInfo.loadLabel(packageManager);
            appName = packageManager.getApplicationLabel(applicationInfo).toString();
            Log.e("cateye", "app name is:" + appName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    /**
     * 获取当前栈顶进程名
     * @return
     */
    private String getProcessName() {
        return ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE))
                .getRunningAppProcesses().get(0).processName;
    }

    /**
     * 保存bitmap格式的截图
     * @param image
     * @return
     */
    public String saveScreenShot(Bitmap image) {
        String fileName = name();
        try {
            File imageFile = new File(mContext.getCacheDir().getAbsolutePath(), fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mContext.getCacheDir().getAbsolutePath() + "/" + fileName;
    }

    /**
     * 利用当前系统时间为图片命名
     * @return
     */
    private String name() {
        return System.currentTimeMillis() + ".png";
    }

    /**
     * 获取MAC地址
     * @return
     */
    public static String getLocalMacAddressFromIp() {
        String mac_s = "";
        try {
            byte[] mac;
            NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mac_s != "") {
            StringBuilder stringBuilder = new StringBuilder(mac_s);
            for (int n = 2; n < 15; n += 3) {
                stringBuilder.insert(n, ":");
            }
            mac_s = stringBuilder.toString();
        }
        Log.e("cateye", "mac is:" + mac_s);
        return mac_s;
    }

    /**
     * 字节类型转为16进制数
     * @param b
     * @return
     */
    private static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs = hs.append("0").append(stmp);
            else {
                hs = hs.append(stmp);
            }
        }
        return String.valueOf(hs);
    }

    /**
     * 获取本地IP地址
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("cateye", "WifiPreference IpAddress:" + ex.toString());
        }
        return null;
    }

    /**
     * 判断当前时间是否在监控时间范围内
     * @param television
     * @return
     */
    public boolean isTimeNmow(Television television) {
        ArrayList<MonitorTime> monitorTimeList = television.getMonitorTime();
        if (monitorTimeList == null) {
            return true;
        }
        Calendar nowTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        int nowHour = nowTime.get(Calendar.HOUR_OF_DAY);
        int nowMinute = nowTime.get(Calendar.MINUTE);
        Log.e("cateye",monitorTimeList.toString());
        Log.e("cateye","now time is:"+nowHour+":"+nowMinute);
        for (MonitorTime monitorTime : monitorTimeList) {
            if (nowHour < monitorTime.getBeginHour() && nowHour > monitorTime.getEndHour()) {
            } else if (nowHour == monitorTime.getBeginHour() && nowMinute < monitorTime.getBeginMinute()) {
            } else if (nowHour == monitorTime.getEndHour() && nowMinute > monitorTime.getEndMinute()) {
            } else {
                return true;
            }
        }
        return false;
    }
}
