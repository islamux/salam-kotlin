package com.islamux.khatir.util

/**
 * Makes search diacritic-insensitive by keeping ONLY Arabic letters plus
 * whitespace.
 *
 * How it works, in one line: harakat (the fatha, kasra, damma and friends) and
 * every symbol sit at HIGHER code points than the letters, so keeping only the
 * letters range \u0621..\u064A (hamza through yaa) drops the diacritics
 * automatically. That is cheaper and less error-prone than listing every
 * diacritic character to remove.
 *
 * Applied to BOTH sides of every comparison in SearchViewModel.search — the query
 * AND the content — so a user typing the bare word still matches text written
 * with full vowel marks.
 */
fun removeSearchDiacritics(text: String?): String {
    // Nullable input, non-null output: a null becomes "" so callers never have to
    // special-case it before comparing.
    if (text == null) return ""
    // `filter` keeps the characters that satisfy the condition, i.e. Arabic letters
    // or spaces — everything else is dropped.
    return text.filter { it in '\u0621'..'\u064A' || it.isWhitespace() }
}
