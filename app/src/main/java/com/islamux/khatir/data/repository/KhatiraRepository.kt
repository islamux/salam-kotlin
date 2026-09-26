package com.islamux.khatir.data.repository

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.KhatiraContent

/**
 * The data contract every content source must fulfill.
 *
 * Why an interface? The ViewModels depend on THIS abstraction, not on the JSON
 * implementation. Two payoffs:
 *  1. Unit tests inject a fake repository (see HomeViewModelErrorStateTest) and
 *     run on the JVM in milliseconds — no asset, no Android, no emulator.
 *  2. A future database or remote source can replace JsonKhatiraRepository
 *     without touching a single ViewModel.
 *
 * Kotlin note: an interface describes WHAT is available, never HOW. The
 * implementation (`: KhatiraRepository`) supplies the how.
 */
interface KhatiraRepository {
    /** The whole book. `suspend` = may do slow IO, so it runs off the main thread. */
    suspend fun getContent(): KhatiraContent

    /** One chapter by id, or null when the id does not exist. */
    suspend fun getChapter(chapterId: String): Chapter?

    /** Every chapter, in the order stored in the content file. */
    suspend fun getAllChapters(): List<Chapter>
}
