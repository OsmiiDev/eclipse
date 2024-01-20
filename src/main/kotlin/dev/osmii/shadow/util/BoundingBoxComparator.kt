package dev.osmii.shadow.util

import org.bukkit.util.BoundingBox

class BoundingBoxComparator : Comparator<BoundingBox> {
    override fun compare(o1: BoundingBox, o2: BoundingBox): Int {
        return (o1.minX - o2.minX).toInt()
    }
}