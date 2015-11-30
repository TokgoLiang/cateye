package com.konka.cateye.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by Morning on 2015-11-10.
 */
public class RealTimeRecord extends BmobObject {

    private Television televisionId;
    private String information;
    private boolean isRequest;
    private String imageName;

    public Television getTelevisionId() {
        return televisionId;
    }

    public void setTelevisionId(Television televisionId) {
        this.televisionId = televisionId;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public boolean getIsRequest() {
        return isRequest;
    }

    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }
}
