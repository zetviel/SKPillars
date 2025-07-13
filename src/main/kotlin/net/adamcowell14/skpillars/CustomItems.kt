package net.adamcowell14.skpillars

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class CustomItems(private val plugin: SKPillars) {
    lateinit var statisticHead: ItemStack
    lateinit var leaveBed: ItemStack

    fun init() {
        statisticHead = statisticHead()
        leaveBed = leaveBed()
    }

    private fun statisticHead() : ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val meta = item.itemMeta as SkullMeta
        val texture_base64 = plugin.messagesConfig.getString("item-statisticHead-texture") ?: ""

        val profile: PlayerProfile = Bukkit.createProfile(UUID.randomUUID(), null)
        profile.setProperty(
            ProfileProperty(
                "textures",
                texture_base64
            )
        )

        val rawLore = plugin.messagesConfig.getStringList("item-statisticHead-lore")
        val parsedLore = rawLore.map { line ->
            plugin.messageHelper.parse(line)
        }

        meta.displayName(plugin.messageHelper.parse("item-statisticHead-name"))
        meta.lore(parsedLore)
        meta.playerProfile = profile
        item.itemMeta = meta

        return item
    }

    private fun leaveBed(): ItemStack {
        val item = ItemStack(Material.RED_BED)
        val meta = item.itemMeta

        val rawLore = plugin.messagesConfig.getStringList("item-leaveBed-lore")
        val parsedLore = rawLore.map { line ->
            plugin.messageHelper.parse(line)
        }

        meta.displayName(plugin.messageHelper.parse("item-leaveBed-name"))
        meta.lore(parsedLore)

        item.itemMeta = meta
        return item
    }
}