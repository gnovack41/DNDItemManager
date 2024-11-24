package com.gnovack.dnditemmanager.android.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gnovack.dnditemmanager.android.rarityToDisplayName
import com.gnovack.dnditemmanager.services.Item

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemRow(
    item: Item,
    selected: Boolean = false,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        border = BorderStroke(
            if (selected) 3.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else Color.LightGray
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.combinedClickable(
            onClick = { onClick() },
            onLongClick = { onLongClick() }
        )
    ) {
        ListItem(
            headlineContent = { Text(text = item.name ?: "Unknown", fontWeight = FontWeight.Bold) },
            leadingContent = {
                Column(
                    Modifier
                        .height(70.dp)
                        .width(70.dp), verticalArrangement = Arrangement.Center
                ) {
                    if (item.imageUrl != null) AsyncImage(
                        model = item.imageUrl,
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
                            .padding(16.dp)
                    )
                }
            },
            trailingContent = {
                Column(
                    modifier = Modifier.height(70.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = rarityToDisplayName(item.rarity!!))
                    Text(text = item.source ?: "Unknown")
                }
            },
            supportingContent = {
                if (item.description != null) Text(
                    text = item.description!!,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}