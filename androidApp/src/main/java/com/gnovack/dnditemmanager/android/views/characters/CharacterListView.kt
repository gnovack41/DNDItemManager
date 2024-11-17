package com.gnovack.dnditemmanager.android.views.characters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel


@Composable
fun CharacterListView(
    viewModel: DNDApiViewModel = viewModel(),
    onNavigateToItemList: (Character) -> Unit,
    onOpenCharacterCreateDialog: () -> Unit,
) {
    val characterList by viewModel.characterList.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = onOpenCharacterCreateDialog,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "+ Add Character", softWrap = false)
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(characterList){ character ->
                CharacterListItem(
                    character = character,
                    onClick = { onNavigateToItemList(character) },
                )
            }
        }
    }
}


@Composable
fun CharacterListItem(
    character: Character,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        ListItem(
            headlineContent = { Text("Name: ${character.name}", fontWeight = FontWeight.Bold) },
            supportingContent = { Text("Class: ${character.dndClass} (${character.level})") },
            trailingContent = {
                Text(
                    "# Items: ${character.inventory.size}",
                    fontSize = TextUnit(14f, TextUnitType.Sp)
                )
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        )
    }
}
