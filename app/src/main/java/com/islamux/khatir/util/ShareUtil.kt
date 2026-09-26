package com.islamux.khatir.util

import android.content.Context
import android.content.Intent

/**
 * Shares plain text with ANY app the user chooses (WhatsApp, Telegram, SMS, mail).
 *
 * An Intent is Android's system for asking another app to do something: it is a
 * message with an action ("send this") plus the payload. `createChooser` wraps it
 * in the system picker sheet, which is why we never name a target app here.
 *
 * Called from HomeScreen and ReaderScreen. The text itself is built by the
 * ViewModel (ReaderViewModel.getShareText), so this object stays a pure Android
 * helper with no knowledge of chapters or pages.
 */
object ShareUtil {

    /**
     * Opens the share sheet for [text].
     *
     * @param chooserTitle the title shown on the system picker.
     *
     * `apply { }` configures the freshly created Intent and returns it, all in one
     * expression — it is Kotlin's "build this object" scope function
     * (`.also` in di/AppModule.kt does the same job for a different shape).
     */
    fun shareText(context: Context, text: String, chooserTitle: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            // The MIME type tells the receiving app what kind of data this is;
            // "text/plain" is what makes the target app offer its text field.
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
    }
}
