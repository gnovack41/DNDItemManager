package com.gnovack.dnditemmanager.android

import android.content.Context
import android.content.res.AssetManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun <T> T.useDebounce(
    delayMillis: Long = 300L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit,
): T {
    val state by rememberUpdatedState(this)

    DisposableEffect(state) {
        val job = coroutineScope.launch {
            delay(delayMillis)
            onChange(state)
        }
        onDispose {
            job.cancel()
        }
    }

    return state
}


class FormField<T>(value: T, rule: (T) -> Boolean) {
    val isValid = rule(value)
}

fun AssetManager.readAssetsFileAsString(fileName: String): String = open(fileName).bufferedReader().use {
    it.readText()
}

inline fun <reified T> getObjectFromJsonAssetFile(context: Context, fileName: String): T {
    val jsonString = context.assets.readAssetsFileAsString(fileName)
    return Json.decodeFromString<T>(jsonString)
}
