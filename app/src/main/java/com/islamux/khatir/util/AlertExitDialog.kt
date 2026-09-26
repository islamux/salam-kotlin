package com.islamux.khatir.util

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamux.khatir.data.static.AppStrings
import com.islamux.khatir.ui.theme.AmiriFontFamily
import com.islamux.khatir.ui.theme.AppColors
import android.app.Activity
import androidx.compose.ui.platform.LocalContext

/**
 * Confirms before the user leaves the app, instead of closing it instantly.
 *
 * The problem it solves: on the home screen there is nothing to "go back" TO, so
 * the system Back button would close the app. People then lose their place by
 * accident, so we intercept Back and ask first.
 *
 * Only HomeScreen installs this handler. The reader and search screens do not,
 * which is exactly right: there, Back means "return to the previous screen" and
 * must keep working normally.
 */
@Composable
fun BackPressHandlerWithExitDialog() {
    // Same `by remember { mutableStateOf(...) }` delegate pattern taught in
    // PageContent, with a Boolean instead of an Int. `remember` is what keeps the
    // dialog's open/closed state across recompositions.
    var showDialog by remember { mutableStateOf(false) }

    // BackHandler intercepts the system Back button for as long as this composable
    // is in the composition: the block runs instead of the default behaviour, so
    // navigation does not pop and the app does not close.
    BackHandler {
        showDialog = true
    }

    val context = LocalContext.current
    // `as?` is a SAFE cast: it yields null instead of throwing when the object is
    // not an Activity (which happens in previews and tests).
    val activity = context as? Activity

    // Rendering the dialog conditionally is how Compose shows and hides it — there
    // is no separate show()/hide() API.
    if (showDialog) {
        AlertDialog(
            // Called when the user taps outside the dialog or presses Back again.
            onDismissRequest = { showDialog = false },
            containerColor = AppColors.white,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        // An AutoMirrored icon: Compose flips it automatically in
                        // RTL, which is why it is used instead of the plain one.
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = AppColors.golden
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.alertTitle,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.black
                    )
                }
            },
            text = {
                Text(
                    text = AppStrings.alertExitMessage,
                    fontFamily = AmiriFontFamily,
                    color = AppColors.grey,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    // `activity?.` is the safe call: if the cast above produced null
                    // (preview or test), this does nothing instead of crashing.
                    // finishAffinity() closes this activity AND its whole task, so
                    // the app disappears completely — the correct choice on a screen
                    // that may itself be several levels deep in the back stack.
                    onClick = { activity?.finishAffinity() },
                    // Red: the destructive choice should not look like the safe one.
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = AppStrings.alertYes,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                // "No" simply hides the dialog and leaves the user where they were.
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.black
                    )
                ) {
                    Text(
                        text = AppStrings.alertNo,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        )
    }
}

