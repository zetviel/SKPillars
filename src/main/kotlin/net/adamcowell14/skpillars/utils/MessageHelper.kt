package net.adamcowell14.skpillars.utils

import me.clip.placeholderapi.PlaceholderAPI
import net.adamcowell14.skpillars.SKPillars
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.util.regex.Pattern

class MessageHelper(private val plugin: SKPillars) {
    private val legacyColorPattern: Pattern = Pattern.compile("&([0-9a-fk-orA-FK-OR])|§([0-9a-fk-orA-FK-OR])")

    fun parse(messageString: String, player: Player? = null, placeholders: Map<String, String> = mapOf()): Component {
        try {
            var message = plugin.messagesConfig.getString(messageString)
            val prefix = plugin.messagesConfig.getString("prefix")

            if (message == null) message = messageString
            message = PlaceholderAPI.setPlaceholders(player, message)

            val mm = MiniMessage.miniMessage()

            for (ph in placeholders.keys) {
                try {
                    message = message?.replace("{$ph}", placeholders.getValue(ph))
                } catch (err: Exception) {
                    plugin.logger.warning("Placeholder ${placeholders.getValue(ph)} error: " + err)
                }
            }

            if (prefix != null) message = message?.replace("{PREFIX}", prefix)

            val legacyProcessed = legacyColorPattern.matcher(message).replaceAll {
                when (it.group(1).lowercase()) {
                    "0" -> "<black>"
                    "1" -> "<dark_blue>"
                    "2" -> "<dark_green>"
                    "3" -> "<dark_aqua>"
                    "4" -> "<dark_red>"
                    "5" -> "<dark_purple>"
                    "6" -> "<gold>"
                    "7" -> "<gray>"
                    "8" -> "<dark_gray>"
                    "9" -> "<blue>"
                    "a" -> "<green>"
                    "b" -> "<aqua>"
                    "c" -> "<red>"
                    "d" -> "<light_purple>"
                    "e" -> "<yellow>"
                    "f" -> "<white>"
                    "k" -> "<obfuscated>"
                    "l" -> "<bold>"
                    "m" -> "<strikethrough>"
                    "n" -> "<underlined>"
                    "o" -> "<italic>"
                    "r" -> "<reset>"
                    else -> it.group()
                }
            }

            if (legacyProcessed != null) {
                val messageParsed = mm.deserialize(legacyProcessed)
                return messageParsed
            } else {
                return Component.text("Null message")
            }
        } catch (e: Exception) {
            return Component.text("Error $e")
        }
    }
}