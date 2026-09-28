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
 * Confirms before leaving the app: on the home screen Back has nowhere to go, so
 * it would close the app and lose the user's place. Only HomeScreen installs this.
 */
@Composable
fun BackPressHandlerWithExitDialog() {
    // `by remember` keeps the dialog's open/closed state across recompositions.
    var showDialog by remember { mutableStateOf(false) }

    // BackHandler replaces the default Back behaviour while in composition.
    BackHandler {
        showDialog = true
    }

    val context = LocalContext.current
    // `as?` is a safe cast: null instead of throwing when not an Activity.
    val activity = context as? Activity

    // Conditional rendering is how Compose shows and hides the dialog.
    if (showDialog) {
        AlertDialog(
            // Runs on outside tap or Back again.
            onDismissRequest = { showDialog = false },
            containerColor = AppColors.white,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        // AutoMirrored icons flip automatically in RTL.
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
                    // `activity?.` does nothing when the cast above gave null (preview or test)
                    // instead of crashing. finishAffinity() closes this activity and its whole task.
                    onClick = { activity?.finishAffinity() },
                    // Red: the destructive choice must not look like the safe one.
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
                // "No" just hides the dialog.
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

