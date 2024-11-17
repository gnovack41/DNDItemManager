package com.gnovack.dnditemmanager.android

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.android.views.characters.CharacterCreateDialog
import com.gnovack.dnditemmanager.android.views.characters.CharacterListView
import com.gnovack.dnditemmanager.android.views.inventory.ItemListView
import kotlinx.serialization.Serializable


@Serializable
object ItemList


@Serializable
object CharacterList


@Serializable
object CharacterCreate


@Composable
fun DNDNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: DNDApiViewModel = viewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = CharacterList,
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        modifier = modifier,
    ) {
        composable<ItemList> {
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
                viewModel = viewModel,
                currentCharacter = viewModel.selectedCharacter!!,
                itemAsyncStateHandler = itemAsyncStateHandler,
                itemsFilterAsyncStateHandler = itemFilterAsyncStateHandler,
                onNavigateToCharacterList = {
                    navController.navigate(CharacterList)
                }
            )
        }

        composable<CharacterList> {
            CharacterListView(
                viewModel = viewModel,
                onNavigateToItemList = { character ->
                    viewModel.setSelectedCharacter(character)
                    navController.navigate(ItemList)
                },
                onOpenCharacterCreateDialog = {
                    navController.navigate(CharacterCreate)
                }
            )
        }

        dialog<CharacterCreate> {
            CharacterCreateDialog(
                closeDialog = { navController.navigate(CharacterList) },
                onSubmit = { character ->
                    viewModel.addCharacter(character)
                    navController.navigate(CharacterList)
                }
            )
        }
    }
}
