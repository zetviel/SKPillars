package net.adamcowell14.skpillars.utils

import com.sk89q.worldedit.*
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.Region
import net.adamcowell14.skpillars.SKPillars
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class WorldEditHelper(private val plugin: SKPillars) {

    fun getPlayerSelection(player: Player): Region? {
        return try {
            val actor = BukkitAdapter.adapt(player)
            val session = WorldEdit.getInstance().sessionManager.get(actor)
            session.getSelection(BukkitAdapter.adapt(player.world))
        } catch (e: IncompleteRegionException) {
            null
        } catch (e: Exception) {
            plugin.logger.severe("Ошибка получения выделения: ${e.message}")
            null
        }
    }

    fun getSelectionBounds(player: Player): Pair<Location, Location>? {
        val selection = getPlayerSelection(player) ?: return null
        val world = player.world

        val min = BukkitAdapter.adapt(world, selection.minimumPoint)
        val max = BukkitAdapter.adapt(world, selection.maximumPoint)

        return Pair(min, max)
    }

    fun clearChunks(world: World, chunkX1: Int, chunkZ1: Int, chunkX2: Int, chunkZ2: Int) {
        val minX = minOf(chunkX1, chunkX2)
        val maxX = maxOf(chunkX1, chunkX2)
        val minZ = minOf(chunkZ1, chunkZ2)
        val maxZ = maxOf(chunkZ1, chunkZ2)

        var currentX = minX
        var currentZ = minZ

        object : BukkitRunnable() {
            override fun run() {
                if (currentX > maxX) {
                    plugin.cooldownGame = false
                    cancel()
                    return
                }

                val chunk = world.getChunkAt(currentX, currentZ)
                for (bx in 0..15) {
                    for (bz in 0..15) {
                        for (y in world.minHeight..<world.maxHeight) {
                            val blockCC = chunk.getBlock(bx, y, bz)
                            if (blockCC.type != Material.AIR) {
                                blockCC.type = Material.AIR
                            }
                        }
                    }
                }
                world.refreshChunk(currentX, currentZ)

                currentZ++
                if (currentZ > maxZ) {
                    currentZ = minZ
                    currentX++
                }
            }
        }.runTaskTimer(plugin, 0L, 0L)
    }
}