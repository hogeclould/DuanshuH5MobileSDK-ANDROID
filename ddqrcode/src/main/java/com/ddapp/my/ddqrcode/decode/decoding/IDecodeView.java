package com.ddapp.my.ddqrcode.decode.decoding;

import android.graphics.Bitmap;
import android.os.Handler;

import com.ddapp.my.ddqrcode.decode.view.ViewfinderView;
import com.google.zxing.Result;

/**
 * Created by tanqiang on 2017/9/26.
 */

public interface IDecodeView {
    ViewfinderView getViewfinderView();
    void handleDecode(Result obj, Bitmap barcode);
    void drawViewfinder();
    Handler getHandler();
}
