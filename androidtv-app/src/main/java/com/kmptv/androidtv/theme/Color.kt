package com.kmptv.androidtv.theme

import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for the palette used outside `MaterialTheme`.
 *
 * Raw `Color(0xFF...)` literals were previously scattered across every
 * Compose screen. Pull from this object instead.
 */
object KmptvColors {
    /** Primary page background — a near-black to avoid LCD haloing on TVs. */
    val Background = Color(0xFF0D0D0D)

    /** Panels sitting slightly above the background. */
    val SurfaceElevated = Color(0xFF1A1A1A)

    /** Unfocused button / control fill. */
    val SurfaceFocus = Color(0xFF2A2A2A)

    /** Seek-bar / progress accent (Netflix-red). */
    val Accent = Color(0xFFE50914)
}
