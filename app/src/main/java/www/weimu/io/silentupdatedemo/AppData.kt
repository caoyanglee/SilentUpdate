package www.weimu.io.silentupdatedemo

import android.app.AlertDialog
import android.content.Context
import android.view.View
import com.weimu.universalview.OriginAppData
import com.pmm.silentupdate.SilentUpdate
import com.pmm.silentupdate.core.DialogTipAction
import com.pmm.silentupdate.core.UpdateInfo
import com.weimu.universalview.ktx.openApkByFilePath

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 13:59
 * Description:
 */
class AppData : OriginAppData() {
    override fun isDebug(): Boolean = BuildConfig.DEBUG

    override fun onCreate() {
        super.onCreate()
        //初始化 step01
        SilentUpdate.init(this)
        ///间隔弹窗提示时间- 默认7天后提醒
        SilentUpdate.intervalDay = 7
        //下载提示 -> 流量模式
        SilentUpdate.downLoadTipDialog = object : DialogTipAction {
            override fun show(context: Context, updateInfo: UpdateInfo, positiveClick: () -> Unit, negativeClick: () -> Unit) {
                val dialog = AlertDialog.Builder(context)
                        .setCancelable(!updateInfo.isForce)
                        .setTitle(updateInfo.title)
                        .setMessage("下载提示弹窗 自定义 ${updateInfo.msg}")
                        .setPositiveButton("立即更新", null)
                        .setNegativeButton("稍后", null)
                        .create()
                dialog.setOnShowListener {
                    //positive
                    val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    posBtn.setOnClickListener {
                        dialog.dismiss()
                        positiveClick()
                    }
                    val negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    //negative
                    if (updateInfo.isForce) {
                        negBtn.visibility = View.GONE
                    } else {
                        negBtn.setOnClickListener {
                            dialog.dismiss()
                            negativeClick()
                        }
                    }
                }
                dialog.show()
            }

        }
        //安装提示 -> 无线模式，文件已存在
        SilentUpdate.installTipDialog = object : DialogTipAction {
            override fun show(context: Context, updateInfo: UpdateInfo, positiveClick: () -> Unit, negativeClick: () -> Unit) {
                val dialog = AlertDialog.Builder(context)
                        .setCancelable(!updateInfo.isForce)
                        .setTitle(updateInfo.title)
                        .setMessage("安装提示弹窗 自定义 ${updateInfo.msg}")
                        .setPositiveButton("立即安装", null)
                        .setNegativeButton("稍后", null)
                        .create()
                dialog.setOnShowListener {
                    //positive
                    val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    posBtn.setOnClickListener {
                        dialog.dismiss()
                        positiveClick()
                    }
                    val negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    //negative
                    if (updateInfo.isForce) {
                        negBtn.visibility = View.GONE
                    } else {
                        negBtn.setOnClickListener {
                            dialog.dismiss()
                            negativeClick()
                        }
                    }
                }
                dialog.show()
            }
        }
    }


}
