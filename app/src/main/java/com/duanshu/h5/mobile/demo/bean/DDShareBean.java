package com.duanshu.h5.mobile.demo.bean;

import java.io.Serializable;

/**
 * Created by conanaiflj on 2018/4/12.
 */

public class DDShareBean implements Serializable{
    //标题
    public String title;
    //内容
    public String content;
    //图片
    public String picurl;
    //链接
    public String url;
    //显示分享按钮
    public String showShareButton;
    //更新分享数据
    public String updateShareData;

    public DDShareBean(String title, String content, String picurl, String url, String showShareButton, String updateShareData) {
        this.title = title;
        this.content = content;
        this.picurl = picurl;
        this.url = url;
        this.showShareButton = showShareButton;
        this.updateShareData = updateShareData;
    }
}
