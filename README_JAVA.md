## 准备工作 【Java】
1.获取依赖

**project的build.gradle**

```
allprojects {
    repositories {
        ......        
        maven { url  "https://dl.bintray.com/yongdongji/android" }
    }
}
```
**app的build.gradle**
[ ![Download](https://api.bintray.com/packages/yongdongji/android/silentupdate/images/download.svg) ](https://bintray.com/yongdongji/android/silentupdate/_latestVersion)

> 注意：默认使用kotlin1.2.20版本的库

```gradle
compile "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.2.20"
compile 'www.weimu.io:silentupdate:{version_code}@aar'
```

2.增加权限

```xml
<!-- 联网权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- 存储权限 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<!-- 通知权限 -->
<uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />

```       
3.增加FileProvider【适配7.0】

> 注意：此处的```android:resource="@xml/filepaths"```自己谷歌或直接获取demo文件

```xml
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/filepaths" />
</provider>
```


4.在Application中进行初始化

```java
UpdateCenter.INSTANCE.init(this);
```


## 用法
> 注意：<br>
apkUrl：服务器提供的apk下载地址<br>
latestVersion：服务器返回客户端的最新版本号

```java
UpdateCenter.INSTANCE.update(apkUrl, latestVersion);
```

## 自定义配置
1.开关显示自带Notification和dialog<br>

```java
UpdateCenter.INSTANCE.setUseDefaultHint(false);//是否使用默认提示 包括Dialog和Notification
```

2.实现回调<br>
> 注意：默认情况下，【下载完成】或【文件已存在】都会有默认的Notification和Dialog提示。<br>
若想自定义提示，请实现以下接口并配合【自定义配置】的第一步

* 执行下载任务之前都会判断更新文件是否存在，**存在**：调用`onFileIsExist(file: File)`，不再进行下载操作
* 下载完成则调用`onDownLoadSuccess(file: File)`

```java
UpdateCenter.INSTANCE.setDownloadListener(new DownloadListener() {
     @Override
     public void onDownLoadSuccess(@NotNull File file) {
         //下载完成
         SilentUpdate.INSTANCE.openApkInstallPage(file);//当取消默认的dialog时
     }

     @Override
     public void onFileIsExist(@NotNull File file) {
         //文件已存在
         SilentUpdate.INSTANCE.openApkInstallPage(file);//当取消默认的dialog时
     }
});
```
