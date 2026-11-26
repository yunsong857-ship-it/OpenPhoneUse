package com.phoneuse.i18n

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class LangTest {
    private val beforeKorean = Lang.korean
    private val beforeSimplifiedChinese = Lang.simplifiedChinese

    @After fun restore() {
        Lang.korean = beforeKorean
        Lang.simplifiedChinese = beforeSimplifiedChinese
    }

    @Test fun testsRunInKorean() {
        assertTrue(Lang.korean)
        assertFalse(Lang.simplifiedChinese)
        assertEquals("검색", tr("Search", "검색"))
    }

    @Test fun anyOtherLanguageIsEnglish() {
        assertTrue(Lang.isKorean(Locale.KOREA))
        assertFalse(Lang.isKorean(Locale.US))
        assertFalse(Lang.isKorean(Locale.JAPAN))
        Lang.korean = false
        Lang.simplifiedChinese = false
        assertEquals("Search", tr("Search", "검색"))
        assertEquals("en", Lang.tag)
    }

    @Test fun simplifiedChineseLocalesAreRecognized() {
        assertTrue(Lang.isSimplifiedChinese(Locale.SIMPLIFIED_CHINESE))
        assertTrue(Lang.isSimplifiedChinese(Locale.CHINA))
        assertTrue(Lang.isSimplifiedChinese(Locale.forLanguageTag("zh-Hans")))
        assertTrue(Lang.isSimplifiedChinese(Locale.forLanguageTag("zh-SG")))
        assertFalse(Lang.isSimplifiedChinese(Locale.TRADITIONAL_CHINESE))
        assertFalse(Lang.isSimplifiedChinese(Locale.TAIWAN))
        assertFalse(Lang.isSimplifiedChinese(Locale.forLanguageTag("zh-HK")))
    }

    @Test fun simplifiedChineseUsesChineseCopyAndModelLanguage() {
        Lang.korean = false
        Lang.simplifiedChinese = true
        assertEquals("搜索", tr("Search", "검색"))
        assertEquals("设置", tr("Settings", "설정"))
        assertEquals("zh-CN", Lang.tag)
        assertEquals("Simplified Chinese", Lang.name)
    }

    @Test fun koreanAndEnglishNamesFollowLanguage() {
        Lang.simplifiedChinese = false
        Lang.korean = false
        assertEquals("English", Lang.name)
        Lang.korean = true
        assertEquals("Korean", Lang.name)
    }
}
