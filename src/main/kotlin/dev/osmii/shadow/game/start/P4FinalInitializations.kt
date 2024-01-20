package dev.osmii.shadow.game.start

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.game.abilities.KillOneNearby

class P4FinalInitializations(private val shadow: Shadow) {
    fun init() {
        // Set Starting cooldown for KillOneNearby
        KillOneNearby.cooldownMap.keys.forEach { player ->
            KillOneNearby.cooldownMap[player] = 1200
        }
    }
}