package com.phoneuse.overlay

import android.view.accessibility.AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY
import android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION
import android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD
import com.phoneuse.screen.Bounds
import com.phoneuse.screen.ScreenSnapshot
import com.phoneuse.screen.UiNode
import org.junit.Assert.assertEquals
import org.junit.Test

class HandoffWindowsTest {
    private val app = HandoffWindow(1, TYPE_APPLICATION, 1, Bounds(0, 0, 400, 700))
    private val dialog = HandoffWindow(2, TYPE_APPLICATION, 2, Bounds(60, 100, 340, 410))
    private val keyboard = HandoffWindow(3, TYPE_INPUT_METHOD, 3, Bounds(0, 430, 400, 700))

    @Test
    fun usesLayersToPickTheFrontDialogAndKeyboardAndIgnoresOurOverlay() {
        val lowerDialog = dialog.copy(id = 4, bounds = Bounds(40, 90, 360, 430))
        val upperDialog = dialog.copy(layer = 4)
        val lowerKeyboard = keyboard.copy(id = 5, layer = 2, bounds = Bounds(0, 490, 400, 700))
        val overlay = HandoffWindow(6, TYPE_ACCESSIBILITY_OVERLAY, 6, app.bounds)
        val windows = listOf(lowerKeyboard, app, overlay, keyboard, lowerDialog, upperDialog)
        val expected = HandoffWindows(setOf(1, 2, 4), upperDialog.bounds, keyboard.bounds)
        assertEquals(expected, HandoffWindows.from(windows))
        assertEquals(expected, HandoffWindows.from(windows.reversed()))
    }

    @Test
    fun sideBySideAppsArePeersEvenWhenTheKeyboardOverlapsBoth() {
        val left = app.copy(bounds = Bounds(0, 0, 200, 700))
        val right = app.copy(id = 2, layer = 2, bounds = Bounds(200, 0, 400, 700))
        assertEquals(
            HandoffWindows(setOf(1, 2), keyboard = keyboard.bounds),
            HandoffWindows.from(listOf(keyboard, left, right)),
        )
        val leftDialog = dialog.copy(id = 4, layer = 3, bounds = Bounds(0, 100, 190, 410))
        val frontPeer = right.copy(layer = 4)
        assertEquals(leftDialog.bounds, HandoffWindows.from(listOf(frontPeer, left, leftDialog)).dialog)
    }

    @Test
    fun newlyOpenedKeyboardKeepsOnlyControlsWhollyAboveItInCurrentAppWindows() {
        val field = button(Bounds(10, 80, 390, 170))
        val confirm = button(Bounds(20, 350, 300, keyboard.bounds.top))
        val partiallyCovered = button(Bounds(20, 400, 300, 480))
        val covered = button(Bounds(80, 510, 300, 640))
        val panel = button(Bounds(0, 40, 400, keyboard.bounds.top)).copy(children = listOf(field, confirm))
        val background = button(app.bounds).copy(children = listOf(panel, partiallyCovered, covered))
        val closedWindow = button(Bounds(40, 200, 300, 250)).copy(windowId = 2)
        val screen = ScreenSnapshot(null, 400, 700, listOf(background, closedWindow))
        val windows = HandoffWindows.from(listOf(app, keyboard))
        assertEquals(
            HandoffControls(listOf(field.bounds, confirm.bounds), listOf(panel.bounds)),
            windows.controlsFrom(screen, 400, 700),
        )
    }

    @Test
    fun discardsStaleGeometryAfterRotationOrClosingTheKeyboard() {
        val field = button(Bounds(20, 80, 390, 150))
        val screen = ScreenSnapshot(null, 400, 700, listOf(field), keyboardVisible = true)
        val windows = HandoffWindows.from(listOf(app, keyboard))
        assertEquals(listOf(field.bounds), windows.controlsFrom(screen, 400, 700).targets)
        assertEquals(HandoffControls(), windows.controlsFrom(screen, 700, 400))
        assertEquals(HandoffControls(), HandoffWindows.from(listOf(app)).controlsFrom(screen, 400, 700))
    }

    private fun button(bounds: Bounds) =
        UiNode(1, emptyList(), "Button", null, null, null, null, bounds, clickable = true)
}
