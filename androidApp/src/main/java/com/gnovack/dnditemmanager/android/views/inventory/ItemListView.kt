package com.gnovack.dnditemmanager.android.views.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnovack.dnditemmanager.android.components.FilterDropDown
import com.gnovack.dnditemmanager.android.components.ItemRow
import com.gnovack.dnditemmanager.android.useDebounce
import com.gnovack.dnditemmanager.android.viewmodels.AsyncStateHandler
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.android.views.characters.Character
import com.gnovack.dnditemmanager.services.Item

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemListView(
    viewModel: DNDApiViewModel = viewModel(),
    currentCharacter: Character,
    itemAsyncStateHandler: AsyncStateHandler<String?, List<Item>>,
    itemsFilterAsyncStateHandler: AsyncStateHandler<Any?, Map<String, List<String>>>,
    onNavigateToCharacterList: () -> Unit,
) {
    val itemsRequestState by itemAsyncStateHandler.uiState.collectAsState()
    val itemFiltersRequestState by itemsFilterAsyncStateHandler.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        itemAsyncStateHandler.executeRequest()
        itemsFilterAsyncStateHandler.executeRequest()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row (
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth(),
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
        }
        ItemSearchBar(
            itemsLoading = itemsRequestState.isLoading,
            filtersLoading = itemFiltersRequestState.isLoading,
            filterOptions = itemFiltersRequestState.data ?: emptyMap()
        ) {
            search, rarity, source ->
            itemAsyncStateHandler.executeRequest(search, rarity, source)
        }

        if (itemsRequestState.isLoading || itemFiltersRequestState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )
            Text(text = "Loading...")
        } else if (itemsRequestState.data?.isNotEmpty() == true) {
            ItemList(
                items = itemsRequestState.data!!,
                existingInventory = currentCharacter.inventory,
                onAddToInventory = { newItems ->
                    viewModel.addItemsToSelectedCharacterInventory(newItems)
                    onNavigateToCharacterList()
                },
            )
        } else if (itemsRequestState.isFailed) {
            Text(text = "Error: ${itemsRequestState.error!!.message}")
        } else if (itemsRequestState.isSuccessful) {
            Text(text = "No items found")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSearchBar(
    itemsLoading: Boolean,
    filtersLoading: Boolean,
    filterOptions: Map<String, List<String>>,
    onQueryChange: (String?, String?, String?) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        var selectedRarity by rememberSaveable { mutableStateOf<String?>(null) }
        var selectedSource by rememberSaveable { mutableStateOf<String?>(null) }
        var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }

        val isLoading = itemsLoading || filtersLoading

        searchQuery.useDebounce { if (it != null) onQueryChange(it, selectedRarity, selectedSource) }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterDropDown(
                modifier = Modifier.weight(1f),
                name = "Rarity",
                value = selectedRarity ?: "",
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
                value = selectedSource ?: "",
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
                        query = searchQuery ?: "",
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
fun ItemList(
    items: List<Item>,
    existingInventory: List<Item>,
    onAddToInventory: (List<Item>) -> Unit,
) {
    var selectedItems by rememberSaveable { mutableStateOf(existingInventory.toSet()) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "Update Inventory") },
                icon = { Icon(Icons.Default.Edit, contentDescription = "Test") },
                shape = RoundedCornerShape(16.dp),
                onClick = { onAddToInventory(selectedItems.toList()) },
            )
        },
        modifier = Modifier.shadow(16.dp)
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { item: Item ->
                ItemRow(
                    item,
                    onLongClick = {
                        if (it in selectedItems) {
                            selectedItems -= it
                        } else {
                            selectedItems += it
                        }
                    },
                    selected = item in selectedItems,
                )
            }
        }
    }
}
