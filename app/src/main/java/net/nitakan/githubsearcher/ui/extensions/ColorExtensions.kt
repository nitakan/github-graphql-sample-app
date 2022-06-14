package net.nitakan.githubsearcher.ui.extensions

import androidx.compose.ui.graphics.Color

fun Color.Companion.fromHexString(colorString: String): Color {
    val text = if (colorString.startsWith("#")) {
        colorString
    } else {
        "#$colorString"
    }
    return Color(android.graphics.Color.parseColor(text))
}