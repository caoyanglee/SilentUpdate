package www.weimu.io.silentupdatedemo;

import android.Manifest;
import android.icu.lang.UProperty;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import io.reactivex.functions.Consumer;
import www.weimu.io.silentupdate.DownloadListener;
import www.weimu.io.silentupdate.UpdateCenter;

/**
 * Java的调用方式
 */
public class JavaDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_demo);
        checkPermission();

    }

    //检查权限 step1
    private void checkPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) {
                            getLatestApk();
                        }
                    }
                });
    }


    //获取下载链接 step2
    public void getLatestApk() {
        //具体的网络请求步骤自己操作
        String apkUrl = "https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk";
        //判断版本号
        String latestVersion = "1.1.0";
        String currentVersion = BuildConfig.VERSION_NAME;

        //将服务器传给你的最新版本号字段给latestVersion
        if (latestVersion.compareTo(currentVersion) > 0) {
            Toast.makeText(JavaDemoActivity.this, "开始下载中...", Toast.LENGTH_SHORT).show();
            UpdateCenter.INSTANCE.update(apkUrl, latestVersion);
        }
    }


}
