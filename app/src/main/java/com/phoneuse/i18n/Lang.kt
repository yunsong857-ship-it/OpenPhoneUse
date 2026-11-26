package com.phoneuse.i18n

import android.os.LocaleList
import java.util.Locale

/**
 * The app follows the first configured app/device locale.
 * English and Korean copy remains side by side in [tr]; Simplified Chinese is supplied by [ZhCN].
 */
object Lang {
    @Volatile
    var korean: Boolean = isKorean(Locale.getDefault())

    @Volatile
    var simplifiedChinese: Boolean = isSimplifiedChinese(Locale.getDefault())

    /** Follows the first app/device language; called at startup and when configuration changes. */
    fun update(locales: LocaleList) {
        val locale = locales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()
        korean = isKorean(locale)
        simplifiedChinese = isSimplifiedChinese(locale)
    }

    fun isKorean(locale: Locale): Boolean = locale.language == "ko"

    /**
     * Accept explicit Simplified Chinese script tags (zh-Hans) and the common
     * Simplified Chinese regions (CN and SG). Traditional Chinese (TW/HK/MO) stays English
     * until a Traditional Chinese translation is provided.
     */
    fun isSimplifiedChinese(locale: Locale): Boolean =
        locale.language == "zh" && (
            locale.script.equals("Hans", ignoreCase = true) ||
                locale.country.equals("CN", ignoreCase = true) ||
                locale.country.equals("SG", ignoreCase = true)
            )

    /** The language's English name, for instructions to the model. */
    val name: String
        get() = when {
            simplifiedChinese -> "Simplified Chinese"
            korean -> "Korean"
            else -> "English"
        }

    /** Language tag the model is asked to use when the user's own words do not settle it. */
    val tag: String
        get() = when {
            simplifiedChinese -> "zh-CN"
            korean -> "ko"
            else -> "en"
        }
}

/** The app's UI copy: English by default, Korean or Simplified Chinese when selected. */
fun tr(en: String, ko: String): String = when {
    Lang.simplifiedChinese -> ZhCN.translate(en) ?: en
    Lang.korean -> ko
    else -> en
}
