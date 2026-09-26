package com.islamux.khatir.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens a WhatsApp chat with the app's contact number.
 *
 * The interesting part is the FAILURE PATH: if WhatsApp is not installed, Android
 * throws ActivityNotFoundException when we try to start the intent. Rather than
 * letting that crash the app, we catch it and open the Play Store page instead —
 * graceful degradation, the difference between a user who cannot reach us and a
 * user who sees an install button.
 */
object WhatsAppUtil {
    private const val PHONE_NUMBER = "96772699924"
    private const val WHATSAPP_PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.whatsapp"

    fun openChat(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                // A custom scheme: Android routes "whatsapp://" to WhatsApp if it is
                // installed, and throws if it is not — which is what the catch below
                // is there for. `phone=` is WhatsApp's own convention.
                data = Uri.parse("whatsapp://send?phone=$PHONE_NUMBER")
            }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // The fallback needs no special handling: a plain browser intent, which
            // every device can satisfy.
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(WHATSAPP_PLAY_STORE_URL)
            }
            context.startActivity(fallbackIntent)
        }
    }
}
