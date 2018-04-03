package com.duanshu.h5.mobile.demo.callback;

import com.duanshu.h5.mobile.demo.bean.DDFileBean;

import java.util.List;

/**
 * Created by conanaiflj on 2018/4/3.
 */

public interface MultiFileBytesCallback {
    void fileBytes(List<DDFileBean> base64s);
}
