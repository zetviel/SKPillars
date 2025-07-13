package net.adamcowell14.skpillars.listeners

import net.adamcowell14.skpillars.SKPillars
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class DisableCommand(private val plugin: SKPillars) : Listener {

    @EventHandler
    fun onPlayerCommand(e: PlayerCommandPreprocessEvent) {
        val player = e.player
        val command = e.message

        if (player.isOp) return

        if (command !in listOf("/pillars leave")) {
            for (key in plugin.getArenaNames()) {
                if (plugin.activeGames[key]?.players?.containsKey(player.name) == true &&
                    plugin.activeGames[key]?.players?.get(player.name)?.lose == false) {

                    player.sendMessage("§cТебе нельзя писать данную команду")
                    e.isCancelled = true
                }
            }
        }
    }
}