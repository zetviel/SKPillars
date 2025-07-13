package net.adamcowell14.skpillars.listeners

import net.adamcowell14.skpillars.SKPillars
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class LeaveZone(private val plugin: SKPillars) : Listener {

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        val player = e.player
        val arenasSection = plugin.config.getConfigurationSection("game") ?: return

        if (e.from.x == e.to.x && e.from.y == e.to.y && e.from.z == e.to.z) return

        for (arenaName in arenasSection.getKeys(false)) {
            if (plugin.activeGames[arenaName]?.players?.containsKey(player.name) == false) return

            val arenaSection = arenasSection.getConfigurationSection(arenaName) ?: continue
            val deathArena = arenaSection.getStringList("death_arena").takeIf { it.size == 2 } ?: continue

            val deathArena1 = parseLocation(deathArena[0], player.world.name)
            val deathArena2 = parseLocation(deathArena[1], player.world.name)

            if (deathArena1 != null && deathArena2 != null && isInArea(player.location, deathArena1, deathArena2)) {
                handleDeathZone(player, arenaName)
            }
        }
    }

    private fun parseLocation(locString: String, worldName: String): Location? {
        val parts = locString.split(" ")
        if (parts.size != 3) return null

        return try {
            Location(
                plugin.server.getWorld(worldName),
                parts[0].toDouble(),
                parts[1].toDouble(),
                parts[2].toDouble()
            )
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun isInArea(loc: Location, pos1: Location, pos2: Location): Boolean {
        if (loc.world?.name != pos1.world?.name) return false

        val minX = minOf(pos1.x, pos2.x)
        val maxX = maxOf(pos1.x, pos2.x)
        val minY = minOf(pos1.y, pos2.y)
        val maxY = maxOf(pos1.y, pos2.y)
        val minZ = minOf(pos1.z, pos2.z)
        val maxZ = maxOf(pos1.z, pos2.z)

        return loc.x in minX..maxX &&
                loc.y in minY..maxY &&
                loc.z in minZ..maxZ
    }

    private fun handleDeathZone(player: Player, arenaName: String) {
        if (plugin.activeGames[arenaName]?.players?.containsKey(player.name) == false) {
            return
        }

        plugin.activeGames[arenaName]?.let { arena ->
            if (arena.players[player.name]?.lose == true || arena.players[player.name]?.spec == true) {
                return
            }
            arena.players[player.name]?.spec = true
            arena.players[player.name]?.lose = true

            player.inventory.clear()
            player.sendMessage(plugin.messageHelper.parse("lose"))
            player.gameMode = GameMode.SPECTATOR
        }
    }
}