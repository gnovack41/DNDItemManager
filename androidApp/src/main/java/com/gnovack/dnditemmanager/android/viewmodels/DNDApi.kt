package com.gnovack.dnditemmanager.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnovack.dnditemmanager.android.views.characters.Character
import com.gnovack.dnditemmanager.services.DNDApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AsyncStateHandler<P, T>(
    private val scope: CoroutineScope,
    private val request: suspend (args: List<P?>) -> T,
    private val _uiState: MutableStateFlow<AsyncUiState<T>> = MutableStateFlow(AsyncUiState()),
    val uiState: StateFlow<AsyncUiState<T>> = _uiState.asStateFlow()
) {
    data class AsyncUiState<T>(
        val data: T? = null,
        val isSuccessful: Boolean = false,
        val isLoading: Boolean = false,
        val isFailed: Boolean = false,
        val error: Exception? = null,
        internal var job: Job? = null,
    )

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

    private var _characterList: MutableStateFlow<List<Character>> = MutableStateFlow(listOf())
    val characterList: StateFlow<List<Character>> = _characterList.asStateFlow()

    private var _characterItemMap: MutableStateFlow<MutableMap<Int, List<String>>> = MutableStateFlow(mutableMapOf())
    val characterItemMap: StateFlow<MutableMap<Int, List<String>>> = _characterItemMap.asStateFlow()

    private var _selectedCharacter: MutableStateFlow<Character?> = MutableStateFlow(null)
    val selectedCharacter: StateFlow<Character?> = _selectedCharacter.asStateFlow()

    fun addCharacter(character: Character) {
        character.id = _characterList.value.size + 1
        _characterItemMap.update { map -> (map + (character.id!! to listOf())) as MutableMap}
        _characterList.value += character
    }

    fun addItemsToSelectedCharacterInventory(items: List<String>) {
        if (selectedCharacter.value == null) return

        val updateMap = _characterItemMap.value.toMutableMap()
        updateMap[_selectedCharacter.value!!.id!!] = items

        _characterItemMap.value = updateMap
    }

    fun setSelectedCharacter(character: Character) {
        _selectedCharacter.value = character
    }

    fun <P, T> useAsyncUiState(request: suspend (args: List<P?>) -> T) = AsyncStateHandler(viewModelScope, request)
}
