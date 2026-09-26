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

/**
 * What HomeScreen draws: the chapter list plus the loading and error flags.
 *
 * Same idea as ReaderUiState — one immutable object holding everything the UI
 * needs, so the UI never has to combine several sources of truth itself.
 */
data class HomeUiState(
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * HomeScreen's brain — the "VM" in MVVM. It fetches the chapter list from the
 * repository and exposes it as observable state. It contains ZERO UI code.
 *
 * CANONICAL PATTERNS (the other two ViewModels follow this exact shape — read
 * this file first when you read them):
 *
 *  1. Backing property. [_uiState] is private AND mutable; [uiState] is public
 *     and read-only. The UI can observe the state but can never set it, which
 *     keeps the data flow one-way: user event -> ViewModel -> new state -> UI.
 *  2. viewModelScope. A coroutine scope tied to this ViewModel's life, cancelled
 *     automatically when the ViewModel dies — so leaving the screen cannot leave
 *     a coroutine running against a dead UI.
 *  3. init { loadChapters() }. Work starts the moment the ViewModel is created,
 *     not when the screen first needs it.
 */
class HomeViewModel(private val repository: KhatiraRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            // Coroutines read like sequential code but run off the main thread, and
            // the suspend repository call may do IO. Everything sits inside
            // try/catch so ANY failure becomes error state instead of a crash.
            try {
                val chapters = repository.getAllChapters()
                _uiState.value = HomeUiState(chapters = chapters, isLoading = false, error = null)
            } catch (e: Exception) {
                // `e.message` is nullable (an exception may carry no message), so the
                // elvis operator `?:` substitutes the generic Arabic message rather
                // than publishing a null error the UI could not display.
                _uiState.value = HomeUiState(isLoading = false, error = e.message ?: AppStrings.unknownError)
            }
        }
    }

    /**
     * Builds this ViewModel for the Android system.
     *
     * Android recreates ViewModels itself (on rotation, for example) and can only
     * do so through a no-argument constructor — but ours needs a repository. A
     * Factory is the bridge: the system asks it for a ViewModel and it supplies
     * the dependencies. AppModule provides the instance (di/AppModule.kt).
     *
     * @Suppress("UNCHECKED_CAST") silences the unavoidable generic cast below:
     * the system only ever asks for HomeViewModel, so the cast is safe even
     * though the compiler cannot prove it.
     */
    class Factory(private val repository: KhatiraRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
