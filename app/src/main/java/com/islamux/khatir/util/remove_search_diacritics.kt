package com.islamux.khatir.util

fun removeSearchDiacritics(text: String?): String {
    if (text == null) return ""
    return text.filter { it in '\u0621'..'\u064A' || it.isWhitespace() }
}
