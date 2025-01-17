package com.gnovack.dnditemmanager.android.views.characters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.gnovack.dnditemmanager.android.components.RoundedTextField
import com.gnovack.dnditemmanager.android.parseDndBeyondCharacterIdFromUrl
import com.gnovack.dnditemmanager.android.viewmodels.AsyncStateHandler
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.services.Character

@Composable
fun CharacterImportDialog(
    viewModel: DNDApiViewModel = viewModel(),
    closeDialog: () -> Unit,
    onImportComplete: (Character) -> Unit,
) {
    val characterImportAsyncHandler by remember { derivedStateOf {
        viewModel.useAsyncUiState<String, Character> { characterId ->
            viewModel.client.importCharacter(characterId.first()!!)
        }
    } }

    val characterImportState by characterImportAsyncHandler.uiState.collectAsState()

    var foundCharacter by remember { mutableStateOf<Character?>(null) }

    Dialog(onDismissRequest = { if (!characterImportState.isLoading) closeDialog() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (foundCharacter == null) CharacterImportUrlSection(
                    characterImportAsyncHandler,
                    characterImportState,
                    onCharacterFound = { foundCharacter = it }
                ) else CharacterImportConfirmSection(
                    character = foundCharacter!!,
                    onConfirm = {
                        onImportComplete(foundCharacter!!)
                    },
                    onDecline = {
                        characterImportAsyncHandler.resetUiState(isLoading = false)
                        foundCharacter = null
                    }
                )
            }
        }
    }
}

@Composable
fun CharacterImportUrlSection(
    characterImportAsyncHandler: AsyncStateHandler<String?, Character>,
    characterImportState: AsyncStateHandler.AsyncUiState<Character>,
    onCharacterFound: (Character) -> Unit,
) {
    var characterUrl: String by rememberSaveable { mutableStateOf("") }

    val characterId by remember { derivedStateOf {
        parseDndBeyondCharacterIdFromUrl(characterUrl)
    }}

    var submitted by remember { mutableStateOf(false) }

    if (characterImportState.isSuccessful) {
        onCharacterFound(characterImportState.data!!)
    }

    Text(
        text = "Import Character",
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(24f, TextUnitType.Sp)
    )

    Row {
        RoundedTextField(
            value = characterUrl,
            onValueChange = { characterUrl = it },
            name = "DNDBeyond Character URL",
            supportingText = "Required",
            isError = submitted && characterUrl.isBlank(),
            modifier = Modifier.weight(1f)
        )
    }
    Row {
        RoundedTextField(
            value = characterId,
            onValueChange = {  },
            name = "Character ID",
            enabled = false,
            modifier = Modifier.weight(1f)
        )
    }

    if (characterImportState.isFailed) {
        val characterNotFound = characterImportState.errorBody?.containsKey("missing_character") == true

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Failed to Import Character",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
                if (characterNotFound) Text(
                    "Character not found. Pleasee ensure your character is public on DNDBeyond.",
                    color = MaterialTheme.colorScheme.error,
                ) else Text(
                    "An error occurred. Please try again later.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    Button(
        onClick = {
            submitted = true
            if (characterId.isNotBlank()) characterImportAsyncHandler.executeRequest(characterId)
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !characterImportState.isLoading,
    ) {
        if (characterImportState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(32.dp)
                    .padding(top = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )
        } else {
            Text(text = "Submit")
        }
    }
}

@Composable
fun CharacterImportConfirmSection(
    character: Character,
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
) {
    Text(
        "Character Found!",
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(24f, TextUnitType.Sp)
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
    ) {
        ListItem(
            modifier = Modifier.background(color = Color.LightGray),
            headlineContent = { Text(character.name, fontWeight = FontWeight.Bold) },
            leadingContent = {
                Column(
                    modifier = Modifier
                        .height(70.dp)
                        .width(70.dp)
                ) {
                    if (character.avatarUrl != null) AsyncImage(
                        model = character.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxHeight()
                    ) else Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxSize()
                            .background(Color.LightGray)
                            .padding(16.dp)
                    )
                }
            },
            supportingContent = {
                Column {
                    Text("Race: ${character.race}")
                    Text("Class: ${character.dndClass}(${character.level})")
                }
            }
        )
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(onClick = onDecline, colors = ButtonDefaults.filledTonalButtonColors()) {
            Text("Go Back")
        }
        Button(onClick = onConfirm) {
            Text("Confirm")
        }
    }
}
