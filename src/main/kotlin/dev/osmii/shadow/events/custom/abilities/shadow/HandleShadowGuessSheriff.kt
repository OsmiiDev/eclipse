package dev.osmii.shadow.events.custom.abilities.shadow

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.Namespace
import dev.osmii.shadow.enums.PlayableFaction
import dev.osmii.shadow.enums.PlayableRole
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class HandleShadowGuessSheriff(var shadow: Shadow) : Listener {

    private var guesses: HashMap<Player, Player> = HashMap()
    private var pages = HashMap<Player, Int>()
    private var items = HashMap<Player, ItemStack>()

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        if (e.currentItem == null || e.currentItem?.itemMeta == null || !e.currentItem?.itemMeta?.persistentDataContainer?.has(
                Namespace.CUSTOM_ID,
                PersistentDataType.STRING
            )!!
        ) return
        if (!e.currentItem?.itemMeta?.persistentDataContainer?.get(Namespace.CUSTOM_ID, PersistentDataType.STRING)
                .equals("ability-shadow-guess-sheriff")
        ) return
        if (e.whoClicked !is Player) return

        pages[e.whoClicked as Player] = 1
        items[e.whoClicked as Player] = e.currentItem!!
        e.whoClicked.setItemOnCursor(null)

        val player = AtomicReference(e.whoClicked as Player)
        Bukkit.getScheduler().runTaskLater(shadow, Runnable {
            buildInventory(player.get(), 1)
        }, 1L)

        e.isCancelled = true
    }

    private fun buildInventory(p: Player, page: Int) {
        val e: Inventory = Bukkit.createInventory(
            null, 54,
            MiniMessage.miniMessage().deserialize("<dark_gray>Guess Sheriff</dark_gray>")
        )
        for (i in 0..53) {
            val useless = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
            useless.itemMeta = useless.itemMeta.apply {
                this?.displayName(Component.text(" "))
            }
            e.setItem(i, useless)
        }

        var guessablePlayers =
            shadow.gameState.currentRoles.filter { (_, role) -> role.roleFaction != PlayableFaction.SHADOW && role != PlayableRole.SPECTATOR }.keys.toTypedArray()
        val constraints = guessablePlayers.size
        guessablePlayers = guessablePlayers.copyOfRange((page - 1) * 21, (page * 21).coerceAtMost(constraints))
        var i = 10
        for (uuid in guessablePlayers) {
            val player = shadow.server.getPlayer(uuid)
            if (player == null) {
                shadow.logger.warning("Player $uuid is null!")
                continue
            }
            val head = ItemStack(Material.PLAYER_HEAD)

            head.itemMeta = (head.itemMeta as SkullMeta).apply {
                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                this.setOwningPlayer(player)
                this.displayName(MiniMessage.miniMessage().deserialize("<gold>${player.displayName()}</gold>"))
            }
            e.setItem(i, head)
            i++
            if (i == 17 || i == 26 || i == 35) i += 2
        }

        val cancel = ItemStack(Material.BARRIER)
        cancel.itemMeta = cancel.itemMeta!!.apply {
            this.displayName(MiniMessage.miniMessage().deserialize("<red>Cancel</red>"))
        }
        val guess = ItemStack(Material.GREEN_CONCRETE, 1)
        guess.itemMeta = guess.itemMeta!!.apply {
            this.displayName(MiniMessage.miniMessage().deserialize("<green>Guess</green>"))
            this.lore(
                listOf(
                    MiniMessage.miniMessage()
                        .deserialize("<gray><i>Click to guess a player as a sheriff.</i></gray>"),
                    MiniMessage.miniMessage()
                        .deserialize("<red><i>You will be revealed if you guess incorrectly.</i></red>"),
                )
            )
        }

        e.setItem(39, cancel)
        e.setItem(41, guess)

        p.openInventory(e)
    }

    @EventHandler
    fun onClickInSelector(e: InventoryClickEvent) {
        if (e.currentItem == null || e.currentItem?.itemMeta == null) return
        if (e.view.title() != MiniMessage.miniMessage().deserialize("<dark_gray>Guess Sheriff</dark_gray>")) return
        if (e.whoClicked !is Player) return
        e.isCancelled = true

        val player = e.whoClicked as Player

        if (e.currentItem!!.itemMeta?.displayName() == MiniMessage.miniMessage().deserialize("<red>Cancel</red>")) {
            Bukkit.getScheduler().runTaskLater(shadow, Runnable {
                player.closeInventory()
            }, 1L)
            return
        }

        if (e.currentItem!!.itemMeta is SkullMeta) {
            val uuid = (e.currentItem!!.itemMeta as SkullMeta).owningPlayer?.uniqueId ?: return
            if (shadow.server.getPlayer(uuid) == null || shadow.server.getPlayer(uuid)?.isOnline == false) return
            guesses[player] = shadow.server.getPlayer(uuid)!!

            for (i in 10..53) {
                if (e.inventory.getItem(i) == null || e.inventory.getItem(i)?.itemMeta == null) continue
                e.inventory.getItem(i)?.itemMeta = (e.inventory.getItem(i)?.itemMeta).apply {
                    this?.removeEnchant(Enchantment.DAMAGE_ALL)
                }
            }
            e.currentItem!!.itemMeta = (e.currentItem!!.itemMeta as SkullMeta).apply {
                this.addEnchant(Enchantment.DAMAGE_ALL, 1, true)
            }

            e.inventory.getItem(41)?.itemMeta = e.inventory.getItem(41)?.itemMeta!!.apply {
                this.lore(
                    listOf(
                        Component.text("Click to guess").color(NamedTextColor.BLUE)
                            .append(
                                Component.text(" ${shadow.server.getPlayer(uuid)?.displayName()}")
                                    .color(NamedTextColor.GOLD)
                            )
                            .append(Component.text(" as a sheriff.").color(NamedTextColor.BLUE)),
                        MiniMessage.miniMessage()
                            .deserialize("<red><i>You will be revealed if you guess incorrectly.</i></red>"),
                    )
                )
            }

            return
        }

        if (e.currentItem!!.itemMeta?.displayName() == Component.text("Guess").color(NamedTextColor.GREEN)) {
            if (shadow.gameState.currentRoles.filter { (_, role) -> role.roleFaction != PlayableFaction.SHADOW && role != PlayableRole.SPECTATOR }.size < 2) {
                player.sendMessage(
                    MiniMessage.miniMessage()
                        .deserialize("<red>You cannot guess any players when less than two non-shadows are alive.</red>")
                )
                return
            }
            if (guesses[player] == null) {
                player.sendMessage(
                    MiniMessage.miniMessage().deserialize("<red>You must select a player to guess.</red>")
                )
                return
            }
            val guess = guesses[player]!!
            if (shadow.gameState.currentRoles[guess.uniqueId] != PlayableRole.SHERIFF) {
                player.sendMessage(
                    MiniMessage.miniMessage()
                        .deserialize("<red>You guessed your target incorrectly! You have been revealed as a shadow.</red>")
                )
                shadow.gameState.currentRoles[player.uniqueId] = PlayableRole.SHADOW
                shadow.server.onlinePlayers.forEach { p ->
                    p.sendMessage(
                        Component.text("${player.displayName()} ")
                            .color(NamedTextColor.GOLD)
                            .append(Component.text(" tried to cast a spell, but it backfired! They must be a shadow!"))
                    )
                }
                player.world.strikeLightningEffect(player.location)
            } else {
                player.sendMessage(
                    MiniMessage.miniMessage().deserialize("<green>You guessed your target's role correctly.</green>")
                )
                val guessed = shadow.server.getPlayer(guess.uniqueId)
                MiniMessage.miniMessage().deserialize("<red>Your role was guessed by a Shadow!</red>")
                guessed?.damage(1000.0)
                guessed?.world?.strikeLightningEffect(guessed.location)
            }

            Bukkit.getScheduler().runTaskLater(shadow, Runnable {
                if (e.view.title() != MiniMessage.miniMessage()
                        .deserialize("<dark_gray>Guess Sheriff</dark_gray>")
                ) return@Runnable
                player.closeInventory()
                val item = items[player]!!
                item.amount -= 1
            }, 1L)
            return
        }
    }
}