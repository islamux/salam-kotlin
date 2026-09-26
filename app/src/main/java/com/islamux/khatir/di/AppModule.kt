package com.islamux.khatir.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.islamux.khatir.data.repository.JsonKhatiraRepository
import com.islamux.khatir.data.repository.KhatiraRepository
import com.islamux.khatir.ui.home.HomeViewModel
import com.islamux.khatir.ui.reader.ReaderViewModel
import com.islamux.khatir.ui.search.SearchViewModel

/**
 * Hand-written dependency-injection container. This project deliberately uses no
 * Hilt/Dagger — the whole graph is three factories, so a framework would add
 * build time and indirection for nothing.
 *
 * Kotlin `object` = a class with exactly ONE instance, created lazily the first
 * time it is touched. That is all a service locator like this needs.
 *
 * Screens call these provide* functions when they create their ViewModel:
 *   viewModel(factory = AppModule.provideHomeViewModelFactory(LocalContext.current))
 *
 * How a dependency reaches a ViewModel: screen asks AppModule for a Factory ->
 * AppModule builds the repository -> the Factory hands both to the ViewModel's
 * constructor. Nobody calls `new` on a ViewModel directly.
 */
object AppModule {

    // Nullable so it can start as "not created yet" — the usual lazy-singleton shape.
    private var repository: KhatiraRepository? = null

    // `?:` + `.also` = the classic one-liner: if repository exists use it, otherwise
    // build one; `.also` stores the new instance into the field AND returns it, so we
    // both cache and hand it back in one expression.
    //
    // applicationContext (not the Activity's own context) on purpose: a repository
    // outlives any single screen, and holding an Activity context here would leak
    // that destroyed Activity for the app's whole lifetime.
    fun provideRepository(context: Context): KhatiraRepository =
        repository ?: JsonKhatiraRepository(context.applicationContext).also { repository = it }

    // Each screen needs a DIFFERENT factory because each ViewModel takes different
    // constructor arguments — ReaderViewModel, for instance, also needs a chapterId.

    fun provideHomeViewModelFactory(context: Context): ViewModelProvider.Factory {
        return HomeViewModel.Factory(provideRepository(context))
    }

    fun provideReaderViewModelFactory(
        context: Context,
        chapterId: String
    ): ViewModelProvider.Factory {
        return ReaderViewModel.Factory(provideRepository(context), chapterId)
    }

    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory {
        return SearchViewModel.Factory(provideRepository(context))
    }
}
