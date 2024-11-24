package com.gnovack.dnditemmanager.android.views.characters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnovack.dnditemmanager.android.components.RoundedTextField
import com.gnovack.dnditemmanager.android.parseDndBeyondCharacterUrl
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.services.Character

@Composable
fun CharacterImportDialog(
    viewModel: DNDApiViewModel = viewModel(),
    closeDialog: () -> Unit,
    onImportComplete: (Character) -> Unit,
) {
    var characterUrl: String by remember { mutableStateOf("") }

    val characterImportAsyncHandler by remember { derivedStateOf {
        viewModel.useAsyncUiState<Nothing, Unit> {
            val characterId = parseDndBeyondCharacterUrl(characterUrl)
            onImportComplete(viewModel.client.importCharacter(characterId))
        }
    } }

    val characterImportState by characterImportAsyncHandler.uiState.collectAsState()

    var submitted by remember { mutableStateOf(false) }

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
                Text(
                    text = "Import Character",
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(24f, TextUnitType.Sp)
                )

                Row {
                    RoundedTextField(
                        value = characterUrl,
                        onValueChange = { characterUrl = it },
                        name = "Link to DNDBeyond Character",
                        supportingText = "Required",
                        isError = submitted && characterUrl.isBlank(),
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
                        if (characterUrl.isNotBlank()) characterImportAsyncHandler.executeRequest()
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
        }
    }
}