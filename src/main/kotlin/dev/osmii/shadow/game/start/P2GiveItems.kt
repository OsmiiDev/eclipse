package dev.osmii.shadow.game.start

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.Namespace
import dev.osmii.shadow.enums.PlayableRole
import dev.osmii.shadow.util.ItemUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class P2GiveItems(private var shadow: Shadow) {
    fun giveItems() {
        shadow.gameState.currentRoles.forEach { (uuid, role) ->
            val player: Player? = shadow.server.getPlayer(uuid)
            if (player == null) {
                shadow.logger.warning("Player $uuid is null!")
                return@forEach
            }
            player.inventory.clear()
            player.inventory.setItem(0, ItemStack(Material.BREAD, 16))

            if (role == PlayableRole.SHADOW) {
                val strength = ItemStack(Material.POTION, 1)
                strength.itemMeta = (strength.itemMeta as PotionMeta).apply {
                    this.displayName(Component.text("Potion of Strength").color(TextColor.color(0xf72362)))
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
                player.inventory.setItem(17, strength)
            }

            if (role == PlayableRole.SHERIFF) {
                val bow = ItemStack(Material.BOW, 1)
                bow.itemMeta = (bow.itemMeta!! as Damageable).apply {
                    this.displayName(Component.text("Sheriff's Bow").color(NamedTextColor.GOLD))
                    this.isUnbreakable = true
                    this.addEnchant(Enchantment.ARROW_DAMAGE, 1, true)
                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    this.persistentDataContainer.set(
                        Namespace.FORBIDDEN,
                        PersistentDataType.BYTE_ARRAY,
                        ItemUtil.forbidden(drop = true, use = false, move = false, moveContainer = true)
                    )
                    this.persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "sheriff-bow")
                }
                player.inventory.setItem(9, bow)
            }

            val abilitySelector: ItemStack = ItemStack(Material.NETHER_STAR, 1)
            abilitySelector.itemMeta = abilitySelector.itemMeta.apply {
                if (this == null) return@apply

                this.displayName(Component.text("Ability Selector").color(NamedTextColor.GOLD))

                this.persistentDataContainer.set(
                    Namespace.FORBIDDEN,
                    PersistentDataType.BYTE_ARRAY,
                    ItemUtil.forbidden(drop = true, use = false, move = true, moveContainer = true)
                )
                this.persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "ability-selector")
            }
        }

        P3SpawnEnderJesus(shadow).spawnEnderJesus()
    }
}