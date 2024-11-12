package com.gnovack.dnditemmanager.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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

    if (!doneFirstLoad) {
        dndViewModel.loadItems { doneFirstLoad = true }
    }

    if (dndUiState.filterOptions.isEmpty()) {
        dndViewModel.loadFilterOptions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
        ) {
            var selectedRarity by remember { mutableStateOf<String?>(null) }
            var selectedSource by remember { mutableStateOf<String?>(null) }
            var searchQuery by remember { mutableStateOf("") }


            if (dndUiState.filterOptions.isNotEmpty()) {
                searchQuery.useDebounce {
                    dndViewModel.loadItems(search = it, rarity = selectedRarity, source = selectedSource)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterDropDown(
                        modifier = Modifier.weight(1f),
                        name = "Rarity",
                        options = dndUiState.filterOptions["rarities"] ?: emptyList(),
                        onOptionSelected = {
                            selectedRarity = it
                            dndViewModel.loadItems(search = searchQuery, rarity = it, source = selectedSource)
                        },
                        enabled = !itemsLoading,
                    )
                    FilterDropDown(
                        modifier = Modifier.weight(1f),
                        name = "Source",
                        options = dndUiState.filterOptions["sources"] ?: emptyList(),
                        onOptionSelected = {
                            selectedSource = it
                            dndViewModel.loadItems(search = searchQuery, rarity = selectedRarity, source = it)
                        },
                        enabled = !itemsLoading,
                    )
                }
                Row {
                    SearchBar(
                        inputField = { SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = {
                                dndViewModel.loadItems(
                                    search = searchQuery,
                                    rarity = selectedRarity,
                                    source = selectedSource,
                                )
                            },
                            enabled = !itemsLoading,
                            expanded = false,
                            onExpandedChange = {},
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            placeholder = { Text(text = "Search") },
                        ) },
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier.weight(1f),
                    ) {}
                }
            }
        }

        if (itemsLoading || filtersLoading) {
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
        modifier = Modifier.shadow(16.dp)
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { item: Item ->
                ItemRow(
                    item,
                    onLongClick = {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemRow(item: Item, selected: Boolean = false, onLongClick: (Item) -> Unit) {
    val context = LocalContext.current

    Surface(
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.combinedClickable(
            onClick = { Toast.makeText(context, "Open ${item.name}", Toast.LENGTH_SHORT).show() },
            onLongClick = { onLongClick(item) }
        )
    ) {
        ListItem(
            headlineContent = { Text(text = item.name ?: "Unknown", fontWeight = FontWeight.Bold) },
            leadingContent = {
                Column(
                    Modifier
                        .height(70.dp)
                        .width(70.dp), verticalArrangement = Arrangement.Center) {
                    if (item.imageUrl != null) AsyncImage(
                        model = item.imageUrl ?: "https://t3.ftcdn.net/jpg/04/60/01/36/360_F_460013622_6xF8uN6ubMvLx0tAJECBHfKPoNOR5cRa.jpg",
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxHeight()
                    ) else Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxSize()
                            .background(Color.LightGray)
                            .padding(16.dp)
                    )
                }
            },
            trailingContent = {
                Column(
                    modifier = Modifier.height(70.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = item.rarity?.replace('_', ' ')?.replaceFirstChar(Char::titlecaseChar) ?: "Unknown")
                    Text(text = item.source?.replace('-', ' ')?.replaceFirstChar(Char::titlecaseChar) ?: "Unknown")
                }
            },
            supportingContent = { if (item.description != null) Text(
                text = item.description!!,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            ) },
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

    val formattedSelectedName = selectedOption.replace('_', ' ').replaceFirstChar(Char::titlecaseChar)

    val focusManager = LocalFocusManager.current

    Box(modifier = modifier) {
        TextField(
            modifier = Modifier.onFocusChanged { expanded = it.isFocused },
            readOnly = true,
            value = formattedSelectedName,
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
                DropdownMenuItem(
                    text = { Text(option.replace('_', ' ').replaceFirstChar(Char::titlecaseChar)) },
                    onClick = {
                        selectedOption = option
                        onOptionSelected(option)
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
