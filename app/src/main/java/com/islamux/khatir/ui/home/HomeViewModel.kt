package com.islamux.khatir.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.repository.KhatiraRepository
import com.islamux.khatir.data.static.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** What HomeScreen draws: the chapter list plus loading and error flags. */
data class HomeUiState(
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * HomeScreen's brain: fetches the chapter list and exposes it as observable state,
 * with zero UI code. The other two ViewModels copy this shape.
 */
class HomeViewModel(private val repository: KhatiraRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            // viewModelScope is Dispatchers.Main, so this body runs ON the UI
            // thread: `suspend` lets a call pause, it does not move it to a
            // background thread. That needs withContext(Dispatchers.IO), which this
            // project does not do, so the first content load blocks the UI thread.
            try {
                val chapters = repository.getAllChapters()
                _uiState.value = HomeUiState(chapters = chapters, isLoading = false, error = null)
            } catch (e: Exception) {
                // `e.message` is nullable, so fall back rather than publish a null
                // error the UI could not display.
                _uiState.value = HomeUiState(isLoading = false, error = e.message ?: AppStrings.unknownError)
            }
        }
    }

    /**
     * Supplies the repository Android cannot pass itself: it can only rebuild a
     * ViewModel through a no-argument constructor. See di/AppModule.kt.
     */
    class Factory(private val repository: KhatiraRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
