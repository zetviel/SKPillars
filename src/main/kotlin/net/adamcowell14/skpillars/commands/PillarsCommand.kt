package net.adamcowell14.skpillars.commands

import net.adamcowell14.skpillars.SKPillars
import net.adamcowell14.skpillars.Arena
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class PillarsCommand(private val plugin: SKPillars) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cТолько для игроков!")
            return false
        }

        when (args.getOrNull(0)?.lowercase()) {
            "join" -> {
                if (args.size < 2) {
                    sender.sendMessage(plugin.messageHelper.parse("error-args"))
                    return false
                }

                if (plugin.cooldownGame) {
                    sender.sendMessage(plugin.messageHelper.parse("error-cooldown"))
                    return false
                }

                val arenaName = args[1]
                val playerName = sender.name

                if (!plugin.getArenaNames().contains(arenaName)) {
                    sender.sendMessage(plugin.messageHelper.parse("error-map-not-founded"))
                    return false
                }

                if (plugin.activeGames.isEmpty()) {
                    setupArena(arenaName)
                    plugin.logger.info("Setup arena")
                } else if (plugin.activeGames[arenaName]?.started == true) {
                    sender.sendMessage(plugin.messageHelper.parse("error-game-already-started"))
                    return false
                }

                val arena = plugin.activeGames.getOrPut(arenaName) { Arena(arenaName, plugin) }

                if (arena.players.containsKey(playerName)) {
                    sender.sendMessage(plugin.messageHelper.parse("error-already-in-game"))
                    return false
                }

                arena.addPlayer(playerName)
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    teleportArena(sender, arenaName)
                }, 2)

                return true
            }

            "spectate" -> {
                if (args.size < 2) {
                    sender.sendMessage(plugin.messageHelper.parse("error-args"))
                    return false
                }

                val arenaName = args[1]
                val playerName = sender.name

                if (!plugin.activeGames.keys.toList().contains(arenaName)) {
                    sender.sendMessage(plugin.messageHelper.parse("error-game-not-founded"))
                    return false
                }

                val arena = plugin.activeGames.getOrPut(arenaName) { Arena(arenaName, plugin) }

                if (!arena.started) {
                    sender.sendMessage(plugin.messageHelper.parse("error-game-already-not-started"))
                    return false
                }

                if (arena.players.containsKey(playerName)) {
                    sender.sendMessage(plugin.messageHelper.parse("error-already-in-game"))
                    return false
                }

                arena.addPlayer(playerName)
                arena.players[playerName]?.spec = true
                arena.players[playerName]?.lose = true

                val waitingLocation = plugin.config.getStringList("game.$arenaName.waiting_location")
                val worldGame = plugin.config.getString("world_all")?.let { Bukkit.getWorld(it) }

                val cords = waitingLocation[0].split(" ")
                val x = cords[0].toDouble()
                val y = cords[1].toDouble()
                val z = cords[2].toDouble()
                sender.teleport(Location(worldGame, x, y, z))
                sender.clearActivePotionEffects()
                sender.inventory.clear()
                sender.gameMode = GameMode.SPECTATOR

                return true
            }

            "leave" -> {
                val arenas = plugin.activeGames.keys
                for (key in arenas) {
                    if (plugin.activeGames[key]?.players?.containsKey(sender.player?.name) == true) {
                        plugin.activeGames[key]?.players?.remove(sender.player?.name)

                        if (plugin.activeGames[key]!!.players.isEmpty()) {
                            plugin.activeGames[key]?.declareWinner("none")
                        }

                        plugin.config.getString("game.$key.settings.back-command")?.let { sender.player?.performCommand(it)
                            ?: plugin.logger.warning("Leave command error") }
                    }
                }

                return true
            }

            else -> {
                sender.sendMessage("pashalko 1487".trimIndent())
                return false
            }
        }
    }

    private fun teleportArena(player: Player, arenaName: String) {
        val waitingLocation = plugin.config.getStringList("game.$arenaName.waiting_location")
        val worldGame = plugin.config.getString("world_all")?.let { Bukkit.getWorld(it) }

        val cords = waitingLocation[0].split(" ")
        val x = cords[0].toDouble()
        val y = cords[1].toDouble()
        val z = cords[2].toDouble()
//        plugin.scoreboardHelper.showScoreboard(player, "lobby-scoreboard")
        player.teleport(Location(worldGame, x, y, z))
        player.clearActivePotionEffects()
        player.inventory.clear()
        player.gameMode = GameMode.SURVIVAL
        player.health = 20.0
        player.foodLevel = 20

        player.inventory.setItem(0, plugin.customItems.leaveBed)
        player.inventory.setItem(4, plugin.customItems.statisticHead)
        player.inventory.setItem(8, plugin.customItems.statisticHead)

        plugin.scoreboardHelper.showScoreboard(player, "lobby-scoreboard")
    }

    private fun setupArena(arenaName: String) {
        val schematicName = plugin.config.getString("game.$arenaName.settings.schematic.name")
        val schematicCords = plugin.config.getString("game.$arenaName.settings.schematic.cords")?.split(" ")
        val worldGame = plugin.config.getString("world_all")?.let { Bukkit.getWorld(it) }
        val center: Any? = plugin.config.get("game.$arenaName.worldborder.center")

        // WORLD SETTINGS

        val (x2, z2) = when (center) {
            is List<*> -> {
                if (center.size >= 2) {
                    Pair(
                        center[0].toString().toDoubleOrNull() ?: 0.0,
                        center[1].toString().toDoubleOrNull() ?: 0.0
                    )
                } else {
                    Pair(0.0, 0.0)
                }
            }
            is String -> { // Строка "50 50"
                val coords = center.split(" ")
                if (coords.size >= 2) {
                    Pair(
                        coords[0].toDoubleOrNull() ?: 0.0,
                        coords[1].toDoubleOrNull() ?: 0.0
                    )
                } else {
                    Pair(0.0, 0.0)
                }
            }
            else -> {
                Pair(0.0, 0.0)
            }
        }
        worldGame?.worldBorder?.setCenter(x2, z2)
        plugin.config.getString("game.$arenaName.worldborder.diameter")?.toDouble()
            ?.let { worldGame?.worldBorder?.setSize(it) }

        worldGame?.time = plugin.config.getString("game.$arenaName.settings.time")?.toLong()!!

        worldGame?.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        worldGame?.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        worldGame?.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        worldGame?.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false)
        worldGame?.difficulty = Difficulty.PEACEFUL

        // SCHEMATIC LOAD

        if (schematicName != null) {
            schematicCords?.get(0)?.toDouble()?.let {
                Location(worldGame, it,
                    schematicCords[1].toDouble(),
                    schematicCords[2].toDouble()
                )
            }?.let {
                plugin.schematicManager.paste(schematicName,
                    it
                )
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("join", "leave", "spectate").filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> if (args[0].equals("join", ignoreCase = true)) {
                plugin.getArenaNames().filter { it.startsWith(args[1], ignoreCase = true) }
            } else if (args[0].equals("spectate", ignoreCase = true)) {
                plugin.activeGames.keys.toList().filter { it.startsWith(args[1], ignoreCase = true) }
            } else {
                emptyList()
            }
            else -> emptyList()
        }
    }
}