## 短书Android接入文档

### 1. 添加aar依赖

根据需要选择自己需要的版本

- x5Webview(dingdone:duanshux5sdk:1.0.20)
- 系统Webview(dingdone:duanshusdk:1.0.20)

##### Add this in your root build.gradle file (not your module build.gradle file):

   ```java
   allprojects {
     repositories {
        maven { url 'http://andriod-sdk.ddapp.com/nexus/content/groups/public/' }
       }
   }
   ```

   ​

##### Then, add the library to your module build.gradle

   ```json
 dependencies {
  implementation'com.duanshu.h5.mobile:duanshusdk:duanshusdk:1.0.20'
 }
   ```

##### Then,Sync Project

### 2. 初始化DuanshuSDK

1. 在manifest文件的application节点下添加
   ```java
   <meta-data android:name="com.duanshu.h5.mobile.APP_ID" android:value="申请的appID"></meta-data>
   ```
   ```java
   <meta-data android:name="com.duanshu.h5.mobile.APP_SECRET" android:value="申请的appSecret"></meta-data>
   ```
   在你的application的onCreate方法中添加duanshu的初始化代码
      ```
   DuanshuSdk.init(this);
      ```

2. 你也可以在application的onCreate方法中直接调用以下方法初始化

   ```java
   DuanshuSdk.init(this,"申请的appID","申请的appSecret");
   ```



### 3. 代码实现

初始化webview

```
DDWebView webView = new DDWebView(context);
```

实现DuanshuAPIInterface的接口类中的方法

```java
public interface DuanshuAPIInterface {
    /**
     * 获取用户信息
     * @param data 可选
     * @param callBackFunction 必选
     * 返回数据结构:
     *   {"code":0,"msg":"success","data":{"userName":"用户名","userId":"用户id","avatarUrl":"用户头像链接","telephone":"绑定手机号"}}
     */
    void getUserInfo(Map<String, Object> data, CallBackFunction callBackFunction);
    /**
     * 预览多图
     * @param data 必选 "position":0, // 默认从哪张图片开始预览 注意：position不得大于图片张数   "pics":预览的图片数组
     * @param callBackFunction  可选
     */
    void previewPic(Map<String, Object> data, CallBackFunction callBackFunction);
    /**
     * 选择图片
     * @param data 必选 "count":"最多选取图片张数"  "base64_enabled":1|0  1:返回64进制编码和格式 0 不返回64进制编码和格式
     * @param callBackFunction  必选
     * 返回数据结构:
     *   {"code":0,"msg":"success","data":["图片本地路径1","图片本地路径2"]}
     */
    void chooseImage(Map<String, Object> data, CallBackFunction callBackFunction);
    /**
     * 分享
     * @param data  必选 {"title":“分享标题”,"content":“分享描述”,"picurl":“分享图片链接”,"url":“分享内容链接”
     *              ,"showShareButton":"1显示分享按钮 0不显示分享按钮","updateShareData":"1数据只更新，不打开分享面板 0直接弹出分享"}
     * @param callBackFunction 可选
     * 返回数据结构:
     *   {"code":0,"msg":"success"}
     */
    void share(Map<String,Object> data, CallBackFunction callBackFunction);
    /**
     * 预览单图
     * @param data 必选 "imgUrl":"单张图片的地址"
     * @param callBackFunction 可选
     */
    void previewImage(Map<String, Object> data, CallBackFunction callBackFunction);
    /**
     * 开始录音
     * @param data  可选  "base64_enabled":1|0  1:返回64进制编码和格式 0 不返回64进制编码和格式
     * @param callBack  必选
     * 返回数据结构:
     *   {"code":0,"msg":"success"}
     */
    void startRecord(Map<String, Object> data, CallBackFunction callBack);
    /**
     * 停止录音
     * @param data  可选
     * @param callBack  必选
     * 返回数据结构:
     *   {"code":0,"msg":"success","data":{"localPath":"录音文件的本地暂存文件路径"}}
     */
    void stopRecord(Map<String, Object> data, CallBackFunction callBack);
    /**
     * 播放音频
     * @param data 必选 {"record_url":"http://xxx.mp3"}
     * @param callBack 必选
     * 返回数据结构:
     *    {"code":0,"msg":"success"}
     */
    void playVoice(Map<String, Object> data, CallBackFunction callBack);
    /**
     * 暂停播放
     * @param data 可选
     * @param callBack 必选
     * 返回数据结构:        
     *    {"code":0,"msg":"success"}
     */
    void pauseVoice(Map<String, Object> data, CallBackFunction callBack);
    /**
     * 停止播放
     * @param data 可选
     * @param callBack 必选
     * 返回数据结构:   
     *    {"code":0,"msg":"success"}
     */
    void stopVoice(Map<String, Object> data, CallBackFunction callBack);
    /**
     * 加载url
     * @param data 必选
     * 打开外链 {"url": "http://www.baidu.com"}
     * 拨打电话 {"url": "dingdone://tel?phone_number=10086"}
     * @param callBack 必选
     * 返回数据结构:       
     *    {"code":0,"msg":"success"}
     */
    void loadUrl(Map<String, Object> data, CallBackFunction callBack);
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

### 4. 混淆设置

- 底层是普通webview的aar混淆

```java
#duanshusdk
-keep class com.duanshu.h5.mobile.bean.**{*;}
# 避免混淆泛型，这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature
#------------------  下方是retrofit，这里不要动------
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

#------------------  下方是retrofit，这里不要动------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

#------------------  下方是okio，这里不要动------
-dontwarn okio.**
```

- 底层是x5webview的aar

  因为aar本身做了一定的混淆，内部包含了x5所需的混淆策略。但是项目中还需添加以下混淆:

  ```java
  #x5需要(必选)
  -dontwarn dalvik.**
  #duanshusdk
  -keep class com.duanshu.h5.mobile.bean.**{*;}
  # 避免混淆泛型，这在JSON实体映射时非常重要，比如fastJson
  -keepattributes Signature
  #------------------  下方是retrofit，这里不要动------
  -keepclassmembernames,allowobfuscation interface * {
      @retrofit2.http.* <methods>;
  }
  # Ignore annotation used for build tooling.
  -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

  #------------------  下方是retrofit，这里不要动------
  -dontwarn okhttp3.**
  -dontwarn okio.**
  -dontwarn javax.annotation.**
  -dontwarn org.conscrypt.**
  # A resource is loaded with a relative path so the package of this class must be preserved.
  -keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

  #------------------  下方是okio，这里不要动------
  -dontwarn okio.**
  ```

  ​

### 5. 注意事项

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
    DDPageCallBackManager.getInstance().callBack(Integer key, String returnData);
    }
```

