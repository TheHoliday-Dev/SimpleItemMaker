package com.holiday.simpleitemmaker.build;

import com.holiday.simpleitemmaker.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class PlayerCraft {
    Inventory inventory;
    InventoryView inventoryView;
    Player player;
    String recipe;
    BukkitTask task;

    int animationIndex = 0;

    public PlayerCraft(Inventory inventory, InventoryView inventoryView, Player player, String recipe) {
        this.inventory = inventory;
        this.inventoryView = inventoryView;
        this.player = player;
        this.recipe = recipe;
    }

    public void startAnimation(Runnable runnable) {
        FileConfiguration config = Main.getPlugin(Main.class).getConfig();

        List<String> frames = config.getConfigurationSection("animation-gui").getValues(false)
                .entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(Integer.parseInt(e1.getKey()), Integer.parseInt(e2.getKey())))
                .map(e -> (String) e.getValue())
                .toList();

        int interval = config.getInt("animation-speed", 5);

        task = new BukkitRunnable() {
            @Override
            public void run() {

                if (animationIndex >= frames.size()) {
                    this.cancel();
                    animationIndex = 0;

                    if (runnable != null) {
                        runnable.run();
                    }
                    return;
                }

                String title = frames.get(animationIndex);
                title = ChatColor.translateAlternateColorCodes('&', title);
                title = PlaceholderAPI.setPlaceholders(player, title);
                inventoryView.setTitle(title);
                animationIndex++;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, interval);
    }

    public String getRecipe() {
        return recipe;
    }

    public int getAnimationIndex() {
        return animationIndex;
    }

    public BukkitTask getTask() {
        return task;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public InventoryView getInventoryView() {
        return inventoryView;
    }

    public void setInventoryView(InventoryView inventoryView) {
        this.inventoryView = inventoryView;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
