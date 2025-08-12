package com.hhkv.rustitok.utils
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import com.hhkv.rustitok.R
import com.hhkv.rustitok.application.LocationApplication
import java.lang.Thread.UncaughtExceptionHandler

/***
 * 崩溃拦截和管理
 */
class CrashManager : UncaughtExceptionHandler {

    companion object {
        const val TAG = "CrashManager"
        const val DELAY = 1000L // 1s后重启延迟（单位毫秒）
        val instance by lazy { CrashManager() }
    }

    var context: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun init(context: Context) {
        this.context = context
        Thread.currentThread().uncaughtExceptionHandler = this
    }

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        Log.d(TAG, "捕捉到崩溃: ${p1.cause?.message}")
        if (p1.cause?.message == "重启") {
            restart(p1)
        }
    }

    /***
     * 开始重启步骤
     */
    fun restart(throwable: Throwable?) {
        if (throwable == null) return
        try {
            mainHandler.postDelayed({
                context?.let {
                    val packageName = it.packageName
                    val packageManager = it.packageManager
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        launchIntent.putExtra("FROM_CRASH",true)
                        it.startActivity(launchIntent)
                        Log.d(TAG,"定时重启任务设置成功")
                    } else {
                        Log.d(TAG,"定时重启任务设置失败 获取launchIntent失败")
                    }
                }
            }, DELAY)
        } catch (e: Exception) {
            Log.e(TAG, "重启失败", e)
        }
    }

}