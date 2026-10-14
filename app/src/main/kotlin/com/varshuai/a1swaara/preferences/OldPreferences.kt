package com.varshuai.a1swaara.preferences

import com.varshuai.a1swaara.GlobalPreferencesHolder

internal object OldPreferences : GlobalPreferencesHolder() {
    val oldColorPaletteName by enum(ColorPaletteName.Default, "colorPaletteName")
    val oldColorPaletteMode by enum(ColorPaletteMode.Dark, "colorPaletteMode")

    enum class ColorPaletteName {
        Default,
        Dynamic,
        MaterialYou,
        PureBlack,
        AMOLED
    }

    enum class ColorPaletteMode {
        Light,
        Dark,
        System
    }
}
