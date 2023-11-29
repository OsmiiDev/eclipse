package dev.osmii.shadow

import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableRole
import dev.osmii.shadow.game.rolelist.Rolelist
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

class ShadowGameState {
    var currentPhase: GamePhase = GamePhase.NONE

    var participationStatus = HashMap<UUID, Boolean>()

    var originalRolelist: Rolelist = Rolelist()
    var originalRoles: HashMap<UUID, PlayableRole> = HashMap<UUID, PlayableRole>()
    var currentRoles: HashMap<UUID, PlayableRole> = HashMap<UUID, PlayableRole>()
    var currentWinners: HashSet<Player> = HashSet<Player>()
}