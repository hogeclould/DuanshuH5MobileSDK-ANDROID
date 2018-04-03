package com.duanshu.h5.mobile.demo;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.duanshu.h5.mobile.demo.bean.DDFileBean;
import com.duanshu.h5.mobile.demo.callback.MultiFileBytesCallback;
import com.duanshu.h5.mobile.demo.callback.SingleFileBytesCallback;
import com.duanshu.h5.mobile.utils.DDStreamUtil;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by conanaiflj on 2018/4/3.
 */

public class FilesTask {
    private List<String> paths;//多文件
    private String path;//单文件
    private ExecutorService executorService;
    private MultiFileBytesCallback multiFileBytesCallback;
    private SingleFileBytesCallback singleFileBytesCallback;

    private List<DDFileBean> base64s = new ArrayList<>();


    public FilesTask(List<String> paths) {
        this.paths = paths;
        executorService = Executors.newFixedThreadPool(2);
    }

    public FilesTask(String path) {
        this.path = path;
        executorService = Executors.newSingleThreadExecutor();
    }


    public void setMultiFileBytesCallback(MultiFileBytesCallback multiFileBytesCallback) {
        this.multiFileBytesCallback = multiFileBytesCallback;
    }

    public void setSingleFileBytesCallback(SingleFileBytesCallback singleFileBytesCallback) {
        this.singleFileBytesCallback = singleFileBytesCallback;
    }

    public void executeMuti(){
        if(paths == null || paths.size()==0){
            return;
        }
        for(int i = 0;i < paths.size();i++){
            final String path = paths.get(i);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    File file = new File(path);
                    try {
                        FileInputStream in = new FileInputStream(file);
                        String stream = DDStreamUtil.readStream(in);
                        String enToStr = Base64.encodeToString(stream.getBytes(), Base64.DEFAULT);
                        String type = MimeTypeMap.getFileExtensionFromUrl(URLEncoder.encode(path,"UTF-8"));
                        DDFileBean fileBean = new DDFileBean(path, type, enToStr);
                        Log.i("test","enToStr:"+enToStr);
                        base64s.add(fileBean);
                        if(base64s.size() == paths.size()){
                            if(multiFileBytesCallback != null){
                                multiFileBytesCallback.fileBytes(base64s);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
    }



    public void executeSingle(){
        if(!TextUtils.isEmpty(path)){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    File file = new File(path);
                    try {
                        FileInputStream in = new FileInputStream(file);
                        String stream = DDStreamUtil.readStream(in);
                        String enToStr = Base64.encodeToString(stream.getBytes(), Base64.DEFAULT);
                        String type = MimeTypeMap.getFileExtensionFromUrl(URLEncoder.encode(path, "UTF-8"));
                        DDFileBean fileBean = new DDFileBean(path, type, enToStr);
                        Log.i("test", "enToStr:" + enToStr);
                        if (singleFileBytesCallback != null) {
                            singleFileBytesCallback.fileBytes(fileBean);
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
    }
}
