package net.adamcowell14.skpillars.listeners

import net.adamcowell14.skpillars.SKPillars
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerEvents(private val plugin: SKPillars) : Listener {
    @EventHandler
    fun onDisconnect(e: PlayerQuitEvent) {
        val player = e.player

        val arenas = plugin.activeGames.keys
        for (key in arenas) {
            if (plugin.activeGames[key]?.players?.containsKey(player.name) == true) {
                plugin.activeGames[key]?.removePlayer(player.name)
                player.inventory.clear()

                if (plugin.activeGames[key]?.players?.size!! <= 0) {
                    plugin.activeGames[key]?.declareWinner("none")
                }
            }
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.player

        val arenas = plugin.activeGames.keys
        for (key in arenas) {
            if (plugin.activeGames[key]?.players?.containsKey(player.name) == true) {
                plugin.activeGames[key]?.removePlayer(player.name)
                player.inventory.clear()

                if (plugin.activeGames[key]?.players?.size!! <= 0) {
                    plugin.activeGames[key]?.declareWinner("none")
                }
            }
        }
    }

    @EventHandler
    fun onPlayerDamage(e: EntityDamageByEntityEvent) {
        val player = e.entity

        if (player !is Player) {
            return
        }

        val arenas = plugin.activeGames.keys
        for (key in arenas) {
            if (plugin.activeGames[key]?.players?.containsKey(player.name) == true && plugin.activeGames[key]?.waiting == true) {
                e.isCancelled = true
            }
        }
    }
}