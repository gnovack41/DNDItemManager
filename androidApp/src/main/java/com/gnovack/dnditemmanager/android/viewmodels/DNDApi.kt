package com.gnovack.dnditemmanager.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnovack.dnditemmanager.android.views.characters.Character
import com.gnovack.dnditemmanager.services.DNDApiClient
import com.gnovack.dnditemmanager.services.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


private const val CHARACTER_LIST_FILE = "characterList.json"


class AsyncStateHandler<P, T>(
    private val scope: CoroutineScope,
    private val request: suspend (args: List<P?>) -> T,
) {
    data class AsyncUiState<T>(
        val data: T? = null,
        val isSuccessful: Boolean = false,
        val isLoading: Boolean = false,
        val isFailed: Boolean = false,
        val error: Exception? = null,
        internal var job: Job? = null,
    )

    private val _uiState: MutableStateFlow<AsyncUiState<T>> = MutableStateFlow(AsyncUiState())
    val uiState: StateFlow<AsyncUiState<T>> = _uiState.asStateFlow()

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

    private val currentMaxCharacterId: Int
        get() = characterList.value.maxOfOrNull { it.id ?: 0 } ?: 0

    private var _characterList: MutableStateFlow<List<Character>> = MutableStateFlow(listOf())
    val characterList: StateFlow<List<Character>> = _characterList.asStateFlow()

    private var _selectedCharacterId: MutableStateFlow<Int?> = MutableStateFlow(null)
    val selectedCharacterId: StateFlow<Int?> = _selectedCharacterId.asStateFlow()

    val selectedCharacter: Character?
        get() = characterList.value.find { character -> character.id == selectedCharacterId.value }

    fun addCharacter(character: Character) {
        character.id = currentMaxCharacterId + 1
        _characterList.value += character
    }

    fun removeCharacters(characters: List<Character>) {
        _characterList.value -= characters
    }

    fun addItemsToSelectedCharacterInventory(items: List<Item>) {
        _characterList.update { list ->
            list.map { character ->
                if (character.id == _selectedCharacterId.value) {
                    character.apply { inventory = items }
                }

                return@map character
            }
        }
    }

    fun setSelectedCharacter(character: Character) {
        _selectedCharacterId.value = character.id
    }

    fun saveCharacterList(context: Context) {
        val file = File(context.filesDir, CHARACTER_LIST_FILE)
        file.writeText(Json.encodeToString(characterList.value))
    }

    fun loadCharacterList(context: Context) {
        val file = File(context.filesDir, CHARACTER_LIST_FILE)

        if (file.exists()) {
            try {
                _characterList.value = Json.decodeFromString(file.readText())
            } catch (e: Exception) {
                _characterList.value = listOf()
            }
        }
    }

    fun <P, T> useAsyncUiState(request: suspend (args: List<P?>) -> T) = AsyncStateHandler(viewModelScope, request)
}
