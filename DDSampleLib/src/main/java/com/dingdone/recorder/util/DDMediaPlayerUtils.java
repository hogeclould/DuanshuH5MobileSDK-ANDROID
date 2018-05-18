package com.dingdone.recorder.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by chenwenchao on 2017/8/10
 * <p>
 * 对系统的MediaPlay进行简单的封装
 */
public class DDMediaPlayerUtils {

    private static MediaPlayer mediaPlayer;

    private static AudioManager am;

    private static WeakReference<MediaPlayer.OnCompletionListener> sOnCompletionListenerWeakReference;

    private static WeakReference<MediaPlayer.OnErrorListener> sOnErrorListenerWeakReference;


    //播放前需要初始化
    private static void init(Context context) {
        if (context == null) {
            throw new RuntimeException("MediaPlayerUtil context can not be null");
        }
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    public static void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        sOnCompletionListenerWeakReference = new WeakReference<>(listener);
    }

    public static void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        sOnErrorListenerWeakReference = new WeakReference<>(listener);
    }


    private static void setListener() {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // Request audio focus for playback
                int result = am.requestAudioFocus(afChangeListener,
                        // Use the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mediaPlayer.start();
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (sOnCompletionListenerWeakReference != null
                        && sOnCompletionListenerWeakReference.get() != null) {
                    sOnCompletionListenerWeakReference.get().onCompletion(mp);
                }
//                handler.removeCallbacks(runnable);
                release();
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (sOnErrorListenerWeakReference != null
                        && sOnErrorListenerWeakReference.get() != null) {
                    sOnErrorListenerWeakReference.get().onError(mp, what, extra);
                }
//                handler.removeCallbacks(runnable);
                return false;
            }
        });

//        if(progressChangeListener !=null){
//            handler.removeCallbacks(runnable);
//            handler.post(runnable);
//        }
    }

    //播放音频
    public static void play(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mediaPlayer == null) {
            init(context);
            setListener();
            try {
                mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
                if (url.startsWith("http")) {
                    mediaPlayer.setDataSource(url);
                } else {
                    File file = new File(url);
                    if (!file.exists()) {
//                    DDToast.showToast("播放文件不存在");
                        stop();
                        return;
                    }
                    FileInputStream fis = new FileInputStream(file);
                    mediaPlayer.setDataSource(fis.getFD());
                }
                mediaPlayer.prepareAsync();//异步准备资源,避免大资源未加载完成就开始播放
            } catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
                e.printStackTrace();
                mediaPlayer.reset();
            }
        } else {
            if (mediaPlayer.isPlaying()) {
                return;
            } else if (mediaPlayer.getCurrentPosition() > 0) {
                //继续播放
//                if(progressChangeListener !=null){
//                    handler.removeCallbacks(runnable);
//                    handler.post(runnable);
//                }
                mediaPlayer.start();
            }
        }
    }

//    private static Handler handler = new Handler();
//    private static Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            if(progressChangeListener !=null && mediaPlayer != null ){
//                progressChangeListener.progressChange(getCurrentPosition(),getDuration());
//                handler.removeCallbacks(runnable);
//                handler.postDelayed(runnable, 1000);
//            }
//        }
//    };


    private static String getCurrentPosition() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int position = mediaPlayer.getCurrentPosition();
            String positionStr = generateTime(position);
            return positionStr;
        } else {
            return "";
        }
    }

    private static String getDuration() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int duration = mediaPlayer.getDuration();
            String durationStr = generateTime(duration);
            return durationStr;
        } else {
            return "";
        }
    }

    private static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public static void pauseVoice() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
//                handler.removeCallbacks(runnable);
                mediaPlayer.pause();
            }
        }
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        release();
    }

    //播放完毕需要释放MediaPlayer的资源
    private static void release() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (am != null) {
            am.abandonAudioFocus(afChangeListener);
            am = null;
        }
    }

    static AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                am.abandonAudioFocus(afChangeListener);
                // Stop playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
        }
    };
}
