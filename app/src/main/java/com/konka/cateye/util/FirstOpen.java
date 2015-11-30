package com.konka.cateye.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.konka.cateye.StaticFinal;
import com.konka.cateye.bean.RealTimeMessage;
import com.konka.cateye.bean.RealTimeRecord;
import com.konka.cateye.bean.Television;
import com.konka.cateye.bean.User;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * 处理第一次启动相关操作
 */
public class FirstOpen {

    /**
     * 判断是否第一次启动
     * 从数据库判断
     */
    public static void isFirstOpen(final Context context, final Handler handler) {
        final String mac = getLocalMacAddressFromIp();
        BmobQuery<Television> query = new BmobQuery<>();
        query.addWhereEqualTo("mac", mac);
        query.findObjects(context, new FindListener<Television>() {
            @Override
            public void onSuccess(List<Television> list) {
                if (0 == list.size()) {
                    //第一次启动
                    Television television = new Television();
                    television.setMac(mac);
                    User user = new User();
                    television.setUserId(user);
                    television.setTvName("");
                    television.save(context, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            getTelevision(context, mac, handler);
                            Log.d("TAG", "创建TV成功");
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.d("TAG", "创建TV失败");
                        }
                    });
                } else if (list.get(0).getUserId().getObjectId().equals("null")) {
                    //未绑定
                    Log.d("TAG", list.get(0).getUserId()+"未绑定");
                    Message msg = new Message();
                    msg.what = StaticFinal.UNBOUNDED;
                    handler.sendMessage(msg);
                } else {
                    Log.d("TAG", list.get(0).getUserId().getObjectId()+"已绑定");
                    //已绑定
                    Message msg = new Message();
                    msg.what = StaticFinal.BOUNDED;
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    /**
     * 获取到电视对象，然后创建实时表
     */
    private static void getTelevision(final Context context, String mac, final Handler handler) {
        BmobQuery<Television> query = new BmobQuery<>();
        query.addWhereEqualTo("mac", mac);
        query.findObjects(context, new FindListener<Television>() {
            @Override
            public void onSuccess(List<Television> list) {
                Television television = list.get(0);
                initrealtimetable(context, television, handler);
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    /**
     * 向RealTimeReco表和RealTimeMessage表中添加一条记录
     * 将televisionId设置为该设备
     *
     * @param context 上下文
     */
    public static void initrealtimetable(final Context context, final Television television, final Handler handler) {
        RealTimeRecord realTimeRecord = new RealTimeRecord();
        realTimeRecord.setTelevisionId(television);
        realTimeRecord.setIsRequest(false);
        realTimeRecord.save(context, new SaveListener() {
            @Override
            public void onSuccess() {
                RealTimeMessage realTimeMessage = new RealTimeMessage();
                realTimeMessage.setTelevisionId(television);
                realTimeMessage.setIsRequest(false);
                realTimeMessage.save(context, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        Message msg = new Message();
                        msg.what = StaticFinal.UNBOUNDED;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(int i, String s) {

                    }
                });
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }


    /**
     * 根据IP获取本地Mac地址
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
        return mac_s;
    }

    public static String byte2hex(byte[] b) {
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
     * 获取本地IP地址，并格式化
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 初始化操作，包括：
     * 生成二维码，上传mac地址到数据库，
     * 以及向RealTimeReco表和RealTimeMessage表中添加一条记录
     * 在数据中查询该设备对应Television中的记录，获取ObjectId并保存到本地
     */
    public static Bitmap createQrCode() {

        //获取物理地址
        final String macAddress = getLocalMacAddressFromIp();
        //生成二维码，并保存到本地
        Bitmap bitmap = null;
        try {
            bitmap = EncodingHandler.createQRCode(macAddress, 350);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}

