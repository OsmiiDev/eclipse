package dev.osmii.shadow.util

import java.time.Duration

object TimeUtil {
    fun ticks(ticks: Int): Duration {
        return Duration.ofMillis(ticks.toLong() * 50)
    }
}