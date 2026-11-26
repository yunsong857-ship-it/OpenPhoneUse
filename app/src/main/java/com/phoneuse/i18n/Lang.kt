package com.phoneuse.i18n

import android.os.LocaleList
import java.util.Locale

/** A persisted per-app language. SYSTEM follows the phone's current language. */
enum class AppLanguage(val localeTag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    KOREAN("ko"),
    CHINESE_SIMPLIFIED("zh-CN"),
}

/** App language used by all text produced through [tr], including text created outside Compose. */
object Lang {
    @Volatile
    private var systemLocale: Locale = Locale.getDefault()

    @Volatile
    var selection: AppLanguage = AppLanguage.SYSTEM
        private set

    @Volatile
    var korean: Boolean = isKorean(Locale.getDefault())
        private set

    @Volatile
    var chinese: Boolean = isChinese(Locale.getDefault())
        private set

    /** Follow the effective app locale; called at startup and when Android configuration changes. */
    fun update(locales: LocaleList) {
        systemLocale = locales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()
        applySelection()
    }

    /** Apply a saved or newly selected language immediately. */
    fun select(language: AppLanguage) {
        selection = language
        applySelection()
    }

    private fun applySelection() {
        val locale = when (selection) {
            AppLanguage.SYSTEM -> systemLocale
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.KOREAN -> Locale.KOREAN
            AppLanguage.CHINESE_SIMPLIFIED -> Locale.forLanguageTag("zh-CN")
        }
        korean = isKorean(locale)
        chinese = isChinese(locale)
    }

    fun isKorean(locale: Locale): Boolean = locale.language == "ko"
    fun isChinese(locale: Locale): Boolean = locale.language == "zh"

    /** Language name used in instructions sent to the model. */
    val name: String
        get() = when {
            chinese -> "Simplified Chinese"
            korean -> "Korean"
            else -> "English"
        }

    /** Language tag sent to the model when the user's own words do not settle the response language. */
    val tag: String
        get() = when {
            chinese -> "zh-CN"
            korean -> "ko"
            else -> "en"
        }
}

/** English and Korean stay inline; Chinese uses the app's Simplified Chinese text catalogue. */
fun tr(en: String, ko: String): String = when {
    Lang.chinese -> ChineseText.translate(en) ?: en
    Lang.korean -> ko
    else -> en
}
