package com.gnovack.dnditemmanager.android.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
fun RoundedTextField(
    modifier: Modifier = Modifier,
    value: String,
    supportingText: String? = null,
    name: String? = null,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { if (name != null) Text(text = name) },
        supportingText = { if (supportingText != null) Text(text = supportingText) },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        isError = isError,
        modifier = modifier,
    )
}

@Composable
fun SelectInput(
    modifier: Modifier = Modifier,
    name: String,
    value: String,
    options: List<String>,
    enabled: Boolean = true,
    nullOption: String? = null,
    isError: Boolean = false,
    onOptionSelected: (String?) -> Unit,
    supportingText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedSelectedName = value.replace('_', ' ').replaceFirstChar(Char::titlecaseChar)

    val focusManager = LocalFocusManager.current

    Box(modifier = modifier) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { expanded = it.isFocused },
            readOnly = true,
            value = formattedSelectedName,
            onValueChange = {},
            label = { Text(text = name) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            enabled = enabled,
            isError = isError,
            supportingText = { if (supportingText != null) Text(text = supportingText) },
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp),
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Menu Dropdown Icon")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = {
            expanded = false
            focusManager.clearFocus()
        }) {
            if (nullOption != null) DropdownMenuItem(text = { Text(nullOption) }, onClick = {
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