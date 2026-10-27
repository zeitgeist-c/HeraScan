package com.seninadin.herascan.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FileUtilsTest {
    @Test
    fun `document name is preserved while adding extension`() {
        assertEquals("Fatura Ağustos.pdf", "Fatura Ağustos".toSafeFileName(".pdf"))
    }

    @Test
    fun `existing extension is not duplicated`() {
        assertEquals("Fatura.pdf", "Fatura.PDF".toSafeFileName(".pdf"))
    }

    @Test
    fun `characters unsupported by file systems are replaced`() {
        assertEquals("Fatura_2026_08.pdf", "Fatura:2026/08".toSafeFileName(".pdf"))
    }

    @Test
    fun `blank document name uses a stable fallback`() {
        assertEquals("HeraScan.jpg", "   ".toSafeFileName(".jpg"))
    }

    @Test
    fun `unique file name keeps extension and adds suffix`() {
        assertEquals("Fatura_abc123.pdf", uniqueFileName("Fatura.pdf", "abc123"))
    }

    @Test
    fun `unique file name handles name without extension`() {
        assertEquals("Fatura_abc123", uniqueFileName("Fatura", "abc123"))
    }

    @Test
    fun `unique file name keeps leading dot names intact`() {
        assertEquals(".pdf_abc123", uniqueFileName(".pdf", "abc123"))
    }
}
