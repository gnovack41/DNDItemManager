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
            ItemListView(
                viewModel = viewModel,
                currentCharacter = viewModel.selectedCharacter!!,
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
