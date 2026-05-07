package com.kmptv.androidtv.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ClickableSurfaceColors
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun transparentSurfaceColors(): ClickableSurfaceColors = ClickableSurfaceDefaults.colors(
    containerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    pressedContainerColor = Color.Transparent,
)
