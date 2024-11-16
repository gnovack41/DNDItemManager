package com.gnovack.dnditemmanager.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnovack.dnditemmanager.services.DNDApiClient
import com.gnovack.dnditemmanager.services.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DNDApiViewModel: ViewModel() {
    private val client = DNDApiClient()

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _itemsLoading = MutableStateFlow(false)
    val itemsLoading: StateFlow<Boolean> = _itemsLoading.asStateFlow()

    private val _filtersLoading = MutableStateFlow(false)
    val filtersLoading: StateFlow<Boolean> = _filtersLoading.asStateFlow()

    fun loadItems(search: String? = null, rarity: String? = null, source: String? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.Default) {
            _itemsLoading.value = true

            val result = runCatching {
                runBlocking {
                    client.getItems(search = search, rarity = rarity, source = source)
                }
            }

            result.onSuccess {
                _uiState.update { state -> state.copy(items = it) }
            }.onFailure {
                _uiState.update { state -> state.copy(error = it.message) }
            }

            onComplete()
            _itemsLoading.value = false
        }
    }

    fun loadFilterOptions(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.Default) {
            _filtersLoading.value = true

            val result = runCatching {
                runBlocking {
                    delay(2000)
                    val sources = client.getSources()
                    val rarities = client.getRarities()

                    return@runBlocking mapOf(Pair("sources", sources), Pair("rarities", rarities))
                }
            }

            result.onSuccess {
                _uiState.update { state -> state.copy(filterOptions = it) }
            }.onFailure {
                _uiState.update { state -> state.copy(error = it.message) }
            }

            onComplete()
            _filtersLoading.value = false
        }
    }
}

data class UIState(
    var items: List<Item> = emptyList(),
    var filterOptions: Map<String, List<String>> = emptyMap(),
    var error: String? = null,
)
