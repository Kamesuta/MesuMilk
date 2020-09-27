package net.teamfruit.mesumilk;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public final class MesuMilk extends JavaPlugin implements Listener {
    private List<String> mesu = new ArrayList<>();
    private List<String> water = new ArrayList<>();
    private Objective takeCount;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();

        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        takeCount = sb.getObjective("mesumilk");
        if (takeCount == null)
            takeCount = sb.registerNewObjective("mesumilk", "dummy", "搾られた回数");

        @SuppressWarnings("unchecked")
        List<String> mesu = (List<String>) getConfig().getList("mesu");
        this.mesu = mesu;

        @SuppressWarnings("unchecked")
        List<String> water = (List<String>) getConfig().getList("water");
        this.water = water;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player me = event.getPlayer();
        PlayerInventory inv = me.getInventory();
        EquipmentSlot hand = event.getHand();
        ItemStack bucket = inv.getItem(hand);
        if (bucket.getType() != Material.BUCKET)
            return;

        Entity entity = event.getRightClicked();
        if (entity instanceof Player) {
            Player target = (Player) entity;
            boolean bMesu = mesu.contains(target.getName());
            boolean bWater = water.contains(target.getName());
            if (bMesu || bWater) {
                me.playSound(me.getLocation(), Sound.ENTITY_COW_MILK, 1, 1);

                ItemStack milk = new ItemStack(bWater ? Material.WATER_BUCKET : Material.MILK_BUCKET);
                ItemMeta meta = milk.getItemMeta();
                meta.setDisplayName("§6" + target.getName() + " の牛乳");
                milk.setItemMeta(meta);
                bucket.setAmount(bucket.getAmount() - 1);
                if (inv.firstEmpty() == -1)
                    me.getWorld().dropItem(me.getLocation(), milk);
                else
                    inv.addItem(milk);
                getServer().dispatchCommand(getServer().getConsoleSender(),
                        "execute at " + target.getName() + " run particle minecraft:spit ~ ~ ~ 1 0 0 1 1000 force");

                Score sc = takeCount.getScore(target.getName());
                sc.setScore(sc.getScore() + 1);
            }
        }
    }

}
