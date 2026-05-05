package com.kmptv.shared_core.models

/**
 * Single source of truth for session timeouts so they cannot drift between
 * `SessionManagerImpl` and `TVApplicationManagerImpl`.
 */
internal object SessionConstants {
    /** 30 minutes — used for guest / unauthenticated sessions. */
    const val GUEST_TIMEOUT_MS: Long = 30 * 60 * 1000L

    /** 2 hours — used for authenticated sessions. */
    const val AUTHENTICATED_TIMEOUT_MS: Long = 2 * 60 * 60 * 1000L
}
