package dev.osmii.shadow.game.abilities

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.Namespace
import dev.osmii.shadow.util.ItemUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ToggleStrength : Ability {
    override val item: ItemStack = ItemStack(Material.POTION)
    init {
        item.itemMeta = (item.itemMeta as PotionMeta).apply {
            this.displayName(MiniMessage.miniMessage().deserialize("<!i><red>Potion of Strength</red></!i>"))
            this.lore(listOf(
                Component.text("Use only when needed... you could be found out").color(NamedTextColor.GRAY)
            ))
            this.addCustomEffect(
                PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    -1,
                    0,
                    false,
                    false,
                    true
                ), true
            )
            this.persistentDataContainer.set(
                Namespace.FORBIDDEN,
                PersistentDataType.BYTE_ARRAY,
                ItemUtil.forbidden(drop = true, use = false, move = false, moveContainer = false)
            )
        }
    }
    override fun apply(player: Player, shadow: Shadow) {

        if(player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE)
            player.sendMessage(
                MiniMessage.miniMessage().deserialize("<red>Toggled Strength Off</red>")
            )
        } else {
            player.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE,-1,0,
                false,false, true))
            player.sendMessage(
                MiniMessage.miniMessage().deserialize("<green>Toggled Strength On</green>")
            )
        }
    }
}