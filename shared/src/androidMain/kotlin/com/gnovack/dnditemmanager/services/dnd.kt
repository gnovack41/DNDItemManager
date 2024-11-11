package com.gnovack.dnditemmanager.services

import io.ktor.client.engine.cio.CIO

actual val platformEngine = CIO.create()
