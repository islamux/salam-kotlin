package com.islamux.khatir.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppUtil {
    private const val COUNTRY_CODE = "YE"
    private const val PHONE_NUMBER = "772699924"
    private const val WHATSAPP_PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.whatsapp"

    fun openChat(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("whatsapp://send?phone=$COUNTRY_CODE$PHONE_NUMBER")
            }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(WHATSAPP_PLAY_STORE_URL)
            }
            context.startActivity(fallbackIntent)
        }
    }
}
