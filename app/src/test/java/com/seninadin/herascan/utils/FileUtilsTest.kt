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
}
