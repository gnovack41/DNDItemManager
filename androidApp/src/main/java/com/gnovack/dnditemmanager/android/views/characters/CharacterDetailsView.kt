package com.gnovack.dnditemmanager.android.views.characters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnovack.dnditemmanager.android.components.ItemRow
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel


@Composable
fun CharacterDetailsView(
    viewModel: DNDApiViewModel = viewModel(),
    characterId: Int,
    onNavigateToItemList: () -> Unit,
    onOpenCharacterUpdateDialog: () -> Unit,
    onNavigateToCharacterList: () -> Unit,
) {
    val character: Character by remember { derivedStateOf { viewModel.getCharacterById(characterId)!! } }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(onClick = onNavigateToCharacterList) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text(text = "Back")
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(2.dp, Color.LightGray),
                    RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 8.dp, vertical = 16.dp),
        ) {
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

            Column() {
                Text(
                    character.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                )
                Text(
                    character.race,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                )
            }

            Text(
                "${character.dndClass} (${character.level})",
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onOpenCharacterUpdateDialog() },
                modifier = Modifier.weight(1f),
            ) {
                Row (
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text(text = "Edit")
                }

            }
            Button(
                onClick = onNavigateToItemList,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Text(text = "Edit Inventory", softWrap = false)
                }
            }
        }
        Column {
            Text(
                "Inventory",
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                color = MaterialTheme.typography.labelLarge.color
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(character.inventory) { item ->
                    ItemRow(
                        item = item,
                        onLongClick = {},
                    )
                }
            }
        }
    }
}
