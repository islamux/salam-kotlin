package com.islamux.khatir.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.islamux.khatir.data.repository.JsonKhatiraRepository
import com.islamux.khatir.data.repository.KhatiraRepository
import com.islamux.khatir.ui.home.HomeViewModel
import com.islamux.khatir.ui.reader.ReaderViewModel
import com.islamux.khatir.ui.search.SearchViewModel

object AppModule {

    private var repository: KhatiraRepository? = null

    fun provideRepository(context: Context): KhatiraRepository {
        if (repository == null) {
            repository = JsonKhatiraRepository(context.applicationContext)
        }
        return repository!!
    }

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
