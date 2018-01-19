## 准备工作 【Java】
1.获取依赖

```gradle
compile 'www.weimu.io:silent-update-lib:0.1.1'
```

2.增加权限

```xml
<uses-permission android:name="android.permission.INTERNET" /><!--联网权限-->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /><!--存储权限-->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /><!--存储权限-->
<uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" /><!--Notification权限-->

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
UpdateCenter.INSTANCE.attach(this);
```

5.在应用退出时

```java
UpdateCenter.INSTANCE.detach();
```


## 用法
> 注意：<br>
apkUrl即服务器提供的apk下载地址<br>
latestVersion即服务器返回客户端的最新版本号

```java
UpdateCenter.INSTANCE.update(apkUrl, latestVersion);
```

## 自定义配置
1.开关显示自带Notification和dialog<br>
> 注意：有的同学可能不喜欢自带的Notification和Dialog，可以将其关闭

```java
UpdateCenter.INSTANCE.setShowDialog(false);
UpdateCenter.INSTANCE.setShowNotification(false);
```

2.实现回调<br>
> 注意：如果要使用自己的Dialog或Notifigation,得实现回调
* 执行下载任务之前都会判断更新文件是否已经存在，若已存在,调用onFileIsExist(file:File)，不在进行下载操作<br>
* 普通下载完成则调用onDownLoadSuccess(file:file)

```java
 UpdateCenter.INSTANCE.setDownloadListener(new DownloadListener() {
     @Override
     public void onDownLoadSuccess(@NotNull File file) {
         //下载完成
     }

     @Override
     public void onFileIsExist(@NotNull File file) {
         //文件已存在
     }
 });
```
