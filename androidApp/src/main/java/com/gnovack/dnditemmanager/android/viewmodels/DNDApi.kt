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

    fun loadItems(rarity: String? = null, source: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _itemsLoading.value = true

            val result = runCatching {
                runBlocking {
                    delay(2000)
                    client.getItems(limit = 20, rarity = rarity, source = source)
                }
            }

            result.onSuccess {
                _uiState.value = UIState(items = it, filterOptions = _uiState.value.filterOptions)
            }.onFailure {
                _uiState.value = UIState(error = it.message, filterOptions = _uiState.value.filterOptions)
            }

            _itemsLoading.value = false
        }
    }

    fun loadFilterOptions() {
        viewModelScope.launch(Dispatchers.IO) {
            _filtersLoading.value = true

            val result = runCatching {
                runBlocking {
                    val sources = client.getSources()
                    val rarities = client.getRarities()

                    return@runBlocking mapOf(Pair("sources", sources), Pair("rarities", rarities))
                }
            }

            result.onSuccess {
                _uiState.value = UIState(filterOptions = it)
            }.onFailure {
                _uiState.value = UIState(error = it.message)
            }

            _filtersLoading.value = false
        }
    }
}

class UIState(
    var items: List<Item> = emptyList(),
    var filterOptions: Map<String, List<String>> = emptyMap(),
    var error: String? = null,
)
