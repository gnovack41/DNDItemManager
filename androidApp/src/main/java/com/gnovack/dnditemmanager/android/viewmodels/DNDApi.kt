package com.gnovack.dnditemmanager.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnovack.dnditemmanager.services.DNDApiClient
import com.gnovack.dnditemmanager.services.Item
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


class DNDApiViewModel: ViewModel() {
    private val client = DNDApiClient()

    private val _itemsRequestState = MutableStateFlow(AsyncUiState<List<Item>>())
    val itemsRequestState: StateFlow<AsyncUiState<List<Item>>> = _itemsRequestState.asStateFlow()

    private val _itemFiltersRequestState = MutableStateFlow(AsyncUiState<Map<String, List<String>>>())
    val itemFiltersRequestState: StateFlow<AsyncUiState<Map<String, List<String>>>> = _itemFiltersRequestState.asStateFlow()

    private fun <T> doBaseRequest(
        uiState: MutableStateFlow<AsyncUiState<T>>,
        request: suspend () -> T,
        onComplete: () -> Unit = {},
    ) {
        uiState.value.job?.cancel()

        uiState.update { state -> state.copy(isLoading = true) }

        val requestJob = viewModelScope.launch {
            try {
                uiState.update { state -> state.copy(isSuccessful = true, data = request()) }
                onComplete()
            } catch (e: Exception) {
                uiState.update { state -> state.copy(isFailed = true, error = e) }
            }

            uiState.update { state -> state.copy(isLoading = false) }
        }

        uiState.value.job = requestJob
    }

//    class AsyncStateHandler<T>(
//        private val scope: CoroutineScope,
//        private val request: suspend (args: Any) -> T,
//    ) {
//        private val _uiState = MutableStateFlow(AsyncUiState<T>())
//        val uiState: StateFlow<AsyncUiState<T>> = _uiState.asStateFlow()
//
//        fun executeRequest(vararg args: Any?, onComplete: () -> Unit = {}) {
//            _uiState.value.job?.cancel()
//
//            _uiState.update { state -> state.copy(isLoading = true) }
//
//            val requestJob = scope.launch {
//                try {
//                    _uiState.update { state -> state.copy(isSuccessful = true, data = request(args)) }
//                    onComplete()
//                } catch (e: Exception) {
//                    _uiState.update { state -> state.copy(isFailed = true, error = e) }
//                }
//
//                _uiState.update { state -> state.copy(isLoading = false) }
//            }
//
//            _uiState.value.job = requestJob
//        }
//    }
//
//    fun <T> useAsyncUiState(request: suspend (args: Any?) -> T) = AsyncStateHandler(viewModelScope, request)

    fun loadItems(
        search: String? = null,
        rarity: String? = null,
        source: String? = null,
        onComplete: () -> Unit = {},
    ) = doBaseRequest(
        uiState = _itemsRequestState,
        request = { client.getItems(search = search, rarity = rarity, source = source) },
        onComplete = onComplete
    )

    fun loadFilterOptions(onComplete: () -> Unit = {}) = doBaseRequest(
        uiState = _itemFiltersRequestState,
        request = { mapOf("sources" to client.getSources(), "rarities" to client.getRarities()) },
        onComplete = onComplete
    )
}
