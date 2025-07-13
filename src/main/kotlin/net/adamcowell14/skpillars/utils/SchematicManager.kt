package net.adamcowell14.skpillars.utils

import dev.efnilite.neoschematic.Schematic
import net.adamcowell14.skpillars.SKPillars
import org.bukkit.Bukkit
import org.bukkit.Location


class SchematicManager(private val plugin: SKPillars) {
    fun save(schematicName: String, pos1: Location, pos2: Location): Boolean {
        try {
            Schematic.createAsync(pos1, pos2, plugin).thenAccept { schematic: Schematic ->
                schematic.saveAsync(
                    "plugins/SKPillars/schematics/$schematicName.json",
                    plugin
                )
            }
            return true
        } catch (e: Exception) {
            plugin.logger.warning(e.toString())
            return false
        }
    }

    fun paste(schematicName: String, location: Location): Boolean {
        try {
            Schematic.loadAsync("plugins/SKPillars/schematics/$schematicName.json", plugin).thenAccept { schematic: Schematic ->
                Bukkit.getScheduler().runTask(plugin,
                    Runnable {
                        schematic.paste(location, true)
                        plugin.logger.info("Schematic $schematicName loaded")
                    })
            }
            return true
        } catch (e: Exception) {
            plugin.logger.warning(e.toString())
            return false
        }
    }
}