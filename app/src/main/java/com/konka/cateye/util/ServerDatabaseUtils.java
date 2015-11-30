package com.konka.cateye.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bmob.BmobProFile;
import com.bmob.btp.callback.UploadListener;
import com.konka.android.tv.KKCommonManager;
import com.konka.android.tv.common.KKTVCamera;
import com.konka.cateye.StaticFinal;
import com.konka.cateye.service.AutoMonitorService;
import com.konka.cateye.bean.HistoryRecord;
import com.konka.cateye.bean.RealTimeMessage;
import com.konka.cateye.bean.RealTimeRecord;
import com.konka.cateye.bean.Television;

import java.io.File;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * Created by Morning on 2015-11-16.
 */
public class ServerDatabaseUtils {
    private static final int IMAGE_WIDTH = 480;
    private static final int IMAGE_HEIGHT = 270;

    private Context mContext = null;
    private AutoMonitorService.ServiceHandler mHandler = null;
    private SystemDataUtils mSystemDataUtils = null;


    public ServerDatabaseUtils(Context mContext, AutoMonitorService.ServiceHandler mHandler) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.mSystemDataUtils = new SystemDataUtils(mContext);

    }

    /**
     * 更新电视表，其实也就是监控时间而已
     * @param television
     */
    public void updateTelevision(final Television television, final Handler handler) {
        television.update(mContext, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.e("cateye", "update time success");
                MonitorTimeUtils.getMonitorTimes(mContext, handler, StaticFinal.SHOW_MONITOR_TIMES, television.getObjectId());
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e("cateye", "update time failed:" + s);
                Message message = new Message();
                message.what = AutoMonitorService.ServiceHandler.UPDATE_TELEVISION_FAILURE;
                mHandler.sendMessage(message);
            }
        });
    }

    /**
     * 截图并上传至即时内容监控记录表中
     * @param realTimeRecord
     */
    public void screenShot(final RealTimeRecord realTimeRecord) {
        KKCommonManager.getInstance(mContext).takePictureofTV(IMAGE_WIDTH, IMAGE_HEIGHT, new KKTVCamera.TakePictureCallback() {
            @Override
            public void onPictureTaken(Bitmap bitmap) {
                if (bitmap == null) Log.e("cateye", "screenShot is null");
                String imagePath = mSystemDataUtils.saveScreenShot(bitmap);
                uploadScreenShot(realTimeRecord, imagePath);
            }
        }, KKCommonManager.EN_KK_CAPTURE_MODE.CURRENT_ALL);
    }

    /**
     * 上传图片并更新记录表
     * @param realTimeRecord
     * @param imagePath
     */
    private void uploadScreenShot(final RealTimeRecord realTimeRecord, String imagePath) {
        BmobProFile.getInstance(mContext).upload(imagePath,
                new UploadListener() {
                    @Override
                    public void onSuccess(String s, String s1, BmobFile bmobFile) {
                        BmobProFile.mSocket.close();
                        Log.e("cateye", "real time upload success:" + s);
                        realTimeRecord.setImageName(s);
                        realTimeRecord.setInformation(mSystemDataUtils.getAppName());
                        updateRealTimeRecord(realTimeRecord);
                    }

                    @Override
                    public void onProgress(int i) {
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.e("cateye", "real time upload failed:" + i + s);
                        BmobProFile.mSocket.close();
                    }
                });
    }

    /**
     * 更新记录表
     * @param realTimeRecord
     */
    private void updateRealTimeRecord(RealTimeRecord realTimeRecord) {
        realTimeRecord.update(mContext, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.e("cateye", "real time record success");
                Message message = new Message();
                message.what = AutoMonitorService.ServiceHandler.UPDATE_REAL_TIME_RECORD_SUCCESS;
                mHandler.sendMessage(message);
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e("cateye", "real time record failed:" + s);
            }
        });
    }

    /**
     * 更新消息表
     * @param realTimeMessage
     */
    public void updateRealTimeMessage(RealTimeMessage realTimeMessage) {
        realTimeMessage.update(mContext, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.e("cateye", "message update");
                Message message = new Message();
                message.what = AutoMonitorService.ServiceHandler.UPDATE_REAL_TIME_MESSAGE_SUCCESS;
                mHandler.sendMessage(message);
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e("cateye", "message failed:" + s);
            }
        });
    }

    /**
     * 与实时记录类似，这个是历史记录中的截图
     * @param historyRecord
     */
    public void screenShot(final HistoryRecord historyRecord) {
        KKCommonManager.getInstance(mContext).takePictureofTV(IMAGE_WIDTH, IMAGE_HEIGHT, new KKTVCamera.TakePictureCallback() {
            @Override
            public void onPictureTaken(Bitmap bitmap) {
                if (bitmap == null) Log.e("cateye", "screenShot is null");
                String imagePath = mSystemDataUtils.saveScreenShot(bitmap);
                uploadScreenShot(historyRecord, imagePath);
            }
        }, KKCommonManager.EN_KK_CAPTURE_MODE.CURRENT_ALL);
    }

    /**
     * 上传截图
     * @param historyRecord
     * @param imagePath
     */
    private void uploadScreenShot(final HistoryRecord historyRecord, String imagePath) {
        BmobProFile.getInstance(mContext).upload(imagePath,
                new UploadListener() {
                    @Override
                    public void onSuccess(String s, String s1, BmobFile bmobFile) {
                        BmobProFile.mSocket.close();
                        Log.e("cateye", " screenShot upload success:" + s);
                        historyRecord.setImageName(s);

                        String iconPath = mSystemDataUtils.getAppIcon();
                        uploadAppIcon(historyRecord, iconPath);
                    }

                    @Override
                    public void onProgress(int i) {
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.e("cateye", "screenShot upload failed:" + i + s);
                        BmobProFile.mSocket.close();
                    }
                });
    }

    /**
     * 上传应用图标并保存历史记录
     * @param historyRecord
     * @param iconPath
     */
    private void uploadAppIcon(final HistoryRecord historyRecord, String iconPath){
        BmobProFile.getInstance(mContext).upload(iconPath,
                new UploadListener() {
                    @Override
                    public void onSuccess(String s, String s1, BmobFile bmobFile) {
                        BmobProFile.mSocket.close();
                        Log.e("cateye", " icon upload success:" + s);
                        historyRecord.setIconName(s);
                        saveHistoryRecord(historyRecord);
                    }

                    @Override
                    public void onProgress(int i) {
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.e("cateye", "icon upload failed:" + i + s);
                        BmobProFile.mSocket.close();
                    }
                });
    }

    /**
     * 保存历史记录
     * @param historyRecord
     */
    private void saveHistoryRecord(HistoryRecord historyRecord) {
        historyRecord.save(mContext, new SaveListener() {
            @Override
            public void onSuccess() {
                Log.e("cateye", "history save success");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e("cateye", "history save failed:" + s);
            }
        });
    }
}
