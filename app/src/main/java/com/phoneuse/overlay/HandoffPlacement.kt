package com.phoneuse.overlay

import com.phoneuse.screen.Bounds
import com.phoneuse.screen.ScreenSnapshot
import com.phoneuse.screen.UiNode

internal data class HandoffControls(val targets: List<Bounds> = emptyList(), val containers: List<Bounds> = emptyList())

/** Prefer the space above a docked keyboard unless the bar needs more room to clear the user's controls. */
internal fun handoffArea(area: Bounds, keyboard: Bounds?, minBarHeight: Int, moveFrom: Bounds? = null): Bounds {
    if (keyboard == null || !area.intersects(keyboard)) return area
    val docked = keyboard.left <= area.left && keyboard.right >= area.right && keyboard.bottom >= area.bottom
    val height = minBarHeight.coerceAtLeast(1)
    if (!docked || keyboard.top - area.top < height) return area
    val strip = area.copy(bottom = keyboard.top)
    if (moveFrom != null && strip.intersects(moveFrom) &&
        moveFrom.top - strip.top < height && strip.bottom - moveFrom.bottom < height
    ) return area
    return strip
}

/** At most half the area, so Move can uncover where the bar was, but never less than the collapsed bar needs. */
internal fun handoffBarMaxHeight(areaHeight: Int, collapsedHeight: Int): Int {
    val height = areaHeight.coerceAtLeast(0)
    return (height / 2).coerceAtLeast(collapsedHeight).coerceAtMost(height)
}

/** Use only the cached geometry of app windows that are still on this display. */
internal fun ScreenSnapshot.handoffControls(windowIds: Set<Int>): HandoffControls {
    val targets = mutableListOf<Bounds>()
    val containers = mutableListOf<Bounds>()
    fun visit(node: UiNode) {
        val firstChildTarget = targets.size
        node.children.forEach(::visit)
        if (!node.visibleToUser || !node.enabled || node.covered) return
        val input = node.editable || node.checkable || node.password
        if (!input && !node.clickable && !node.longClickable) return
        val bounds = node.uncovered ?: node.bounds
        if (bounds.isEmpty) return
        // A tappable background may span the entire app, including the buttons inside it.
        if (!input && (firstChildTarget until targets.size).any { bounds.contains(targets[it]) }) {
            containers.add(bounds)
        } else {
            targets.add(bounds)
        }
    }

    for (root in appRoots) if (root.windowId in windowIds) visit(root)
    return HandoffControls(targets, containers)
}

/** The closest clear position to the requested edge for a horizontally centered handoff bar. */
internal fun handoffBarTop(
    area: Bounds,
    barWidth: Int,
    barHeight: Int,
    controls: List<Bounds>,
    preferBottom: Boolean,
    containers: List<Bounds> = emptyList(),
    clearance: Int,
): Int {
    if (area.isEmpty || barHeight >= area.bottom - area.top) return area.top
    val lastTop = area.bottom - barHeight.coerceAtLeast(0)
    val edge = if (preferBottom) lastTop else area.top
    if (barWidth <= 0 || barHeight <= 0) return edge

    val width = minOf(barWidth, area.right - area.left)
    val left = area.left + (area.right - area.left - width) / 2
    // The half of the area on the bar's own side. A keyboard or a saved-password sheet opens in the other half,
    // and the bar cannot see those.
    val middle = area.top + (area.bottom - area.top) / 2
    fun inOwnHalf(top: Int): Boolean {
        val center = top + barHeight / 2
        return if (preferBottom) center >= middle else center <= middle
    }
    fun clearTop(obstacles: List<Bounds>, ownHalf: Boolean): Int? {
        val ordered = if (preferBottom) obstacles.sortedByDescending { it.bottom } else obstacles.sortedBy { it.top }
        fun place(gap: Int): Int? {
            var top = edge
            for (obstacle in ordered) {
                val footprint = Bounds(left - gap, top - gap, left + width + gap, top + barHeight + gap)
                if (obstacle.intersects(footprint)) {
                    top = if (preferBottom) obstacle.top - barHeight - gap else obstacle.bottom + gap
                    if (top !in area.top..lastTop) return null
                }
            }
            return top.takeIf { !ownHalf || inOwnHalf(it) }
        }
        // In a tight gap, keeping the controls tappable takes priority over leaving a margin.
        return place(clearance) ?: place(0)
    }

    // A clear place in the bar's own half comes first, even one without a margin. Only then does the bar cross over.
    for (ownHalf in listOf(true, false)) {
        if (containers.isNotEmpty()) clearTop(controls + containers, ownHalf)?.let { return it }
        clearTop(controls, ownHalf)?.let { return it }
    }
    return edge
}
