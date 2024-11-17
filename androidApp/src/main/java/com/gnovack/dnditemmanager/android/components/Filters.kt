package com.gnovack.dnditemmanager.android.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@Composable
fun FilterDropDown(
    modifier: Modifier = Modifier,
    name: String,
    value: String,
    options: List<String>,
    enabled: Boolean = true,
    onOptionSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedSelectedName = value.replace('_', ' ').replaceFirstChar(Char::titlecaseChar)

    val focusManager = LocalFocusManager.current

    Box(modifier = modifier) {
        TextField(
            modifier = Modifier.onFocusChanged { expanded = it.isFocused },
            readOnly = true,
            value = formattedSelectedName,
            onValueChange = {},
            label = { Text(text = name) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            enabled = enabled,
        )
        IconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Menu Dropdown Icon")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = {
            expanded = false
            focusManager.clearFocus()
        }) {
            DropdownMenuItem(text = { Text("Any") }, onClick = {
                onOptionSelected(null)
                expanded = false
                focusManager.clearFocus()
            })
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.replace('_', ' ').replaceFirstChar(Char::titlecaseChar)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
