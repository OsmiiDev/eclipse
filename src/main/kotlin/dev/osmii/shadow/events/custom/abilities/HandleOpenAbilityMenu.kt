package dev.osmii.shadow.events.custom.abilities

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.CID
import dev.osmii.shadow.util.ItemUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import kotlin.collections.ArrayList

class HandleOpenAbilityMenu(val shadow: Shadow): Listener {

    @EventHandler
    fun onOpenAbilityMenu(e: PlayerInteractEvent) {
        if(e.item == null || !ItemUtil.customIdIs(e.item!!, CID.HOTBAR_ABILITY_SELECT)) return
        shadow.gameState.queuedAbilityMenus.getOrPut(e.player) { LinkedList() }.clear()
        shadow.gameState.queuedAbilityActions.getOrPut(e.player) { ArrayList() }.clear()
        e.player.sendMessage("Ability menu opened!")
    }
}