package net.adamcowell14.skpillars.listeners

import net.adamcowell14.skpillars.SKPillars
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.slf4j.Logger

class CustomItemsEvents(private val plugin: SKPillars) : Listener {
    val items = listOf(plugin.customItems.statisticHead, plugin.customItems.leaveBed)

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        val player = e.player

        if (e.item == null) return
        if (e.item!!.isSimilar(plugin.customItems.statisticHead)) {
            player.sendMessage(plugin.messageHelper.parse("&7Statika"))
            player.performCommand("pillars stats")
        } else if (e.item!!.isSimilar(plugin.customItems.leaveBed)) {
            player.sendMessage(plugin.messageHelper.parse("&4Leave"))
            player.performCommand("pillars leave")
        }
    }

    @EventHandler
    fun onDropItem(e: PlayerDropItemEvent) {
        for (item2 in items) if (e.itemDrop.itemStack.isSimilar(item2)) e.isCancelled = true
    }

    @EventHandler
    fun onPlaceEvent(e: BlockPlaceEvent) {
        for (item2 in items) if (e.itemInHand.isSimilar(item2)) e.isCancelled = true
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.currentItem == null) return
        for (item2 in items) if (e.currentItem!!.isSimilar(item2)) e.isCancelled = true
    }
}