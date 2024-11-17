package com.gnovack.dnditemmanager.android.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RoundedTextField(
    modifier: Modifier = Modifier,
    value: String,
    name: String? = null,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { if (name != null) Text(text = name) },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        modifier = modifier,
    )
}