## 在Application中进行初始化

```java
SilentUpdate.INSTANCE.init(this);
```


## 用法
> 注意：<br>
apkUrl：服务器提供的apk下载地址<br>
latestVersion：服务器返回客户端的最新版本号<br>
title:提示的标题<br>
msg:提示的内容<br>
isForce:是否强制<br>
extra:通过extra携带更多数据<br>

```java
SilentUpdate.INSTANCE.update(new Function1<UpdateInfo, Unit>() {
    @Override
    public Unit invoke(UpdateInfo updateInfo) {
        updateInfo.setApkUrl(apkUrl);
        updateInfo.setLatestVersion(latestVersion);
        updateInfo.setTitle("这是自定义的标题");
        updateInfo.setMsg("这是自定义的内容");
        updateInfo.setForce(false);
        updateInfo.setExtra(new Bundle());
        return Unit.INSTANCE;
    }
});
```

## 自定义配置
请参考Kotlin版的自定义配置
