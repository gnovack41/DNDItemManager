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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnovack.dnditemmanager.android.components.ItemRow
import com.gnovack.dnditemmanager.android.components.SelectInput
import com.gnovack.dnditemmanager.android.useDebounce
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.services.Item

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemListView(
    viewModel: DNDApiViewModel = viewModel(),
    characterId: String,
    onNavigateToCharacterList: () -> Unit,
    onNavigateToItemDetails: (Item) -> Unit,
) {
    val currentCharacter by remember { derivedStateOf {
        viewModel.getCharacterById(characterId)!!
    } }

    val itemAsyncStateHandler by remember { derivedStateOf {
        viewModel.useAsyncUiState { args: List<String?> ->
            val search: String? = args.getOrNull(0)
            val rarity: String? = args.getOrNull(1)
            val source: String? = args.getOrNull(2)

            viewModel.client.getItems(search = search, rarity = rarity, source = source)
        }
    } }

    val itemsFilterAsyncStateHandler by remember { derivedStateOf {
        viewModel.useAsyncUiState<Nothing, Map<String, List<String>>> {
            mapOf("sources" to viewModel.client.getSources(), "rarities" to viewModel.client.getRarities())
        }
    } }

    val itemsRequestState by itemAsyncStateHandler.uiState.collectAsState()
    val itemFiltersRequestState by itemsFilterAsyncStateHandler.uiState.collectAsState()

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
            filterOptions = itemFiltersRequestState.data ?: emptyMap(),
            loadFilters = { itemsFilterAsyncStateHandler.executeRequest() }
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
                    viewModel.addItemsToCharacterInventory(characterId, newItems)
                    onNavigateToCharacterList()
                },
                onItemClick = { item -> onNavigateToItemDetails(item) }
            )
        } else if (itemsRequestState.isFailed) {
            Text(text = "An error has occurred")
            Button(onClick = { itemAsyncStateHandler.executeRequest() }) {
                Row (
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text(text = "Retry")
                }
            }
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
    loadFilters: () -> Unit,
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

        var debouncedSearch by rememberSaveable { mutableStateOf<String?>(null) }
        searchQuery.useDebounce { debouncedSearch = it }

        LaunchedEffect(Unit) { loadFilters() }

        LaunchedEffect(debouncedSearch, selectedRarity, selectedSource) {
            onQueryChange(debouncedSearch, selectedRarity, selectedSource)
        }

        val isLoading = itemsLoading || filtersLoading

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectInput(
                modifier = Modifier.weight(1f),
                name = "Rarity",
                value = selectedRarity ?: "",
                options = filterOptions["rarities"] ?: emptyList(),
                onOptionSelected = { selectedRarity = it },
                enabled = !isLoading,
            )
            SelectInput(
                modifier = Modifier.weight(1f),
                name = "Source",
                value = selectedSource ?: "",
                options = filterOptions["sources"] ?: emptyList(),
                onOptionSelected = { selectedSource = it },
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
    onItemClick: (Item) -> Unit,
) {
    var selectedItems by rememberSaveable { mutableStateOf(existingInventory.toSet()) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "Update Inventory") },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                onClick = { onAddToInventory(selectedItems.toList()) },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { item: Item ->
                ItemRow(
                    item,
                    onClick = { onItemClick(item) },
                    onLongClick = {
                        if (item in selectedItems) {
                            selectedItems -= item
                        } else {
                            selectedItems += item
                        }
                    },
                    selected = item in selectedItems,
                )
            }
        }
    }
}
