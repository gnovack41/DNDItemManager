package com.gnovack.dnditemmanager.android.views.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.gnovack.dnditemmanager.android.components.RoundedTextField
import com.gnovack.dnditemmanager.android.components.SelectInput
import com.gnovack.dnditemmanager.android.getObjectFromJsonAssetFile
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.services.Character


@Composable
fun CharacterCreateOrUpdateDialog(
    viewModel: DNDApiViewModel = viewModel(),
    characterId: String? = null,
    closeDialog: () -> Unit,
    onSubmit: (Character) -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val context = LocalContext.current

    val existingCharacter: Character? by remember { derivedStateOf { viewModel.getCharacterById(characterId) } }

    var characterName by rememberSaveable { mutableStateOf(existingCharacter?.name ?: "") }
    var characterRace by rememberSaveable { mutableStateOf(existingCharacter?.race ?: "") }
    var characterClass by rememberSaveable { mutableStateOf(existingCharacter?.dndClass ?: "") }
    var characterLevel: Int? by rememberSaveable { mutableStateOf(existingCharacter?.level) }

    var isSubmitted by remember { mutableStateOf(false) }

    val classOptions by remember { derivedStateOf {
        getObjectFromJsonAssetFile<List<String>>(context, "classes.json")
    } }

    val raceOptions by remember { derivedStateOf {
        getObjectFromJsonAssetFile<List<String>>(context, "races.json")
    } }

    val newCharacter = existingCharacter?.copy(
        name = characterName,
        race = characterRace,
        dndClass = characterClass,
        level = characterLevel ?: 0,
    ) ?: Character(
        id = existingCharacter?.id,
        name = characterName,
        race = characterRace,
        dndClass = characterClass,
        level = characterLevel ?: 0,
    )

    Dialog(onDismissRequest = closeDialog) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "${if (existingCharacter != null) "Update" else "Create"} Character",
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(24f, TextUnitType.Sp)
                )

                if (windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT) {
                    Row (
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RoundedTextField(
                            value = characterName,
                            onValueChange = { characterName = it },
                            name = "Name",
                            supportingText = "Required",
                            isError = (characterName.isNotBlank() || isSubmitted) && !newCharacter.nameField.isValid,
                            modifier = Modifier.weight(1f)
                        )
                        SelectInput(
                            name = "Race",
                            value = characterRace,
                            options = raceOptions,
                            onOptionSelected = { characterRace = it ?: "" },
                            isError = (characterRace.isNotBlank() || isSubmitted) && !newCharacter.raceField.isValid,
                            supportingText = "Required",
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    RoundedTextField(
                        value = characterName,
                        onValueChange = { characterName = it },
                        name = "Name",
                        supportingText = "Required",
                        isError = (characterName.isNotBlank() || isSubmitted) && !newCharacter.nameField.isValid
                    )
                    SelectInput(
                        name = "Race",
                        value = characterRace,
                        options = raceOptions,
                        onOptionSelected = { characterRace = it ?: "" },
                        isError = (characterRace.isNotBlank() || isSubmitted) && !newCharacter.raceField.isValid,
                        supportingText = "Required",
                    )
                }
                SelectInput(
                    name = "Class",
                    value = characterClass,
                    options = classOptions,
                    onOptionSelected = { characterClass = it ?: "" },
                    isError = (characterRace.isNotBlank() || isSubmitted) && !newCharacter.classField.isValid,
                    supportingText = "Required",
                )
                RoundedTextField(
                    value = characterLevel?.toString() ?: "",
                    onValueChange = { characterLevel = it.toIntOrNull() },
                    name = "Level",
                    supportingText = "Required",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = (characterLevel != null || isSubmitted) && !newCharacter.levelField.isValid,
                )

                Button(
                    onClick = {
                        isSubmitted = true
                        if (newCharacter.isValid) onSubmit(newCharacter)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }

}
