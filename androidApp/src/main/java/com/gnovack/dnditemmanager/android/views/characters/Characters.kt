package com.gnovack.dnditemmanager.android.views.characters

import kotlinx.serialization.Serializable

@Serializable
data class Character(var id: Int? = null, val name: String, val dndClass: String, val level: Int)
