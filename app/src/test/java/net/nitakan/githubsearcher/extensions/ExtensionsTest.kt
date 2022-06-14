package net.nitakan.githubsearcher.extensions

import org.junit.Assert.assertEquals
import org.junit.Test


internal class ExtensionsTest {

    @Test
    fun toStringWithComma() {
        assertEquals(0.toStringWithComma(), "0")
        assertEquals(999.toStringWithComma(), "999")
        assertEquals(1000.toStringWithComma(), "1,000")
        assertEquals(999999.toStringWithComma(), "999,999")
        assertEquals(1000000.toStringWithComma(), "1,000,000")
        assertEquals((-1).toStringWithComma(), "-1")
        assertEquals((-100).toStringWithComma(), "-100")
        assertEquals((-1000).toStringWithComma(), "-1,000")
        assertEquals((-999999).toStringWithComma(), "-999,999")
        assertEquals((-1999999).toStringWithComma(), "-1,999,999")
    }

    @Test
    fun toStringWithSiUnitSuffix() {
        assertEquals(0.toStringWithSiUnitSuffix(), "0")
        assertEquals(123.toStringWithSiUnitSuffix(), "123")
        assertEquals(1234.toStringWithSiUnitSuffix(), "1.2K")
        assertEquals(1234999.toStringWithSiUnitSuffix(), "1.2M")
    }
}