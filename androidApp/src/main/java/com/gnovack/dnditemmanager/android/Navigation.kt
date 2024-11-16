package com.gnovack.dnditemmanager.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.android.views.characters.CharacterListView
import com.gnovack.dnditemmanager.android.views.inventory.ItemListView
import kotlinx.serialization.Serializable


@Serializable
data class ItemList(val characterId: String = "TEMP")

@Serializable
object CharacterList


@Composable
fun DNDNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: DNDApiViewModel = viewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = CharacterList,
    ) {
        composable<ItemList> { backStackEntry ->
            val itemList: ItemList = backStackEntry.toRoute()

            val itemAsyncStateHandler by remember {
                derivedStateOf { viewModel.useAsyncUiState { args: List<String?> ->
                    val search: String? = args.getOrNull(0)
                    val rarity: String? = args.getOrNull(1)
                    val source: String? = args.getOrNull(2)

                    viewModel.client.getItems(search = search, rarity = rarity, source = source)
                } }
            }

            val itemFilterAsyncStateHandler by remember {
                derivedStateOf { viewModel.useAsyncUiState<Any?, Map<String, List<String>>> {
                    mapOf("sources" to viewModel.client.getSources(), "rarities" to viewModel.client.getRarities())
                } }
            }

            ItemListView(
                characterId = itemList.characterId,
                itemAsyncStateHandler = itemAsyncStateHandler,
                itemsFilterAsyncStateHandler = itemFilterAsyncStateHandler,
            )
        }

        composable<CharacterList> { CharacterListView(onNavigateToItemList = { navController.navigate(ItemList(it))}) }
    }
}
