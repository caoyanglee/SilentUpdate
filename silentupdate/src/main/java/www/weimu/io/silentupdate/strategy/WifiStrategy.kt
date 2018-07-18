package www.weimu.io.silentupdate.strategy

import android.app.Application
import android.app.DownloadManager
import android.app.Notification
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.app.NotificationCompat
import www.weimu.io.silentupdate.R
import www.weimu.io.silentupdate.SilentUpdate
import www.weimu.io.silentupdate.core.*
import java.io.File
import java.util.*

/**
 * Wifi的情况
 */
internal class WifiStrategy : Strategy {

    companion object {
        private var strategy: Strategy? = null


        fun getDefault(): Strategy {
            if (strategy == null) {
                synchronized(Strategy::class.java) {
                    if (strategy == null) {
                        strategy = WifiStrategy()
                    }
                }
            }
            return strategy!!
        }
    }

    private constructor()


    //升级操作 WIFI的情况下
    override fun update(apkUrl: String, latestVersion: String) {
        checkUpdateUrl(apkUrl)
        val context = SilentUpdate.getApplicationContext()
        val fileName = "${context.getAppName()}_v$latestVersion.apk"
        val path = Const.UPDATE_FILE_DIR + fileName

        val taskId = context.getUpdateShare().apkTaskID
        loge("==============")
        loge("taskID=$taskId")
        if (isFileExist(path)) {
            loge("【文件已经存在】")
            if (isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                //状态：完成
                if (SilentUpdate.isUseDefaultHint) {
                    showInstallDialog(File(path)) //弹出dialog
                } else {
                    SilentUpdate.updateListener?.onFileIsExist(File(path))
                }
            } else if (isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                addRequest(apkUrl, fileName, false)
            } else if (isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            } else {
                if (SilentUpdate.isUseDefaultHint) {
                    showInstallDialog(File(path)) //弹出dialog
                } else {
                    SilentUpdate.updateListener?.onFileIsExist(File(path))
                }
            }
        } else {
            loge("开始下载")
            //绑定广播接收者
            bindReceiver()
            //不存在 直接下载
            addRequest(apkUrl, fileName)
        }
    }

    //下载完成后
    override fun afterDownLoadComplete(file: File) {
        showInstallNotification(file)
        showInstallDialog(file)
    }


    //更新Notification
    private fun showInstallNotification(file: File) {
        SilentUpdate.getCurrentActivity()?.apply {
            val title = "发现新版本！"
            val content = "请点击立即安装~"
            val intent = constructOpenApkItent(file)
            val pintent = PendingIntent.getActivity(this, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val builder = NotificationCompat.Builder(this)
            builder.setSmallIcon(R.drawable.ic_get_app_white_24dp)// 设置小图标
            builder.setLargeIcon(BitmapFactory.decodeResource(this.resources, this.getAppIcon()))//设置大图标
            builder.setTicker(title)// 手机状态栏的提示----最上面的一条
            builder.setWhen(System.currentTimeMillis())// 设置时间
            builder.setContentTitle(title)// 设置标题
            builder.setContentText(content)// 设置通知的内容
            builder.setContentIntent(pintent)// 点击后的意图
            builder.setDefaults(Notification.DEFAULT_ALL)// 设置提示全部
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//锁屏通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(Const.NOTIFICATION_CHANNEL_ID)
            }
            val notification = builder.build()// 4.1以上要用才起作用
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL// 点击后自动取消
            //显示
            notificationManager.notify(UUID.randomUUID().hashCode(), notification)
        }

    }


}
