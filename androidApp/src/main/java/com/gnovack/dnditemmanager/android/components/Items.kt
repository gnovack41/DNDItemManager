package com.gnovack.dnditemmanager.android.components

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gnovack.dnditemmanager.services.Item

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemRow(item: Item, selected: Boolean = false, onLongClick: (Item) -> Unit) {
    val context = LocalContext.current

    Surface(
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.combinedClickable(
            onClick = { Toast.makeText(context, "Open ${item.name}", Toast.LENGTH_SHORT).show() },
            onLongClick = { onLongClick(item) }
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
                        model = item.imageUrl
                            ?: "https://t3.ftcdn.net/jpg/04/60/01/36/360_F_460013622_6xF8uN6ubMvLx0tAJECBHfKPoNOR5cRa.jpg",
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
                    Text(text = item.rarity?.replace('_', ' ')?.replaceFirstChar(Char::titlecaseChar) ?: "Unknown")
                    Text(text = item.source?.replace('-', ' ')?.replaceFirstChar(Char::titlecaseChar) ?: "Unknown")
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