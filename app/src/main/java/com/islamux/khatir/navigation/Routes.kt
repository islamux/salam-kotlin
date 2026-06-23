package com.islamux.khatir.navigation

object Routes {
    const val HOME = "home"
    const val READER = "reader/{chapterId}?initialPage={initialPage}"
    const val SEARCH = "search"

    fun readerRoute(chapterId: String, initialPage: Int = 0) =
        "reader/$chapterId?initialPage=$initialPage"
}
