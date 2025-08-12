package com.hhkv.rustitok.utils
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtil {
    var clipboardManager: ClipboardManager? = null

    fun copyText(context: Context,text:String){
        if (clipboardManager == null){
            clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        }
        val clipData = ClipData.newPlainText("pika",text)
        clipboardManager?.setPrimaryClip(clipData)
    }

    fun clearClipboard() {
        clipboardManager?.let {
            if (it.hasPrimaryClip()){
                it.setPrimaryClip(ClipData.newPlainText("pika",null))
            }
        }
    }

}