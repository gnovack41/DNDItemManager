package com.gnovack.dnditemmanager.services


class FormField<T>(value: T, rule: (T) -> Boolean) {
    val isValid = rule(value)
}
