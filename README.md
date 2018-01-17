# SilentUpdateDemo
A library silently & automatically download latest apk to update your App<br>
静默自动下载最新apk升级应用的库


> 注意：此库是由kotlin编写而成<br>

### 静默下载升级的步骤
1. 判断权限【使用者自己实现】
2. 获取下载链接，判断版本号【使用者自己实现】
3. 在后台开始自动下载
4. 下载完成后，接收回调
5. 显示Notification和Dialog进行提示

**点击即安装，方便用户更新**

### 用法
1. 增加权限


```xml
<uses-permission android:name="android.permission.INTERNET" /><!--联网权限-->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /><!--存储权限-->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /><!--存储权限-->
<uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" /><!--Notification权限-->

```
   
    
       
2. 增加FileProvider【适配7.0】


```xml
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="update.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/filepaths" />
</provider>
```

