# 静默更新应用库
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://api.bintray.com/packages/yongdongji/android/silent-update-lib/images/download.svg) ](https://bintray.com/yongdongji/android/silent-update-lib/_latestVersion)

> 注意：此库是由kotlin编写而成<br>

A library silently & automatically download latest apk to update your App<br>
静默自动下载最新apk并升级应用



# 静默更新应用的步骤
1. 判断权限【使用者自己实现】
2. 获取下载链接，判断版本号【使用者自己实现】
3. 开始下载前，判断升级文件是否存在，**存在**：直接弹Dialog和回调(onFileIsExist) **不存在**：开始下载
4. 下载完成后，接收回调(onDownLoadSuccess),显示Notification和Dialog进行提示
5. **用户点击Dialog或Notification即安装，更新对于用户只有1步，简洁高效**

 
## 准备工作 
1.获取依赖

```gradle
dependencies{
    compile 'www.weimu.io:silent-update-lib:0.1.0'
}
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
kotlin
```kotlin
UpdateCenter.attach(this)
```

java
```java
UpdateCenter.INSTANCE.attach(this);
```

5.在应用退出时

kotlin
```kotlin
UpdateCenter.detach()
```

java
```java
UpdateCenter.INSTANCE.detach();
```java



## 用法
> 注意：latestVersion为字符串，将服务器传给你的latestVersion字符串传入即可

kotlin
```kotlin
UpdateCenter.obtainLatestApk(downloadUrl, latestVersion)
```

java
```java
UpdateCenter.INSTANCE.obtainLatestApk(downloadUrl, latestVersion);
```

## 自定义配置
1.开关显示自带Notification和dialog<br>
注意：有的同学可能不喜欢自带的Notification和Dialog，可以将其关闭

kotlin
```kotlin
UpdateCenter.isShowDialog = false//是否显示Dialog
UpdateCenter.isShowNotification=false//是否显示Notification
```

java
```
UpdateCenter.INSTANCE.setShowDialog(false);
UpdateCenter.INSTANCE.setShowNotification(false);
```

2.实现回调<br>
> 注意：如果要使用自己的Dialog或Notifigation,得实现回调
* 执行下载任务之前都会判断更新文件是否已经存在，若已存在,调用onFileIsExist(file:File)，不在进行下载操作<br>
* 普通下载完成则调用onDownLoadSuccess(file:file)

kotlin
```kotlin
UpdateCenter.downloadListener = object : DownloadListener {

    override fun onDownLoadSuccess(file: File) {
        //下载完成
    }

    override fun onFileIsExist(file: File) {
        //文件已存在
    }

}
```

java
```java
 UpdateCenter.INSTANCE.setDownloadListener(new DownloadListener() {
     @Override
     public void onDownLoadSuccess(@NotNull File file) {
         //下载完成
     }

     @Override
     public void onFileIsExist(@NotNull File file) {
         //文件已经
     }
 });
```
