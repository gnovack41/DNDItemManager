package com.gnovack.dnditemmanager.android.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.gnovack.dnditemmanager.android.BuildConfig
import com.gnovack.dnditemmanager.services.Character
import com.gnovack.dnditemmanager.services.DNDApiClient
import com.gnovack.dnditemmanager.services.Item
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.util.UUID


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
        val errorBody: JsonObject? = null,
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
                errorBody = null,
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
                var errorBody: JsonObject? = null
                if (e is ClientRequestException) {
                    errorBody = e.response.body()
                }

                _uiState.update { state -> state.copy(isFailed = true, error = e, errorBody = errorBody) }
            }

            _uiState.update { state -> state.copy(isLoading = false) }
        }

        _uiState.value.job = requestJob
    }
}


@OptIn(SavedStateHandleSaveableApi::class)
class DNDApiViewModel(savedStateHandle: SavedStateHandle): ViewModel() {
    private var testServerUrl: String by savedStateHandle.saveable {
        mutableStateOf("")
    }

    var client = DNDApiClient(baseHostOverride = testServerUrl.ifBlank { null })
        private set

    private var _characterList: MutableStateFlow<List<Character>> = MutableStateFlow(listOf())
    val characterList: StateFlow<List<Character>> = _characterList.asStateFlow()

    fun setServerClientBaseUrl(serverUrl: String) {
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.BUILD_TYPE != "mobile-debug") return

        testServerUrl = serverUrl
        client = DNDApiClient(baseHostOverride = serverUrl.ifBlank { null })
    }

    fun getCharacterById(id: String?): Character? {
        return characterList.value.find { character -> character.id == id }?.copy()
    }

    fun updateOrCreateCharacter(character: Character): Character {
        val existingCharacterIndex = characterList.value.indexOf(character)

        if (existingCharacterIndex == -1) {
            if (character.id == null) character.id = UUID.randomUUID().toString()
            _characterList.value += character
        } else {
            _characterList.update { list ->
                list as MutableList
                list.apply {
                    set(existingCharacterIndex, character)
                }
            }
        }

        return character
    }

    fun removeCharacters(characters: List<Character>) {
        _characterList.value -= characters
    }

    fun addItemsToCharacterInventory(characterId: String, items: List<Item>) {
        _characterList.update { list ->
            val index = list.indexOfFirst { it.id == characterId }

            list as MutableList
            list.apply {
                set(index, list[index].copy(inventory = items))
            }
        }
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
