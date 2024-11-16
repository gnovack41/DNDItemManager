package com.gnovack.dnditemmanager.android.views.inventory

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnovack.dnditemmanager.android.components.FilterDropDown
import com.gnovack.dnditemmanager.android.components.ItemRow
import com.gnovack.dnditemmanager.android.useDebounce
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.services.Item

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemListView(dndViewModel: DNDApiViewModel = viewModel(), characterId: String? = null) {
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
        ItemSearchBar(itemsLoading = itemsLoading, filtersLoading = filtersLoading, filterOptions = dndUiState.filterOptions) {
            search, rarity, source ->
            dndViewModel.loadItems(search, rarity, source)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSearchBar(itemsLoading: Boolean, filtersLoading: Boolean, filterOptions: Map<String, List<String>>, onQueryChange: (String?, String?, String?) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        var selectedRarity by remember { mutableStateOf<String?>(null) }
        var selectedSource by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }

        val isLoading = itemsLoading || filtersLoading

        searchQuery.useDebounce { onQueryChange(it, selectedRarity, selectedSource) }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterDropDown(
                modifier = Modifier.weight(1f),
                name = "Rarity",
                options = filterOptions["rarities"] ?: emptyList(),
                onOptionSelected = {
                    selectedRarity = it
                    onQueryChange(searchQuery, selectedRarity, selectedSource)
                },
                enabled = !isLoading,
            )
            FilterDropDown(
                modifier = Modifier.weight(1f),
                name = "Source",
                options = filterOptions["sources"] ?: emptyList(),
                onOptionSelected = {
                    selectedSource = it
                    onQueryChange(searchQuery, selectedRarity, selectedSource)
                },
                enabled = !isLoading,
            )
        }
        Row {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { onQueryChange(searchQuery, selectedRarity, selectedSource) },
                        expanded = false,
                        onExpandedChange = {},
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text(text = "Search") },
                        enabled = !filtersLoading,
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.weight(1f),
                content = {},
            )
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
