package com.gnovack.dnditemmanager.android

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.android.views.characters.CharacterCreateOrUpdateDialog
import com.gnovack.dnditemmanager.android.views.characters.CharacterDetailsView
import com.gnovack.dnditemmanager.android.views.characters.CharacterImportDialog
import com.gnovack.dnditemmanager.android.views.characters.CharacterListView
import com.gnovack.dnditemmanager.android.views.inventory.ItemDetailsView
import com.gnovack.dnditemmanager.android.views.inventory.ItemListView
import kotlinx.serialization.Serializable


@Serializable
object CharacterList


@Serializable
data class CharacterDetails(val characterId: String)


@Serializable
data class ItemList(val characterId: String)


@Serializable
data class CharacterCreateOrUpdate(val characterId: String? = null)

@Serializable
object CharacterImport

@Serializable
data class ItemDetails(val itemId: String)


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
        composable<CharacterList> {
            CharacterListView(
                viewModel = viewModel,
                onNavigateToCharacterDetails = { characterId ->
                    navController.navigate(CharacterDetails(characterId))
                },
                onOpenCharacterCreateDialog = {
                    navController.navigate(CharacterCreateOrUpdate())
                },
                onOpenCharacterImportDialog = {
                    navController.navigate(CharacterImport)
                }
            )
        }

        composable<CharacterDetails> { backStackEntry ->
            val characterId = backStackEntry.toRoute<CharacterDetails>().characterId

            CharacterDetailsView(
                viewModel = viewModel,
                characterId = characterId,
                onNavigateToItemList = {
                    navController.navigate(ItemList(characterId))
                },
                onOpenCharacterUpdateDialog = {
                    navController.navigate(CharacterCreateOrUpdate(characterId))
                },
                onNavigateToCharacterList = {
                    navController.navigate(CharacterList)
                },
                onNavigateToItemDetails = { item ->
                    navController.navigate(ItemDetails(item.id))
                }
            )
        }

        composable<ItemList> { backStackEntry ->
            val characterId = backStackEntry.toRoute<ItemList>().characterId

            ItemListView(
                viewModel = viewModel,
                characterId = characterId,
                onNavigateToCharacterList = {
                    navController.navigate(CharacterDetails(characterId))
                },
                onNavigateToItemDetails = { item ->
                    navController.navigate(ItemDetails(item.id))
                }
            )
        }

        composable<ItemDetails> { backStackEntry ->
            ItemDetailsView(
                itemId = backStackEntry.toRoute<ItemDetails>().itemId,
                onNavigateBack = { navController.navigateUp() },
            )
        }

        dialog<CharacterCreateOrUpdate> { backStackEntry ->
            CharacterCreateOrUpdateDialog(
                viewModel = viewModel,
                characterId = backStackEntry.toRoute<CharacterCreateOrUpdate>().characterId,
                closeDialog = { navController.navigateUp() },
                onSubmit = { character ->
                    val updatedCharacter = viewModel.updateOrCreateCharacter(character)
                    navController.navigate(CharacterDetails(updatedCharacter.id!!))
                }
            )
        }

        dialog<CharacterImport> {
            CharacterImportDialog(
                viewModel = viewModel,
                closeDialog = { navController.navigateUp() },
                onImportComplete = { character ->
                    val updatedCharacter = viewModel.updateOrCreateCharacter(character)
                    navController.navigate(CharacterDetails(updatedCharacter.id!!))
                }
            )
        }
    }
}
