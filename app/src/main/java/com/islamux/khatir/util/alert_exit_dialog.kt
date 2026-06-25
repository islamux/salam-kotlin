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
import kotlin.system.exitProcess

@Composable
fun BackPressHandlerWithExitDialog() {
    var showDialog by remember { mutableStateOf(false) }

    BackHandler {
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = AppColors.white,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
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
                    onClick = { exitProcess(0) },
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
