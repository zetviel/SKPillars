package net.adamcowell14.skpillars.listeners

import net.adamcowell14.skpillars.SKPillars
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockEvents(private val plugin: SKPillars) : Listener {
    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val player = e.player

        val arenas = plugin.activeGames.keys
        for (key in arenas) {
            if (plugin.activeGames[key]?.players?.containsKey(player.name) == true && plugin.activeGames[key]?.waiting == true) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val player = e.player

        val arenas = plugin.activeGames.keys
        for (key in arenas) {
            if (plugin.activeGames[key]?.players?.containsKey(player.name) == true && plugin.activeGames[key]?.waiting == true) {
                e.isCancelled = true
            }
        }
    }
}