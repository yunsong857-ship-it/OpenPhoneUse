package com.phoneuse.overlay

import android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION
import android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD
import com.phoneuse.screen.Bounds
import com.phoneuse.screen.ScreenSnapshot

internal data class HandoffWindow(val id: Int, val type: Int, val layer: Int, val bounds: Bounds)

internal data class HandoffWindows(
    val appIds: Set<Int> = emptySet(),
    val dialog: Bounds? = null,
    val keyboard: Bounds? = null,
) {
    fun controlsFrom(screen: ScreenSnapshot, width: Int, height: Int): HandoffControls {
        if (screen.displayWidth != width || screen.displayHeight != height) return HandoffControls()
        if (keyboard == null && screen.keyboardVisible) return HandoffControls()
        val controls = screen.handoffControls(appIds)
        if (keyboard == null || screen.keyboardVisible) return controls

        // A newly opened keyboard can cover cached controls. Keep the ones wholly above it.
        return HandoffControls(
            controls.targets.filter { it.bottom <= keyboard.top },
            controls.containers.filter { it.bottom <= keyboard.top },
        )
    }

    companion object {
        fun from(windows: List<HandoffWindow>): HandoffWindows {
            val apps = windows.filter { it.type == TYPE_APPLICATION }
            // A dialog overlaps a lower app window. Side-by-side activities are peers.
            val dialog = apps.sortedByDescending { it.layer }.firstOrNull { front ->
                apps.any { it.layer < front.layer && it.bounds.intersects(front.bounds) }
            }
            val keyboard = windows.filter { it.type == TYPE_INPUT_METHOD }.maxByOrNull { it.layer }
            return HandoffWindows(apps.map { it.id }.toSet(), dialog?.bounds, keyboard?.bounds)
        }
    }
}
