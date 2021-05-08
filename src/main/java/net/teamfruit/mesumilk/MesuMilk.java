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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MesuMilk extends JavaPlugin implements Listener {
    // 牛乳が出る人
    private List<String> mesu = new ArrayList<>();
    // 水が出る人
    private List<String> water = new ArrayList<>();
    // 精が出る人
    private Map<String, Object> osu = new HashMap<>();
    // 溶岩が出る人
    private List<String> lava = new ArrayList<>();
    // スコアボード (搾られた回数)
    private Objective mesuCount;
    // スコアボード (搾られた回数)
    private Objective osuCount;
    // スコアボード (搾った回数)
    private Objective mesuGetCount;
    // スコアボード (搾った回数)
    private Objective osuGetCount;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();

        // スコアボード
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        mesuCount = sb.getObjective("mesumilk");
        if (mesuCount == null)
            mesuCount = sb.registerNewObjective("mesumilk", "dummy", "メス搾られた回数");
        osuCount = sb.getObjective("osumilk");
        if (osuCount == null)
            osuCount = sb.registerNewObjective("osumilk", "dummy", "オス搾られた回数");
        mesuGetCount = sb.getObjective("mesugetmilk");
        if (mesuGetCount == null)
            mesuGetCount = sb.registerNewObjective("mesugetmilk", "dummy", "メス搾った回数");
        osuGetCount = sb.getObjective("osugetmilk");
        if (osuGetCount == null)
            osuGetCount = sb.registerNewObjective("osugetmilk", "dummy", "オス搾った回数");

        // メンバー読み込み
        @SuppressWarnings("unchecked")
        List<String> mesu = (List<String>) getConfig().getList("mesu");
        this.mesu = mesu;

        @SuppressWarnings("unchecked")
        List<String> water = (List<String>) getConfig().getList("water");
        this.water = water;

        @SuppressWarnings("unchecked")
        Map<String, Object> osu = (Map<String, Object>) getConfig().getConfigurationSection("osu").getValues(false);
        this.osu = osu;

        @SuppressWarnings("unchecked")
        List<String> lava = (List<String>) getConfig().getList("lava");
        this.lava = lava;
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

        // 空バケツの場合
        if (bucket.getType() != Material.BUCKET)
            return;

        // プレイヤー右クリック時
        Entity entity = event.getRightClicked();
        if (entity instanceof Player) {
            Player target = (Player) entity;

            // メスの場合
            boolean bMesu = mesu.contains(target.getName());
            boolean bWater = water.contains(target.getName());
            if (bMesu || bWater) {
                // ミルクを搾る音
                me.playSound(me.getLocation(), Sound.ENTITY_COW_MILK, 1, 1);

                // アイテム処理
                ItemStack milk = new ItemStack(bWater ? Material.WATER_BUCKET : Material.MILK_BUCKET);
                ItemMeta meta = milk.getItemMeta();
                meta.setDisplayName("§6" + target.getName() + " の牛乳");
                milk.setItemMeta(meta);
                bucket.setAmount(bucket.getAmount() - 1);
                if (inv.firstEmpty() == -1)
                    me.getWorld().dropItem(me.getLocation(), milk);
                else
                    inv.addItem(milk);

                // パーティクル
                getServer().dispatchCommand(getServer().getConsoleSender(),
                        "execute at " + target.getName() + " run particle minecraft:spit ~ ~ ~ 1 0 0 1 1000 force");
                // スコア (搾られた)
                Score sc = mesuCount.getScore(target.getName());
                sc.setScore(sc.getScore() + 1);
                // スコア (搾った)
                Score scget = mesuGetCount.getScore(me.getName());
                scget.setScore(scget.getScore() + 1);
            } else if (getConfig().getBoolean("enable_osu", true)) {
                // メスがバケツした場合のみ
                boolean bMesuBy = mesu.contains(me.getName());
                boolean bWaterBy = water.contains(me.getName());
                boolean bLava = lava.contains(target.getName());
                if (bMesuBy || bWaterBy || bLava) {
                    // ミルクを搾る音
                    me.playSound(me.getLocation(), Sound.ENTITY_COW_MILK, 1, 1);

                    // アイテム処理
                    ItemStack milk = new ItemStack(bLava ? Material.LAVA_BUCKET : Material.MILK_BUCKET);
                    ItemMeta meta = milk.getItemMeta();
                    meta.setDisplayName("§3" + target.getName() + " の牛乳");
                    milk.setItemMeta(meta);
                    bucket.setAmount(bucket.getAmount() - 1);
                    if (inv.firstEmpty() == -1)
                        me.getWorld().dropItem(me.getLocation(), milk);
                    else
                        inv.addItem(milk);

                    Object osuObject = osu.get(target.getName());
                    int osuAmount = (osuObject instanceof Integer) ? (Integer) osuObject : 100;

                    // パーティクル
                    getServer().dispatchCommand(getServer().getConsoleSender(),
                            "execute at " + target.getName() + " anchored feet positioned ^ ^.5 ^.4 run particle minecraft:spit ^ ^ ^ 0 0 0 0 " + osuAmount + " normal");
                    // スコア (搾られた)
                    Score sc = osuCount.getScore(target.getName());
                    sc.setScore(sc.getScore() + 1);
                    // スコア (搾った)
                    Score scget = osuGetCount.getScore(me.getName());
                    scget.setScore(scget.getScore() + 1);
                }
            }
        }
    }

}
