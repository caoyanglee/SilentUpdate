package com.pmm.silentupdate.core

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Author:你需要一台永动机
 * Date:2019-08-15 11:24
 * Description:接收下载完成的指令
 */
internal class AppUpdateReceiver : BroadcastReceiver() {
    var onDownloadComplete: ((intent: Intent) -> Unit)? = null


    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                onDownloadComplete?.invoke(intent)
            }
        }
    }
}