package com.konka.cateye.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by Morning on 2015-11-10.
 */
public class HistoryRecord extends BmobObject {

    private Television televisionId;
    private String information;
    private String imageName;
    private String iconName;

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

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
}
