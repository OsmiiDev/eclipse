package dev.osmii.shadow.game.rolelist

import dev.osmii.shadow.enums.PlayableFaction
import dev.osmii.shadow.enums.PlayableRole
import org.bukkit.Bukkit

class Rolelist {
    var roles = ArrayList<RolelistSelector>()
    var pickedRoles = ArrayList<PlayableRole>()

    fun addRole(role: RolelistSelector) {
        roles.add(role)
        roles.sort()
    }

    fun getSelectors(): ArrayList<RolelistSelector> {
        return roles
    }

    fun pickRoles(): ArrayList<PlayableRole> {
        val roleList = ArrayList<PlayableRole>()
        var failReason: RolelistInvalidReason? = null
        Bukkit.getLogger().info(getSelectors().toString())

        this.roles.sortBy { it.specificity }
        this.roles.reverse()

        this.roles.forEach { selector ->
            selector.copyToMutableRoles()
        }

        for (sel in this.roles) {
            Bukkit.getLogger().info("1: ${sel.roles}")
            val role = sel.mutableRoles.random()
            roleList.add(role)
            if (sel.mutableRoles.size == 0) failReason = RolelistInvalidReason.UNIQUE_CONFLICT
            if (role.isUnique) {
                this.roles.forEach { selector2 ->
                    Bukkit.getLogger().info("2: ${selector2.roles}")
                    selector2.mutableRoles.remove(role)
                }
                continue
            }
        }

        pickedRoles = roleList
        return roleList
    }

    fun checkValidity(): Pair<Boolean, RolelistInvalidReason> {
        if (roles.size < 4) return Pair(false, RolelistInvalidReason.NOT_ENOUGH_ROLES)
        if (roles.size > 15) return Pair(false, RolelistInvalidReason.TOO_MANY_ROLES)

        var hasVillage = 0
        var hasNeutral = 0
        var hasEvil = 0
        pickedRoles.forEach { role ->
            when (role.roleFaction) {
                PlayableFaction.VILLAGE -> hasVillage = 1
                PlayableFaction.NEUTRAL -> hasNeutral = 1
                PlayableFaction.SHADOW -> hasEvil = 1
                PlayableFaction.SPECTATOR -> return Pair(false, RolelistInvalidReason.INVALID_ROLE)
            }
        }

        if (hasVillage + hasEvil + hasNeutral < 2) return Pair(false, RolelistInvalidReason.ONLY_ONE_FACTION)

        pickedRoles.forEach { role ->
            if (role.isUnique) {
                var count = 0
                pickedRoles.forEach { role2 ->
                    if (role == role2) count++
                }
                if (count > 1) return Pair(false, RolelistInvalidReason.UNIQUE_CONFLICT)
            }
        }

        return Pair(true, RolelistInvalidReason.VALID)
    }
}