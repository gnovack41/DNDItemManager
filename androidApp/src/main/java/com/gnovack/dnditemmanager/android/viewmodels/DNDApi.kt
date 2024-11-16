package com.gnovack.dnditemmanager.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnovack.dnditemmanager.services.DNDApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class AsyncUiState<T>(
    val data: T? = null,
    val isSuccessful: Boolean = false,
    val isLoading: Boolean = false,
    val isFailed: Boolean = false,
    val error: Exception? = null,
    internal var job: Job? = null,
)


class AsyncStateHandler<P, T>(
    private val scope: CoroutineScope,
    private val request: suspend (args: List<P?>) -> T,
    private val _uiState: MutableStateFlow<AsyncUiState<T>> = MutableStateFlow(AsyncUiState()),
    val uiState: StateFlow<AsyncUiState<T>> = _uiState.asStateFlow()
) {
    private fun resetUiState(isLoading: Boolean = false) {
        _uiState.update { state ->
            state.copy(
                data = null,
                isSuccessful = false,
                isLoading = isLoading,
                isFailed = false,
                error = null,
                job = null,
            )
        }
    }

    fun executeRequest(vararg args: P?, onComplete: () -> Unit = {}) {
        _uiState.value.job?.cancel()

        resetUiState(isLoading = true)

        val requestJob = scope.launch {
            try {
                _uiState.update { state -> state.copy(isSuccessful = true, data = request(args.toList())) }
                onComplete()
            } catch (e: Exception) {
                _uiState.update { state -> state.copy(isFailed = true, error = e) }
            }

            _uiState.update { state -> state.copy(isLoading = false) }
        }

        _uiState.value.job = requestJob
    }
}



class DNDApiViewModel: ViewModel() {
    val client = DNDApiClient()

    fun <P, T> useAsyncUiState(request: suspend (args: List<P?>) -> T) = AsyncStateHandler(viewModelScope, request)
}
