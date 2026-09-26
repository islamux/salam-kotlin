package com.islamux.khatir.navigation

/**
 * Every navigation destination, as constants — one place to rename a screen.
 *
 * READER shows the pattern for a route WITH arguments:
 *   "reader/{chapterId}?initialPage={initialPage}"
 * {chapterId} is a mandatory path segment; ?initialPage= is an OPTIONAL query
 * parameter with a default value (declared in NavGraph.kt). Renaming a screen
 * means editing this object only — nothing else hardcodes a route string.
 */
object Routes {
    const val HOME = "home"
    const val READER = "reader/{chapterId}?initialPage={initialPage}"
    const val SEARCH = "search"

    /**
     * Builds a real navigable URL by filling the placeholders, e.g.
     * readerRoute("pre", 3) -> "reader/pre?initialPage=3".
     *
     * Always navigate through this helper rather than concatenating strings by
     * hand: the format must match the READER pattern exactly, or `navigate` throws
     * IllegalArgumentException ("destination ... cannot be found") at the call site.
     */
    fun readerRoute(chapterId: String, initialPage: Int = 0) =
        "reader/$chapterId?initialPage=$initialPage"
}
