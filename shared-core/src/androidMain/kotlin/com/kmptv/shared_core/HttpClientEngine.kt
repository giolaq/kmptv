package com.kmptv.shared_core

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

internal actual fun platformEngine(): HttpClientEngineFactory<*> = OkHttp
