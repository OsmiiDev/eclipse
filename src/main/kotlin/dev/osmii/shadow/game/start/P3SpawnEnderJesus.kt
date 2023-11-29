package dev.osmii.shadow.game.start

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe

class P3SpawnEnderJesus(private val shadow: Shadow) {
    fun spawnEnderJesus() {
        // Kill all other ender jesus
        shadow.server.worlds.forEach { world ->
            world.entities.forEach { entity ->
                if (entity.type == EntityType.VILLAGER
                    && entity.customName() != null
                    && entity.customName()!! == MiniMessage.miniMessage().deserialize("<gold>Ender Jesus</gold>")
                ) {
                    entity.remove()
                }
            }
        }
        val l: Location = shadow.server.getWorld("world")!!.spawnLocation
        l.add(0.0, 80.0, 0.0)
        if (l.y > 320) {
            l.y = 320.0
        }
        val v: Villager = shadow.server.getWorld("world")!!.spawnEntity(l, EntityType.VILLAGER) as Villager
        v.setAI(false)
        v.isInvulnerable = true
        v.isCollidable = false
        v.setGravity(false)
        v.isSilent = true

        v.customName(MiniMessage.miniMessage().deserialize("<gold>Ender Jesus</gold>"))
        v.isCustomNameVisible = true

        v.villagerType = Villager.Type.PLAINS
        v.profession = Villager.Profession.MASON
        v.setAdult()

        val trades: ArrayList<MerchantRecipe> = ArrayList()
        val recipe = MerchantRecipe(ItemStack(Material.ENDER_EYE, 1), Integer.MAX_VALUE)
        recipe.addIngredient(ItemStack(Material.NETHER_BRICKS, 1))
        trades.add(recipe)

        v.recipes = trades

        // Finish phase
        shadow.gameState.currentPhase = GamePhase.GAME_IN_PROGRESS
    }

}