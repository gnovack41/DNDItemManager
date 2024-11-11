package com.gnovack.dnditemmanager.services

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual val platformEngine: HttpClientEngine = Darwin.create()
