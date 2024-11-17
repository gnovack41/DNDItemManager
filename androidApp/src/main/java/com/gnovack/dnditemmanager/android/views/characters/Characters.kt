package com.gnovack.dnditemmanager.android.views.characters

import com.gnovack.dnditemmanager.android.FormField
import com.gnovack.dnditemmanager.services.Item
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class Character(
    var id: Int? = null,
    val name: String,
    val race: String,
    val dndClass: String,
    val level: Int,
    var inventory: List<Item> = listOf()
) {
    @Transient
    val nameField = FormField(name) {
        value -> value.isNotBlank()
    }
    @Transient
    val raceField = FormField(race) {
        value -> value.isNotBlank()
    }
    @Transient
    val classField = FormField(dndClass) {
        value -> value.isNotBlank()
    }
    @Transient
    val levelField = FormField(level) {
        value -> value in 1..20
    }
    @Transient
    val isValid = listOf(
        nameField,
        raceField,
        classField,
        levelField,
    ).all { it.isValid }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Character

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
