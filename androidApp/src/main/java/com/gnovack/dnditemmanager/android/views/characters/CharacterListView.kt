package com.gnovack.dnditemmanager.android.views.characters

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel

@Composable
fun CharacterListView(dndViewModel: DNDApiViewModel = viewModel(), onNavigateToItemList: (String) -> Unit) {
    Button(onClick = { onNavigateToItemList("TESTCHARACTER") }) {
        Text(text = "Go To Item List")
    }
}
