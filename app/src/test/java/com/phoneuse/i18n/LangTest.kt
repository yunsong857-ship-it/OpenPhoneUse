package com.phoneuse.i18n

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class LangTest {
    private val before = Lang.selection

    @After fun restore() {
        Lang.select(before)
    }

    @Test fun testsRunInKorean() {
        Lang.select(AppLanguage.SYSTEM)
        assertTrue(Lang.korean)
        assertEquals("검색", tr("Search", "검색"))
    }

    @Test fun explicitEnglishOverridesSystem() {
        Lang.select(AppLanguage.ENGLISH)
        assertFalse(Lang.korean)
        assertFalse(Lang.chinese)
        assertEquals("Search", tr("Search", "검색"))
        assertEquals("en", Lang.tag)
        assertEquals("English", Lang.name)
    }

    @Test fun explicitChineseUsesSimplifiedChineseAndModelTag() {
        Lang.select(AppLanguage.CHINESE_SIMPLIFIED)
        assertTrue(Lang.chinese)
        assertFalse(Lang.korean)
        assertEquals("搜索", tr("Search", "검색"))
        assertEquals("zh-CN", Lang.tag)
        assertEquals("Simplified Chinese", Lang.name)
    }

    @Test fun localeDetectionRecognizesKoreanAndChinese() {
        assertTrue(Lang.isKorean(Locale.KOREA))
        assertFalse(Lang.isKorean(Locale.US))
        assertFalse(Lang.isKorean(Locale.JAPAN))
        assertTrue(Lang.isChinese(Locale.SIMPLIFIED_CHINESE))
        assertFalse(Lang.isChinese(Locale.US))
    }
}
