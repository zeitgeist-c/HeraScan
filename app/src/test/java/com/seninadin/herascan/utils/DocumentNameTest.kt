package com.seninadin.herascan.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class DocumentNameTest {
    @Test
    fun `blank name falls back`() {
        assertEquals("Belge", resolveDocumentName("   ", "Belge"))
    }

    @Test
    fun `name is trimmed`() {
        assertEquals("Fatura", resolveDocumentName("  Fatura  ", "Belge"))
    }

    @Test
    fun `non-blank name is preserved`() {
        assertEquals("Sözleşme 12", resolveDocumentName("Sözleşme 12", "Belge"))
    }
}

class DateFormatsTest {
    @Test
    fun `history date is not blank and contains year`() {
        val text = DateFormats.formatHistoryDate(1_700_000_000_000L)
        assertTrue(text.isNotBlank())
        assertTrue(text.contains("2023"))
    }

    @Test
    fun `scan timestamp contains year and time separator`() {
        val text = DateFormats.formatScanTimestamp(Date(1_700_000_000_000L))
        assertTrue(text.isNotBlank())
        assertTrue(text.contains("2023"))
        assertTrue(text.contains(":"))
    }

    @Test
    fun `file timestamp is filesystem safe`() {
        val text = DateFormats.formatFileTimestamp(Date(1_700_000_000_000L))
        assertTrue(text.matches(Regex("[0-9_]+")))
    }
}
