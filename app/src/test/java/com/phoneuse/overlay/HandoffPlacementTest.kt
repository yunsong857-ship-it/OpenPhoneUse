package com.phoneuse.overlay

import com.phoneuse.screen.Bounds
import com.phoneuse.screen.ScreenSnapshot
import com.phoneuse.screen.UiNode
import org.junit.Assert.assertEquals
import org.junit.Test

class HandoffPlacementTest {
    private val area = Bounds(40, 80, 360, 680)

    private fun button(bounds: Bounds, window: Int = 1) =
        UiNode(window, emptyList(), "Button", null, null, null, null, bounds, clickable = true)

    @Test
    fun dockedKeyboardLeavesTheSpaceAboveItIncludingAnExactFit() {
        val keyboard = Bounds(0, 400, 400, 700)
        assertEquals(area.copy(bottom = 400), handoffArea(area, keyboard, 48))
        val exact = keyboard.copy(top = area.top + 48)
        assertEquals(area.copy(bottom = exact.top), handoffArea(area, exact, 48))
    }

    @Test
    fun shortSpaceStillAllowsBothRowsOfTheCollapsedBar() {
        assertEquals(300, handoffBarMaxHeight(600, 104))
        // Above a keyboard in landscape, half the space is less than the two rows need.
        assertEquals(104, handoffBarMaxHeight(170, 104))
        assertEquals(104, handoffBarMaxHeight(104, 104))
        assertEquals(80, handoffBarMaxHeight(80, 104))
        assertEquals(0, handoffBarMaxHeight(-20, 104))
    }

    @Test
    fun moveCanLeaveAndReturnToASingleRowAboveTheKeyboard() {
        val strip = area.copy(bottom = area.top + 48)
        val keyboard = area.copy(top = strip.bottom)
        val inStrip = Bounds(120, strip.top, 280, strip.bottom)
        val overKeyboard = Bounds(120, area.bottom - 48, 280, area.bottom)
        assertEquals(strip, handoffArea(area, keyboard, 48))
        assertEquals(area, handoffArea(area, keyboard, 48, inStrip))
        assertEquals(strip, handoffArea(area, keyboard, 48, overKeyboard))
    }

    @Test
    fun fullHeightKeyboardOrTooShortAnExposedStripKeepsBothEdgesAvailable() {
        val usable = handoffArea(area, area, 48)
        assertEquals(area, usable)
        val previousBar = Bounds(120, 80, 280, 128)
        val bottomControl = Bounds(120, 620, 280, 680)
        assertEquals(564, handoffBarTop(usable, 160, 48, listOf(previousBar, bottomControl), true, listOf(area), clearance = 8))
        val narrowStrip = area.copy(top = area.top + 47)
        assertEquals(area, handoffArea(area, narrowStrip, 48))
    }

    @Test
    fun floatingKeyboardLeavesTheFullAreaAvailable() {
        assertEquals(area, handoffArea(area, Bounds(120, 400, 340, 660), 48))
        assertEquals(area, handoffArea(area, null, 48))
    }

    @Test
    fun clickableBackgroundDoesNotForceTheBarOverItsPinKeys() {
        val key = button(Bounds(120, 80, 280, 180))
        val background = button(area).copy(children = listOf(key))
        val controls = ScreenSnapshot(null, 400, 700, listOf(background)).handoffControls(setOf(1))

        assertEquals(listOf(key.bounds), controls.targets)
        assertEquals(listOf(area), controls.containers)
        assertEquals(188, handoffBarTop(area, 160, 48, controls.targets, false, controls.containers, clearance = 8))
    }

    @Test
    fun avoidsTheWholeClickableContainerWhenSpacePermits() {
        val key = button(Bounds(120, 80, 280, 180))
        val panel = button(Bounds(40, 80, 360, 320)).copy(children = listOf(key))
        val controls = ScreenSnapshot(null, 400, 700, listOf(panel)).handoffControls(setOf(1))
        assertEquals(328, handoffBarTop(area, 160, 48, controls.targets, false, controls.containers, clearance = 8))
    }

    @Test
    fun containerDetourThatWouldCrossTheScreenIsNotTaken() {
        // A sign-in page whose form is one tappable container. Below it is where a password sheet opens.
        val toolbar = Bounds(40, 80, 360, 130)
        val field = Bounds(40, 240, 360, 300)
        val submit = Bounds(40, 330, 360, 400)
        val form = Bounds(40, 140, 360, 560)
        val targets = listOf(toolbar, field, submit)
        assertEquals(138, handoffBarTop(area, 160, 48, targets, false, listOf(form), clearance = 8))
        assertEquals(632, handoffBarTop(area, 160, 48, targets, true, listOf(form), clearance = 8))
    }

    @Test
    fun gapInTheBarsOwnHalfIsTakenWithoutAMarginBeforeCrossingTheScreen() {
        // A sign-in page. The gap under the toolbar fits the bar but not its margin, and a password sheet opens below.
        val page = listOf(Bounds(40, 80, 360, 130), Bounds(40, 180, 360, 250), Bounds(40, 260, 360, 400))
        assertEquals(130, handoffBarTop(area, 160, 48, page, preferBottom = false, clearance = 8))
        val mirrored = listOf(Bounds(40, 630, 360, 680), Bounds(40, 510, 360, 580), Bounds(40, 360, 360, 500))
        assertEquals(582, handoffBarTop(area, 160, 48, mirrored, preferBottom = true, clearance = 8))
    }

    @Test
    fun retainsKeypadControlsBehindPictureInPictureAndSkipsClosedOrSystemWindows() {
        val picture = button(Bounds(40, 80, 100, 180), window = 2)
        val key = button(Bounds(120, 80, 280, 180), window = 1)
        val closed = button(area, window = 3)
        val system = button(area, window = 4)
        val screen = ScreenSnapshot(null, 400, 700, listOf(picture, key, closed, system), systemWindowIds = setOf(4))
        val controls = screen.handoffControls(setOf(1, 2, 4))
        assertEquals(listOf(picture.bounds, key.bounds), controls.targets)
        assertEquals(188, handoffBarTop(area, 160, 48, controls.targets, false, controls.containers, clearance = 8))
    }

    @Test
    fun bottomKeypadLeavesTheBarAtTheTop() {
        val keypad = listOf(Bounds(40, 400, 360, 680))
        assertEquals(80, handoffBarTop(area, 160, 48, keypad, preferBottom = false, clearance = 8))
        assertEquals(344, handoffBarTop(area, 160, 48, keypad, preferBottom = true, clearance = 8))
    }

    @Test
    fun topKeypadPlacesTheBarJustBelowItsLastRow() {
        val rows = listOf(Bounds(40, 150, 360, 230), Bounds(40, 70, 360, 160))
        assertEquals(238, handoffBarTop(area, 160, 48, rows, preferBottom = false, clearance = 8))
    }

    @Test
    fun centralGapFitsTheBarExactlyFromEitherDirection() {
        val controls = listOf(Bounds(40, 310, 360, 680), Bounds(40, 70, 360, 262))
        assertEquals(262, handoffBarTop(area, 160, 48, controls, preferBottom = false, clearance = 8))
        assertEquals(262, handoffBarTop(area, 160, 48, controls, preferBottom = true, clearance = 8))
    }

    @Test
    fun leavesClearanceBesideAControlWhenPossible() {
        val sideKey = Bounds(40, 80, 120, 180)
        assertEquals(188, handoffBarTop(area, 160, 48, listOf(sideKey), preferBottom = false, clearance = 8))
    }

    @Test
    fun leavesClearanceAtTheBoundaryOfADockedKeyboard() {
        val keyboard = area.copy(top = 400)
        val strip = handoffArea(area, keyboard, 48)
        assertEquals(344, handoffBarTop(strip, 160, 48, emptyList(), true, listOf(keyboard), clearance = 8))
    }

    @Test
    fun controlsTouchingTheBarAtBothSidesStillAllowAnExactFit() {
        val sides = listOf(Bounds(40, 80, 120, 680), Bounds(280, 80, 360, 680))
        assertEquals(80, handoffBarTop(area, 160, 48, sides, preferBottom = false, clearance = 8))
        assertEquals(632, handoffBarTop(area, 160, 48, sides, preferBottom = true, clearance = 8))
    }

    @Test
    fun noGapTallEnoughFallsBackToTheRequestedEdge() {
        val controls = listOf(Bounds(40, 80, 360, 280), Bounds(40, 300, 360, 680))
        assertEquals(80, handoffBarTop(area, 160, 48, controls, preferBottom = false, clearance = 8))
        assertEquals(632, handoffBarTop(area, 160, 48, controls, preferBottom = true, clearance = 8))
    }

    @Test
    fun areaShorterThanTheBarAnchorsItAtTheTop() {
        val short = Bounds(40, 120, 360, 140)
        assertEquals(120, handoffBarTop(short, 160, 48, emptyList(), preferBottom = false, clearance = 8))
        assertEquals(120, handoffBarTop(short, 160, 48, emptyList(), preferBottom = true, clearance = 8))
    }

    @Test
    fun narrowWindowUsesItsFullWidth() {
        val narrow = Bounds(75, 80, 105, 680)
        val control = Bounds(75, 80, 77, 130)
        assertEquals(138, handoffBarTop(narrow, 160, 48, listOf(control), preferBottom = false, clearance = 8))
    }
}
