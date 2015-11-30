package com.konka.cateye.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by Morning on 2015-11-10.
 */
public class RealTimeMessage extends BmobObject {

    private Television televisionId;
    private String message;
    private boolean isRequest;

    public Television getTelevisionId() {
        return televisionId;
    }

    public void setTelevisionId(Television televisionId) {
        this.televisionId = televisionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIsRequest() {
        return isRequest;
    }

    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }
}
