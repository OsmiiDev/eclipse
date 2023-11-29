package dev.osmii.shadow.events.custom

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.Namespace
import dev.osmii.shadow.util.ItemUtil
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.persistence.PersistentDataType

class HandleParticipationToggle(val shadow: Shadow) : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.item == null) return
        if (!ItemUtil.customIdIs(e.item!!, "participation-toggle")) return
        if (e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK) return

        val player = e.player
        val participationStatus = shadow.gameState.participationStatus[player.uniqueId] ?: return

        shadow.gameState.participationStatus[player.uniqueId] = !participationStatus
        val participationToggle = e.item!!
        participationToggle.itemMeta = participationToggle.itemMeta?.apply {
            if (shadow.gameState.participationStatus[player.uniqueId]!!) {
                this.displayName(
                    MiniMessage.miniMessage().deserialize(
                        "<!i><gray>Participation: <green>Participating</green></gray></!i>"
                    )
                )
                this.addEnchant(Enchantment.DAMAGE_ALL, 1, true)
                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            } else {
                this.displayName(
                    MiniMessage.miniMessage().deserialize(
                        "<!i><gray>Participation: <red>Not Participating</red></gray></!i>"
                    )
                )
                this.removeEnchant(Enchantment.DAMAGE_ALL)
            }
            this.persistentDataContainer.set(
                Namespace.FORBIDDEN,
                PersistentDataType.BYTE_ARRAY,
                ItemUtil.forbidden(drop = true, use = true, move = true)
            )
            this.persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "participation-toggle")
        }
    }
}