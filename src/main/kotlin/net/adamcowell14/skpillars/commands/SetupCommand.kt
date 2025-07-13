package net.adamcowell14.skpillars.commands

import net.adamcowell14.skpillars.SKPillars
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class SetupCommand(private val plugin: SKPillars) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        when (args[0].lowercase()) {
            "reload" -> {
                if (sender.hasPermission("pillars.admin.reload") || sender.isOp) {
                    plugin.reloadPlugin()
                    sender.sendMessage("§aКонфигурация плагина успешно перезагружена!")
                    return true
                } else {
                    sender.sendMessage("§cУ вас нет прав на эту команду!")
                    return false
                }
            }
            "setarena" -> {
                if (sender !is Player) {
                    sender.sendMessage("§cТолько для игроков!")
                    return false
                }

                if (args.size <= 1) {
                    sender.sendMessage(plugin.messageHelper.parse("error-args"))
                    return false
                }

                val positions = sender.player?.let { plugin.worldEditHelper.getSelectionBounds(it) }
                if (positions != null) {
                    val currentList = plugin.config.getList("game.${args[1]}.box_arena")?.toMutableList() ?: mutableListOf()
                    if (currentList.size >= 2) {
                        currentList[0] = "${positions.first.x} ${positions.first.y} ${positions.first.z}"
                        currentList[1] = "${positions.second.x} ${positions.second.y} ${positions.second.z}"

                        plugin.config.set("game.${args[1]}.box_arena", currentList)
                        plugin.config.save(plugin.dataFolder.absolutePath + "/config.yml")

                        sender.player?.sendMessage(plugin.messageHelper.parse("area-saved"))
                    }
                } else {
                    sender.sendMessage(plugin.messageHelper.parse("error-region-not-selected"))
                }
            }
            "setdeatharena" -> {
                if (sender !is Player) {
                    sender.sendMessage("§cТолько для игроков!")
                    return false
                }

                if (args.size <= 1) {
                    sender.sendMessage(plugin.messageHelper.parse("error-args"))
                    return false
                }

                val positions = sender.player?.let { plugin.worldEditHelper.getSelectionBounds(it) }
                if (positions != null) {
                    val currentList = plugin.config.getList("game.${args[1]}.death_arena")?.toMutableList() ?: mutableListOf()
                    if (currentList.size >= 2) {
                        currentList[0] = "${positions.first.x} ${positions.first.y} ${positions.first.z}"
                        currentList[1] = "${positions.second.x} ${positions.second.y} ${positions.second.z}"

                        plugin.config.set("game.${args[1]}.death_arena", currentList)
                        plugin.config.save(plugin.dataFolder.absolutePath + "/config.yml")

                        sender.player?.sendMessage(plugin.messageHelper.parse("area-saved"))
                    }
                } else {
                    sender.sendMessage(plugin.messageHelper.parse("error-region-not-selected"))
                }
            }
            "setspawnlocation" -> {
                if (sender !is Player) {
                    sender.sendMessage("§cТолько для игроков!")
                    return false
                }

                if (args.size <= 2) {
                    sender.sendMessage(plugin.messageHelper.parse("error-args"))
                    return false
                }

                val positions = sender.player?.location
                val currentList = plugin.config.getList("game.${args[1]}.spawn_location")?.toMutableList() ?: mutableListOf()

                if (currentList.size > args[2].toInt()) {
                    currentList[args[2].toInt()] = "${positions?.x} ${positions?.y} ${positions?.z}"

                    plugin.config.set("game.${args[1]}.spawn_location", currentList)

                    plugin.config.save(plugin.dataFolder.absolutePath + "/config.yml")
                    sender.player?.sendMessage(plugin.messageHelper.parse("area-saved"))
                } else if(currentList.size <= args[2].toInt()) {
                    if (positions != null) {
                        currentList.add("${positions.x} ${positions.y} ${positions.z}")
                    }
                    plugin.logger.info(currentList.toString())

                    plugin.config.set("game.${args[1]}.spawn_location", currentList)

                    plugin.config.save(plugin.dataFolder.absolutePath + "/config.yml")
                    sender.player?.sendMessage(plugin.messageHelper.parse("area-saved"))
                }
            }
            "setwaitinglocation" -> {
                if (sender !is Player) {
                    sender.sendMessage("§cТолько для игроков!")
                    return false
                }

                if (args.size <= 1) {
                    sender.sendMessage(plugin.messageHelper.parse("error-args"))
                    return false
                }

                val positions = sender.player?.location
                val currentList = plugin.config.getList("game.${args[1]}.waiting_location")?.toMutableList() ?: mutableListOf()

                currentList[0] = "${positions?.x} ${positions?.y} ${positions?.z}"

                plugin.config.set("game.${args[1]}.waiting_location", currentList)

                plugin.config.save(plugin.dataFolder.absolutePath + "/config.yml")
                sender.player?.sendMessage(plugin.messageHelper.parse("point-saved"))
            }
            "schematics" -> {
                if (sender !is Player) {
                    sender.sendMessage("§cТолько для игроков!")
                    return false
                }

                if (args.size <= 2) {
                    sender.sendMessage(plugin.messageHelper.parse("error-args"))
                    return false
                }

                when (args[1].lowercase()) {
                    "save" -> {
                        val positions = sender.player?.let { plugin.worldEditHelper.getSelectionBounds(it) }
                        if (positions != null) {
                            val out = plugin.schematicManager.save(args[2], positions.first, positions.second)
                            if (out) {
                                sender.sendMessage(plugin.messageHelper.parse("save", sender.player,  mapOf("name_schem" to args[2])))
                            } else {
                                sender.sendMessage(plugin.messageHelper.parse("error-save", sender.player, mapOf("name_schem" to args[2])))
                            }
                        } else {
                            sender.sendMessage(plugin.messageHelper.parse("error-region-not-selected"))
                        }
                    }
                    "paste" -> {
                        val out = sender.player?.let { plugin.schematicManager.paste(args[2], it.location) }
                        if (out == true) {
                            sender.sendMessage(plugin.messageHelper.parse("load", sender.player, mapOf("name_schem" to args[2])))
                        } else {
                            sender.sendMessage(plugin.messageHelper.parse("error-load", sender.player, mapOf("name_schem" to args[2])))
                        }
                    }
                }

                return true
            }
            else -> {
                sender.sendMessage("§cНеизвестная команда. Используйте /psetup reload")
                return false
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("reload", "schematics", "setarena", "setdeatharena", "setspawnlocation", "setwaitinglocation", "setreturnlocation").filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> {
                if (args[0] == "schematics") {
                    listOf("save", "paste").filter { it.startsWith(args[1], ignoreCase = true) }
                } else if (args[0] in listOf("setarena", "setdeatharena", "setspawnlocation", "setwaitinglocation", "setreturnlocation")) {
                    plugin.getArenaNames().filter { it.startsWith(args[1], ignoreCase = true) }
                } else { emptyList() }
            }
            3 -> {
                if (args[0] == "setspawnlocation") {
                    plugin.getIdsSpawn(args[1]).filter { it.startsWith(args[2], ignoreCase = true) }
                } else { emptyList() }
            }
            else -> emptyList()
        }
    }
}