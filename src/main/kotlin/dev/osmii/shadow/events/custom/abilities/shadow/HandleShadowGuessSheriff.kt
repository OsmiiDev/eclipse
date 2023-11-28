package dev.osmii.shadow.events.custom.abilities.shadow

import com.sun.jna.platform.unix.X11.Atom
import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.Namespace
import dev.osmii.shadow.enums.PlayableFaction
import dev.osmii.shadow.enums.PlayableRole
import org.bukkit.Bukkit
import org.bukkit.ChatColor
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
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

class HandleShadowGuessSheriff(var shadow: Shadow) : Listener {

    private var guesses: HashMap<Player, Player> = HashMap()
    private var pages = HashMap<Player, Int>()
    private var items = HashMap<Player, ItemStack>()

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        if (e.currentItem == null || e.currentItem?.itemMeta == null || !e.currentItem?.itemMeta?.persistentDataContainer?.has(Namespace.CUSTOM_ID, PersistentDataType.STRING)!!) return
        if (!e.currentItem?.itemMeta?.persistentDataContainer?.get(Namespace.CUSTOM_ID, PersistentDataType.STRING).equals("ability-shadow-guess-sheriff")) return
        if (e.whoClicked !is Player) return

        pages[e.whoClicked as Player] = 1
        items[e.whoClicked as Player] = e.currentItem!!
        e.whoClicked.setItemOnCursor(null)

        val player = AtomicReference<Player>(e.whoClicked as Player)
        Bukkit.getScheduler().runTaskLater(shadow, Runnable {
            buildInventory(player.get(), 1, null)
        }, 1L)

        e.isCancelled = true
    }

    fun buildInventory(p: Player, page: Int, selected: UUID?) {
        val e: Inventory = Bukkit.createInventory(null, 54, "${ChatColor.DARK_GRAY}Guess Sheriff")
        for (i in 0..53) {
            val useless = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
            useless.itemMeta = useless.itemMeta.apply {
                this?.setDisplayName(" ")
            }
            e.setItem(i, useless)
        }

        var guessablePlayers = shadow.gameState.currentRoles.filter { (_, role) -> role.roleFaction != PlayableFaction.SHADOW && role != PlayableRole.SPECTATOR }.keys.toTypedArray()
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
                if(selected == uuid) {
                    this.addEnchant(Enchantment.DAMAGE_ALL, 1, true)
                }
                this.setOwningPlayer(player)
                this.setDisplayName("${ChatColor.GOLD}${player.displayName}")
            }
            e.setItem(i, head)
            i++
            if(i == 17 || i == 26 || i == 35) i += 2
        }

        val cancel = ItemStack(Material.BARRIER)
        cancel.itemMeta = cancel.itemMeta!!.apply {
            this.setDisplayName("${ChatColor.RED}Cancel")
        }
        val guess = ItemStack(Material.GREEN_CONCRETE, 1)
        guess.itemMeta = guess.itemMeta!!.apply {
            this.setDisplayName("${ChatColor.GREEN}Guess")
            if(selected == null) {
                this.lore = listOf(
                    "${ChatColor.GRAY}${ChatColor.ITALIC}Select a player to guess them as the sheriff.",
                    "${ChatColor.RED}${ChatColor.ITALIC}You will be revealed if you guess incorrectly.",
                )
            } else {
                this.lore = listOf(
                    "${ChatColor.GRAY}${ChatColor.ITALIC}Click to guess ${ChatColor.GOLD}${shadow.server.getPlayer(selected)?.displayName}${ChatColor.GRAY} as a sheriff.",
                    "${ChatColor.RED}${ChatColor.ITALIC}You will be revealed if you guess incorrectly.",
                )
            }
        }

        e.setItem(39, cancel)
        e.setItem(41, guess)

        p.openInventory(e)
    }

    @EventHandler
    fun onClickInSelector(e: InventoryClickEvent) {
        if (e.currentItem == null || e.currentItem?.itemMeta == null) return
        if (e.view.title != "${ChatColor.DARK_GRAY}Guess Sheriff") return
        if (e.whoClicked !is Player) return
        e.isCancelled = true

        val player = e.whoClicked as Player

        if(e.currentItem!!.itemMeta?.displayName == "${ChatColor.RED}Cancel") {
            Bukkit.getScheduler().runTaskLater(shadow, Runnable {
                player.closeInventory()
            }, 1L)
            return
        }

        if(e.currentItem!!.itemMeta is SkullMeta) {
            val uuid = (e.currentItem!!.itemMeta as SkullMeta).owningPlayer?.uniqueId
            if(uuid == null) {
                shadow.logger.warning("Player ${player.uniqueId} clicked a skull with no owning player!")
                return
            }
            if(shadow.server.getPlayer(uuid) == null || shadow.server.getPlayer(uuid)?.isOnline == false) {
                shadow.logger.warning("Player ${player.uniqueId} clicked a skull with no owning player!")
                return
            }
            guesses[player] = shadow.server.getPlayer(uuid)!!

            for(i in 10..53) {
                if(e.inventory.getItem(i) == null || e.inventory.getItem(i)?.itemMeta == null) continue
                e.inventory.getItem(i)?.itemMeta = (e.inventory.getItem(i)?.itemMeta).apply {
                    this?.removeEnchant(Enchantment.DAMAGE_ALL)
                }
            }
            e.currentItem!!.itemMeta = (e.currentItem!!.itemMeta as SkullMeta).apply {
                this.addEnchant(Enchantment.DAMAGE_ALL, 1, true)
            }

            e.inventory.getItem(41)?.itemMeta = e.inventory.getItem(41)?.itemMeta!!.apply {
                this.lore = listOf(
                    "${ChatColor.GRAY}${ChatColor.ITALIC}Click to guess ${ChatColor.GOLD}${shadow.server.getPlayer(uuid)?.displayName}${ChatColor.GREEN} as a sheriff.",
                    "${ChatColor.RED}${ChatColor.ITALIC}You will be revealed if you guess incorrectly.",
                )
            }

            return
        }

        if(e.currentItem!!.itemMeta?.displayName == "${ChatColor.GREEN}Guess") {
            if(shadow.gameState.currentRoles.filter { (_, role) -> role.roleFaction != PlayableFaction.SHADOW && role != PlayableRole.SPECTATOR }.size < 2) {
                player.sendMessage("${ChatColor.RED} You cannot guess when less than two non-shadows are alive.")
                return
            }
            if(guesses[player] == null) {
                player.sendMessage("${ChatColor.RED}You must select a player to guess them as the sheriff.")
                return
            }
            val guess = guesses[player]!!
            if(shadow.gameState.currentRoles[guess.uniqueId] != PlayableRole.SHERIFF) {
                player.sendMessage("${ChatColor.RED}You guessed your target incorrectly! You have been revealed as a shadow.")
                shadow.gameState.currentRoles[player.uniqueId] = PlayableRole.SHADOW
                shadow.server.onlinePlayers.forEach { p ->
                    p.sendMessage("${ChatColor.RED}${player.displayName} cast a spell, but it backfired! They must be a shadow!")
                }
                player.world.strikeLightningEffect(player.location)
            } else {
                player.sendMessage("${ChatColor.GREEN}You guessed your target's role correctly.")
                val sheriff = shadow.server.getPlayer(guess.uniqueId)
                sheriff?.sendMessage("${ChatColor.RED}Your role was guessed by a shadow!")
                sheriff?.damage(1000.0)
                sheriff?.world?.strikeLightningEffect(sheriff.location)
            }

            Bukkit.getScheduler().runTaskLater(shadow, Runnable {
                if(player.openInventory.title != "${ChatColor.DARK_GRAY}Guess Sheriff") return@Runnable
                player.closeInventory()
                val item = items[player]!!
                item.amount -= 1
            }, 1L)
            return
        }
    }
}