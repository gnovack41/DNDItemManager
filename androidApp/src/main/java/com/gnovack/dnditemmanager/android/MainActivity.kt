package com.gnovack.dnditemmanager.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.services.Item

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dndViewModel = DNDApiViewModel()

        setContent {
            MyApplicationTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    InventoryView(dndViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InventoryView(dndViewModel: DNDApiViewModel = DNDApiViewModel()) {
    val dndUiState by dndViewModel.uiState.collectAsState()
    val itemsLoading by dndViewModel.itemsLoading.collectAsState()
    val filtersLoading by dndViewModel.filtersLoading.collectAsState()

    var doneFirstLoad by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var selectedRarity by remember { mutableStateOf<String?>(null) }
            var selectedSource by remember { mutableStateOf<String?>(null) }

            if (dndUiState.filterOptions.isEmpty()) {
                dndViewModel.loadFilterOptions()
                if (filtersLoading){
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface,
                    )
                    Text(text = "Loading...")
                }
            }
            else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterDropDown(
                        modifier = Modifier.weight(1f),
                        name = "Rarity",
                        options = dndUiState.filterOptions["rarities"] ?: emptyList(),
                        onOptionSelected = { selectedRarity = it },
                        enabled = !itemsLoading,
                    )
                    FilterDropDown(
                        modifier = Modifier.weight(1f),
                        name = "Source",
                        options = dndUiState.filterOptions["sources"] ?: emptyList(),
                        onOptionSelected = { selectedSource = it },
                        enabled = !itemsLoading,
                    )
                }
                Row {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            dndViewModel.loadItems(rarity = selectedRarity, source = selectedSource)
                            doneFirstLoad = true
                        },
                        enabled = !itemsLoading,
                    ) {
                        Text(text = "Load Items", softWrap = false)
                    }
                }
            }
        }

        if (itemsLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )
            Text(text = "Loading...")
        } else if (dndUiState.items.isNotEmpty()) {
            ItemList(items = dndUiState.items)
        } else if (dndUiState.error != null) {
            Text(text = "Error: ${dndUiState.error}")
        } else if (doneFirstLoad) {
            Text(text = "No items found")
        }
    }
}

@Composable
fun ItemList(items: List<Item>) {
    var selectedItemIds by remember { mutableStateOf<Set<String>>(mutableSetOf()) }

    Scaffold(
        floatingActionButton = {
            if (selectedItemIds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Add ${selectedItemIds.size} Item${if (selectedItemIds.size > 1) "s" else ""} to Inventory") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Test") },
                    shape = RoundedCornerShape(16.dp),
                    onClick = { /*TODO*/ },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { item: Item ->
                ItemRow(
                    item,
                    onClick = {
                        if (it.id in selectedItemIds) {
                            selectedItemIds -= it.id
                        } else {
                            selectedItemIds += it.id
                        }
                    },
                    selected = item.id in selectedItemIds,
                )
            }
        }
    }
}

@Composable
fun ItemRow(item: Item, selected: Boolean = false, onClick: (Item) -> Unit) {
    Surface(
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp,
        onClick = { onClick(item) },
    ) {
        ListItem(
            headlineContent = { Text(text = item.name ?: "Unknown", fontWeight = FontWeight.Bold) },
            leadingContent = { Text(text = item.rarity ?: "Unknown") },
            trailingContent = { Text(text = item.source ?: "Unknown") },
        )
    }
}

@Composable
fun FilterDropDown(
    modifier: Modifier = Modifier,
    name: String,
    options: List<String>,
    enabled: Boolean = true,
    onOptionSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Box(modifier = modifier) {
        TextField(
            modifier = Modifier.onFocusChanged { expanded = it.isFocused },
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text(text = name) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            enabled = enabled,
        )
        IconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Menu Dropdown Icon")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = {
            expanded = false
            focusManager.clearFocus()
        }) {
            DropdownMenuItem(text = { Text("Any") }, onClick = {
                selectedOption = ""
                onOptionSelected(null)
                expanded = false
                focusManager.clearFocus()
            })
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    selectedOption = option
                    onOptionSelected(option)
                    expanded = false
                    focusManager.clearFocus()
                })
            }
        }
    }
}
