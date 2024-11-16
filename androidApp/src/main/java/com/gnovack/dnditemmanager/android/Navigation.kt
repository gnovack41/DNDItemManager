package com.gnovack.dnditemmanager.android

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gnovack.dnditemmanager.android.views.characters.CharacterListView
import com.gnovack.dnditemmanager.android.views.inventory.ItemListView
import kotlinx.serialization.Serializable


@Serializable
data class ItemList(val characterId: String = "TEMP")

@Serializable
object CharacterList


@Composable
fun DNDNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = CharacterList,
    ) {
        composable<ItemList> { backStackEntry ->
            val itemList: ItemList = backStackEntry.toRoute()
            ItemListView(characterId = itemList.characterId)
        }
        composable<CharacterList> { CharacterListView(onNavigateToItemList = { navController.navigate(ItemList(it))}) }
    }
}
