package com.dingdone.recorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by chenwenchao on 2017/8/10
 *
 * 对Mp3Recorder进行多一次封装，增加录音取消相应的逻辑
 */
public class LameMp3Manager implements Mp3Recorder.OnFinishListener {

    private static final String TAG = LameMp3Manager.class.getSimpleName();
    private Mp3Recorder mp3Recorder;
    private boolean cancel = false;
    private boolean stop = false;
    private MediaRecorderListener mediaRecorderListener;

    private static class LameMp3ManagerHolder {
        /**
         * 单例对象实例
         */
        static final LameMp3Manager INSTANCE = new LameMp3Manager();
    }

    public static LameMp3Manager getInstance() {
        return LameMp3ManagerHolder.INSTANCE;
    }

    /**
     * private的构造函数用于避免外界直接使用new来实例化对象
     */
    private LameMp3Manager(){
        init();
    }

    private void init(){
        mp3Recorder = new Mp3Recorder();
        mp3Recorder.setFinishListener(this);
    }

    public void setMediaRecorderListener(MediaRecorderListener listener){
        mediaRecorderListener = listener;
    }

    public void startRecorder(File saveMp3FullName){
        cancel = stop = false;
        try {
//            mp3Recorder.startRecording(createMp3SaveFile(saveMp3FullName));
            if (mp3Recorder == null) {
                init();
            }
            mp3Recorder.startRecording(saveMp3FullName);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void cancelRecorder(){
        try {
            if (mp3Recorder != null) {
                mp3Recorder.stopRecording();
            }
            cancel = true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void stopRecorder(){
        try {
            if (mp3Recorder != null) {
                mp3Recorder.stopRecording();
            }
            stop = true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

//    private File createMp3SaveFile(String saveMp3FullName){
//        File mp3 = new File(saveMp3FullName);
//        DDLog.e(TAG,"create mp3 file for the recorder");
//        return mp3;
//    }

    public int getVolume(){
        if (mp3Recorder != null) {
            return mp3Recorder.getVolume();
        }
        return 0;
    }

    @Override
    public void onFinish(String mp3FilePath) {
        if(cancel){
            //录音取消的话，将之前的录音数据清掉
            File mp3 = new File(mp3FilePath);
            if(mp3.exists()){
                mp3.delete();
            }
            cancel = false;
        }else if(stop){
            stop = false;
            if(mediaRecorderListener != null){
                mediaRecorderListener.onRecorderFinish(mp3FilePath);
            }
        }
    }

    public interface MediaRecorderListener {
        void onRecorderFinish(String path);
    }

}
