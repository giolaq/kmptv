package com.kmptv.shared_core.models

import kotlin.random.Random

/**
 * Creates unique identifiers for sessions, applications, and devices.
 *
 * All IDs follow the shape `"<prefix>_<epoch-millis>_<random>"` so they remain
 * unique enough for local/in-memory use and can be grepped out of logs.
 */
internal object IdGenerator {
    fun sessionId(): String = id("session", randomMax = 10_000)
    fun applicationId(): String = "kmptv_${nowMillis()}"
    fun deviceId(): String = id("device", randomMax = 1_000_000)

    private fun id(prefix: String, randomMax: Int): String =
        "${prefix}_${nowMillis()}_${Random.nextInt(randomMax)}"
}
