package www.weimu.io.silentupdatedemo;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pmm.silentupdate.core.UpdateInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import com.pmm.silentupdate.SilentUpdate;

import java.util.HashMap;

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
        getLatestApk();
//        Disposable d = new RxPermissions(this)
//                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                .subscribe(new Consumer<Boolean>() {
//                    @Override
//                    public void accept(Boolean granted) throws Exception {
//                        if (granted) {
//                            getLatestApk();
//                        }
//                    }
//                });
    }


    //获取下载链接 step2
    public void getLatestApk() {
        //具体的网络请求步骤自己操作
        final String apkUrl = "https://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk";
        //判断版本号
        final String latestVersion = "1.2.1";
        String currentVersion = BuildConfig.VERSION_NAME;

        //将服务器传给你的最新版本号字段给latestVersion
        if (latestVersion.compareTo(currentVersion) > 0) {
            Toast.makeText(JavaDemoActivity.this, "开始下载中...", Toast.LENGTH_SHORT).show();
            SilentUpdate.INSTANCE.update(new Function1<UpdateInfo, Unit>() {
                @Override
                public Unit invoke(UpdateInfo updateInfo) {
                    updateInfo.setApkUrl(apkUrl);
                    updateInfo.setLatestVersion(latestVersion);
                    updateInfo.setTitle("这是自定义的标题");
                    updateInfo.setMsg("这是自定义的内容");
                    updateInfo.setForce(false);
                    updateInfo.setExtra(new HashMap<String, Object>());
                    return Unit.INSTANCE;
                }
            });
        }
    }


}
