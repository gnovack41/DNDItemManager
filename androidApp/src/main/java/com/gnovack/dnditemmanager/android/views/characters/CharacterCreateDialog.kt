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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.window.core.layout.WindowWidthSizeClass
import com.gnovack.dnditemmanager.android.components.RoundedTextField


@Composable
fun CharacterCreateDialog(
    closeDialog: () -> Unit,
    onSubmit: (Character) -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    var characterName by rememberSaveable { mutableStateOf("") }
    var characterRace by rememberSaveable { mutableStateOf("") }
    var characterClass by rememberSaveable { mutableStateOf("") }
    var characterLevel: Int? by rememberSaveable { mutableStateOf(null) }

    var isSubmitted by remember { mutableStateOf(false) }

    val newCharacter = Character(
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
                    text = "Create New Character",
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
                        RoundedTextField(
                            value = characterRace,
                            onValueChange = { characterRace = it },
                            name = "Race",
                            supportingText = "Required",
                            isError = (characterRace.isNotBlank() || isSubmitted) && !newCharacter.raceField.isValid,
                            modifier = Modifier.weight(1f)
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
                    RoundedTextField(
                        value = characterRace,
                        onValueChange = { characterRace = it },
                        name = "Race",
                        supportingText = "Required",
                        isError = (characterRace.isNotBlank() || isSubmitted) && !newCharacter.raceField.isValid
                    )
                }
                RoundedTextField(
                    value = characterClass,
                    onValueChange = { characterClass = it },
                    name = "Class",
                    supportingText = "Required",
                    isError = (characterClass.isNotBlank() || isSubmitted) && !newCharacter.classField.isValid
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
