package www.weimu.io.silentupdate.core

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.text.TextUtils
import www.weimu.io.silentupdate.UpdateCenter
import java.io.File


object FileUtils {


    //是否存在文件
    fun isFileExist(filePath: String): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }

        val file = File(filePath)
        return file.exists() && file.isFile
    }


    /**
     * 获取文件的Uri
     * 兼容7.0
     */
    fun getUriForFile(context: Context?, file: File?): Uri {
        //获取当前app的包名
        val FPAuth = "${UpdateCenter.getApplicationContext().packageName}.fileprovider"

        if (context == null || file == null) {
            throw NullPointerException()
        }
        val uri: Uri
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.applicationContext, FPAuth, file)
        } else {
            uri = Uri.fromFile(file)
        }
        return uri
    }


}