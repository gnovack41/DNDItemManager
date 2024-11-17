package com.gnovack.dnditemmanager.android.views.characters

import com.gnovack.dnditemmanager.android.FormField
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
data class Character(
    var id: Int? = null,
    val name: String,
    val dndClass: String,
    val level: Int,
) {
    @Contextual
    val nameField = FormField(name) {
        value -> value.isNotBlank()
    }
    @Contextual
    val classField = FormField(dndClass) {
        value -> value.isNotBlank()
    }
    @Contextual
    val levelField = FormField(level) {
        value -> value in 1..20
    }

    val isValid = nameField.isValid && classField.isValid && levelField.isValid
}
