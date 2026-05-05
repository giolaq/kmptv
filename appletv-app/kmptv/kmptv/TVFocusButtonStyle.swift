//
//  TVFocusButtonStyle.swift
//  kmptv
//
//  Single `ButtonStyle` that replaces the two near-identical styles that used
//  to live in ContentView.swift and VideoPlayerView.swift (`TVDarkButtonStyle`
//  + `TVFocusableButton`). Reading `@Environment(\.isFocused)` only works
//  inside a `ButtonStyle`, so every focus-aware button should flow through
//  this style rather than trying to observe focus at view-body level.
//

import SwiftUI

struct TVFocusButtonStyle: ButtonStyle {
    @Environment(\.isFocused) private var isFocused: Bool

    /// Scale multiplier applied when the button is focused.
    var focusedScale: CGFloat = 1.05
    /// Corner radius of the focus ring and fill.
    var cornerRadius: CGFloat = 12
    /// Width of the focus outline.
    var strokeWidth: CGFloat = 3
    /// Extra glow shown when focused.
    var glowRadius: CGFloat = 8

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .background(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(isFocused ? Color.white.opacity(0.2) : Color(white: 0.15))
                    .strokeBorder(
                        isFocused ? Color.white : Color.clear,
                        lineWidth: strokeWidth
                    )
                    .shadow(
                        color: isFocused ? Color.white.opacity(0.4) : Color.clear,
                        radius: isFocused ? glowRadius : 0
                    )
            )
            .scaleEffect(isFocused ? focusedScale : 1.0)
            .animation(.easeInOut(duration: 0.2), value: isFocused)
    }
}
