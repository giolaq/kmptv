package com.kmptv.shared_core.models

import kotlinx.datetime.Clock

/**
 * Returns the current wall-clock time in milliseconds since the Unix epoch.
 *
 * Single multiplatform-safe accessor so the rest of the codebase never has to
 * reach for `System.currentTimeMillis()` (JVM-only) or Apple/Foundation APIs.
 */
internal fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
