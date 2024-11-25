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

fun AssetManager.readAssetsFileAsString(fileName: String): String = open(fileName).bufferedReader().use {
    it.readText()
}

inline fun <reified T> getObjectFromJsonAssetFile(context: Context, fileName: String): T {
    val jsonString = context.assets.readAssetsFileAsString(fileName)
    return Json.decodeFromString<T>(jsonString)
}


private val RARITY_TO_DISPLAY_NAME = mapOf(
    Pair("common", "Common"),
    Pair("uncommon", "Uncommon"),
    Pair("rare", "Rare"),
    Pair("very_rare", "Very Rare"),
    Pair("legendary", "Legendary"),
    Pair("artifact", "Artifact"),
    Pair("varies", "Varies"),
)

fun rarityToDisplayName(rarity: String): String = RARITY_TO_DISPLAY_NAME.getOrDefault(rarity, "Unknown")

fun parseDndBeyondCharacterIdFromUrl(url: String): String {
    val urlParts = url.replace(" ", "").split("/")
    val charactersIndex = urlParts.indexOf("characters")

    if (urlParts.size < 2 || charactersIndex == -1) return ""

    return urlParts[charactersIndex + 1]
}

