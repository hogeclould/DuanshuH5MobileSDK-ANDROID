## 短书Android接入文档

### 1.添加aar

根据需要选择自己需要的版本，底层分别为x5Webview和系统Webview。下载并引入DuanshuH5MobileSDK-v1.0.1.1_r54d1ke32.aar

### 2.添加权限

```
<uses-permission android:name="android.permission.INTERNET"/>
```

### 3.代码实现

初始化webview

```
DDWebView webView = new DDWebView(context);
```

实现DuanshuAPIInterface的接口类中的方法

```java
public interface DuanshuAPIInterface {
    //获取用户信息
    void getUserInfo(CallBackFunction callBackFunction);
    //预览多图
    void previewPic(Map<String, Object> data);
    //选择图片
    void chooseImage(Map<String, Object> data, CallBackFunction callBackFunction);
    //分享
    void share(Map<String,Object> data, CallBackFunction callBackFunction);
    //预览单图
    void previewImage(Map<String, Object> data);
    //开始录音
    void startRecord(CallBackFunction callBack);
    //停止录音
    void stopRecord(CallBackFunction callBack);
    //播放音频
    void playVoice(Map<String, Object> data, CallBackFunction callBack);
    //暂停播放
    void pauseVoice();
    //停止播放
    void stopVoice();
}

```

将DuanshuAPIInterface的实现类引用传递给DuanshuSdk

```java
DuanshuSdk.setDDAPIInterface(duanshuAPIInterfaceImp);
```

加载包含duanshuSDK的js方法的网页地址即可

```java
webView.loadUrl("http://duanshu_demo_Sdk.html");
```

### 4.混淆设置

- 底层是普通webview的aar混淆

```java
#duanshusdk
-keep class com.duanshu.h5.mobile.bean.**{*;}
```

- 底层是x5webview的aar

  因为aar本身做了一定的混淆，内部包含了x5所需的混淆策略。但是项目中还需添加以下混淆:

  ```java
  #x5需要(必选)
  -dontwarn dalvik.**
  #duanshusdk
  -keep class com.duanshu.h5.mobile.bean.**{*;}
  ```

  ​

### 5.注意事项

```java
void chooseImage(Map<String, Object> data, CallBackFunction callBackFunction);
```

1.选择照片方法重写不同于其他方法的方法体内回掉，是跨页面的回掉。可以在DuanshuAPIInterface子类-重写方法chooseImage中添加以下方法

```java
DDPageCallBackManager.getInstance().addMap(Integer key,CallBackFunction callBackFunction);
```

2.在页面回掉方法中调用

```java
 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    DDPageCallBackManager.getInstance().callBack(Integer key,         String returnData);
    }
```

