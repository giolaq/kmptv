package com.kmptv.androidtv.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ClickableSurfaceColors
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api

/**
 * Animates a subtle grow-on-focus scale for any TV element. Replaces the
 * copy-pasted `focused/mutableStateOf/animateFloatAsState/scale/onFocusChanged`
 * block that previously lived in every card and button.
 *
 * @param focusedScale the target scale when focused (defaults to 1.05).
 * @param durationMs   the tween duration, in milliseconds (defaults to 150).
 * @param onFocus      optional side effect fired each time focus is gained.
 */
@Composable
fun Modifier.tvFocusScale(
    focusedScale: Float = 1.05f,
    durationMs: Int = 150,
    onFocus: (() -> Unit)? = null,
): Modifier {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) focusedScale else 1f,
        animationSpec = tween(durationMs),
        label = "tvFocusScale",
    )
    return this
        .scale(scale)
        .onFocusChanged { state ->
            focused = state.isFocused
            if (state.isFocused) onFocus?.invoke()
        }
}

/**
 * The transparent `ClickableSurfaceColors` preset that every TV card / button
 * was constructing by hand. Consolidated here so the call sites read cleanly.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun transparentSurfaceColors(): ClickableSurfaceColors = ClickableSurfaceDefaults.colors(
    containerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    pressedContainerColor = Color.Transparent,
)
