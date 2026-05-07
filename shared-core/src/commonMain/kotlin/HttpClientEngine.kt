package com.kmptv.shared_core

import io.ktor.client.engine.*

internal expect fun platformEngine(): HttpClientEngineFactory<*>
