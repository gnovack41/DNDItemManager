package com.gnovack.dnditemmanager.android.views.characters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var selectedCharacters by rememberSaveable {
        mutableStateOf(setOf<Character>())
    }

    Scaffold(
        floatingActionButton = {
            if (selectedCharacters.isNotEmpty()) ExtendedFloatingActionButton(
                text = { Text(text = "Delete Characters") },
                icon = { Icon(Icons.Default.Clear, contentDescription = "Test") },
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    viewModel.removeCharacters(selectedCharacters.toList())
                    selectedCharacters = setOf()
                },
            )
        },
        modifier = Modifier.shadow(16.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxWidth(),
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
                        selected = character in selectedCharacters,
                        onClick = { onNavigateToItemList(character) },
                        onLongClick = {
                            if (character in selectedCharacters) selectedCharacters -= character
                            else selectedCharacters += character
                        },
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CharacterListItem(
    character: Character,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (selected) 3.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else Color.LightGray
        ),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        ListItem(
            headlineContent = { Text(character.name, fontWeight = FontWeight.Bold) },
            leadingContent = {
                Column(
                    modifier = Modifier
                        .height(70.dp)
                        .width(70.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            },
            supportingContent = {
                Column {
                    Text("Race: ${character.race}")
                    Text("Class: ${character.dndClass} (${character.level})")
                }
            },
        )
    }
}
