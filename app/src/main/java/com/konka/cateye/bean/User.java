package com.konka.cateye.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by Tokgo on 2015-11-10.
 */
public class User extends BmobUser {

    private String realPassword;

    public String getRealPassword() {
        return realPassword;
    }

    public void setRealPassword(String realPassword) {
        this.realPassword = realPassword;
    }
}
