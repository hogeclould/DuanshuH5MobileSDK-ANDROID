package com.duanshu.h5.mobile.demo.bean;

import java.io.Serializable;

/**
 * Created by conanaiflj on 2018/4/3.
 */

public class DDFileBean implements Serializable{
    //本地路径
    public String localPath;
    //文件类型
    public String type;
    //base64字节
    public String base64;

    public DDFileBean(String localPath, String type, String base64) {
        this.localPath = localPath;
        this.type = type;
        this.base64 = base64;
    }
}
