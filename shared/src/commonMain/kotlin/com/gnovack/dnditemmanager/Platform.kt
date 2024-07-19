package com.gnovack.dnditemmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform