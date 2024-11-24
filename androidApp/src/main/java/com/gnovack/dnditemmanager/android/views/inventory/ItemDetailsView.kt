package com.gnovack.dnditemmanager.android.views.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.gnovack.dnditemmanager.android.rarityToDisplayName
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel
import com.gnovack.dnditemmanager.services.Item


@Composable
fun ItemDetailsView(
    viewModel: DNDApiViewModel = viewModel(),
    itemId: String,
    onNavigateBack: () -> Unit,
) {
    val getItemAsyncStateHandler by remember { derivedStateOf {
        viewModel.useAsyncUiState<Nothing, Item> { viewModel.client.getItem(itemId) }
    } }

    val itemRequestState by getItemAsyncStateHandler.uiState.collectAsState()

    val item by remember { derivedStateOf { itemRequestState.data }}

    LaunchedEffect(Unit) { getItemAsyncStateHandler.executeRequest() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onNavigateBack) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Text(text = "Back")
                }
            }
        }

        if (itemRequestState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )
            Text(text = "Loading...")
        } else if (itemRequestState.isFailed) {
            Text(text = "An error has occurred")
            Button(onClick = { getItemAsyncStateHandler.executeRequest() }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text(text = "Retry")
                }
            }
        } else if (itemRequestState.isSuccessful) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(2.dp, Color.LightGray),
                        RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .height(70.dp)
                        .width(70.dp)
                ) {
                    if (item?.imageUrl != null) AsyncImage(
                        model = item!!.imageUrl,
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
                            .padding(16.dp),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(.70f),
                ) {
                    Text(
                        item!!.name!!,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                        lineHeight = MaterialTheme.typography.headlineLarge.lineHeight,
                    )
                    Text(
                        rarityToDisplayName(item!!.rarity!!),
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    )
                }

                Column {
                    Text(
                        item!!.source!!,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    )
                }

            }

        }
        Text(text = item?.description ?: "")
    }
}