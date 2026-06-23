package com.islamux.khatir.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.repository.KhatiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(private val repository: KhatiraRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            try {
                val chapters = repository.getAllChapters()
                _uiState.value = HomeUiState(chapters = chapters, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = HomeUiState(isLoading = false)
            }
        }
    }

    class Factory(private val repository: KhatiraRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
