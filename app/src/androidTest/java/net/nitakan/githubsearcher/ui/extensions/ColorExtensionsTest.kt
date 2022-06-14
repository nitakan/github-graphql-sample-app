package net.nitakan.githubsearcher.ui.extensions

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

internal class ColorExtensionsTest {

    @Test
    fun fromHexString() {
        assertEquals(Color.fromHexString("#FF0000"), Color.Red)
        assertEquals(Color.fromHexString("FF0000"), Color.Red)
    }
}