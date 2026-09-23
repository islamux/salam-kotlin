package com.islamux.khatir.util

import android.content.Context
import android.content.Intent

object ShareUtil {

    fun shareText(context: Context, text: String, chooserTitle: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
    }
}
