package com.kmptv.shared_core

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

internal actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin
