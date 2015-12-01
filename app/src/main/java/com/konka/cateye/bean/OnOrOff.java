package com.konka.cateye.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by Morning on 2015-11-30.
 */
public class OnOrOff extends BmobObject{
    private Television televisionId;
    private boolean state;
    private boolean isRequest;

    public Television getTelevisionId() {
        return televisionId;
    }

    public void setTelevisionId(Television televisionId) {
        this.televisionId = televisionId;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean getIsRequest() {
        return isRequest;
    }

    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }
}
