package com.phoneuse.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class ChineseTextTest {
    @Test fun settingsModelAndPermissionCopyHasChineseTranslations() {
        assertEquals("应用语言", ChineseText.translate("App language"))
        assertEquals("默认模型", ChineseText.translate("Default model"))
        assertEquals("无障碍服务", ChineseText.translate("Accessibility service"))
        assertEquals("正在操作手机", ChineseText.translate("Operating the phone"))
        assertEquals("创建自动流程：silence my phone", ChineseText.translate("Make a routine: silence my phone"))
    }

    @Test fun dynamicErrorsAndCountsAreTranslated() {
        assertEquals("错误：屏幕读取失败", ChineseText.translate("Error: screen read failed"))
        assertEquals("还需授权 3 项", ChineseText.translate("3 needed"))
        assertEquals("已记住 5 项内容", ChineseText.translate("5 things remembered"))
        assertEquals("过去 24 小时内没有收到通知。", ChineseText.translate("No notifications in the last 24 hours."))
        assertEquals("提醒我在 07:30 准备出发", ChineseText.translate("Remind me to get ready to leave at 07:30"))
        assertEquals("每隔 30 分钟", ChineseText.translate("Once every 30 min"))
        assertEquals("有地点的日程开始前 1 小时 15 分钟", ChineseText.translate("1 h 15 min before an event with a location"))
    }
}
