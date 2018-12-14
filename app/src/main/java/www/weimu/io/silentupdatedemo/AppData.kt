package www.weimu.io.silentupdatedemo

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import com.weimu.universalib.OriginAppData
import www.weimu.io.silentupdate.SilentUpdate
import www.weimu.io.silentupdate.core.dialog.DialogTipAction

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
            override fun show(context: Context, updateContent: String, positiveClick: () -> Unit, negativeClick: () -> Unit) {
                AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("提示")
                        .setMessage("下载提示弹窗 自定义 $updateContent")
                        .setPositiveButton("立即更新") { dialog, which -> positiveClick() }
                        .setNegativeButton("稍后") { dialog, which -> negativeClick() }
                        .show()
            }

        }
        //安装提示 -> 无线模式，文件已存在
        SilentUpdate.installTipDialog = object : DialogTipAction {
            override fun show(context: Context, updateContent: String, positiveClick: () -> Unit, negativeClick: () -> Unit) {
                AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("提示")
                        .setMessage("安装提示弹窗 自定义 $updateContent")
                        .setPositiveButton("立即安装") { dialog, which -> positiveClick() }
                        .setNegativeButton("稍后") { dialog, which -> negativeClick() }
                        .show()
            }
        }
    }


}
