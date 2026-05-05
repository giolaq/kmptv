//
//  TVCardButtonStyle.swift
//  kmptv
//
//  Button style for poster cards (16:9 art + overlays). Unlike
//  `TVFocusButtonStyle`, this one doesn't paint a background fill — the card
//  provides its own visual. It only adds focus feedback: a scale-up, a white
//  outline, and a soft shadow. Mirrors the Android TV `TVCard` look.
//

import SwiftUI

struct TVCardButtonStyle: ButtonStyle {
    @Environment(\.isFocused) private var isFocused: Bool

    var focusedScale: CGFloat = 1.08
    var cornerRadius: CGFloat = 8
    var strokeWidth: CGFloat = 3

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .stroke(Color.white, lineWidth: isFocused ? strokeWidth : 0)
            )
            .shadow(
                color: isFocused ? Color.black.opacity(0.6) : .clear,
                radius: isFocused ? 16 : 0,
                y: isFocused ? 6 : 0
            )
            .scaleEffect(isFocused ? focusedScale : 1.0)
            .animation(.easeInOut(duration: 0.2), value: isFocused)
    }
}
