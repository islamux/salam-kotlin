package com.islamux.khatir

import android.app.Application

/**
 * Custom Application class — the FIRST code the Android system runs for this app,
 * before any Activity exists.
 *
 * It is registered in app/src/main/AndroidManifest.xml via android:name=".KhatirApp".
 * We keep it empty because this project needs no app-startup logic: dependency
 * wiring lives in di/AppModule.kt instead (manual DI — read that file next).
 *
 * Kotlin note: `class KhatirApp : Application()` — the parentheses call the
 * superclass constructor with no arguments. Kotlin has no `extends` keyword;
 * the colon IS inheritance.
 */
class KhatirApp : Application()
