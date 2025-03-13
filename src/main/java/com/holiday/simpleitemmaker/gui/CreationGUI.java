package com.holiday.simpleitemmaker.gui;

import com.holiday.simpleitemmaker.Main;
import com.holiday.simpleitemmaker.build.FactoryItem;
import com.holiday.simpleitemmaker.utils.MapUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreationGUI implements Listener {
    Map<Player, String> recipes = new HashMap<>();
    int[] resourceSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public CreationGUI(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player, String recipeName) {
        Inventory gui = Bukkit.createInventory(null, 54, "Create Recipe: " + recipeName);

        // Bordes de cristal
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7");
            border.setItemMeta(meta);
        }

        for (int i = 0; i < 54; i++) {
            gui.setItem(i, border);
        }

        int resultSlot = 40; // Slot del resultado
        int saveButtonSlot = 53; // Botón de guardar

        for (int slot : resourceSlots) {
            gui.setItem(slot, null);
        }
        gui.setItem(resultSlot, null);

        // Botón de guardar
        ItemStack saveButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta saveMeta = saveButton.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName("§aSave Recipe");
            saveButton.setItemMeta(saveMeta);
        }
        gui.setItem(saveButtonSlot, saveButton);

        player.openInventory(gui);
        recipes.put(player, recipeName);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!recipes.containsKey((Player) event.getWhoClicked())) return;
        if (event.getView().getTitle().equals("Create Recipe: " + recipes.get((Player) event.getWhoClicked()))) {
            event.setCancelled(false);

            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) event.setCancelled(true);
            }

            if (event.getRawSlot() == 53) {
                Inventory inventory = event.getInventory();
                List<ItemStack> resources = new ArrayList<>();
                for (int slot : resourceSlots) {
                    ItemStack item = inventory.getItem(slot);
                    if (item != null && item.getType() != Material.AIR) {
                        resources.add(item.clone());
                    }
                }

                ItemStack result = inventory.getItem(40);
                if (result != null && result.getType() != Material.AIR) {

                    FactoryItem factoryItem = new FactoryItem(recipes.get((Player) event.getWhoClicked()), resources, result.clone());
                    MapUtils.factoryItems.add(factoryItem);
                    event.getWhoClicked().sendMessage(getMessage("messages.recipe-success", "recipe", recipes.get((Player) event.getWhoClicked())));
                } else {
                    event.getWhoClicked().sendMessage(getMessage("messages.recipe-no-result", "recipe", recipes.get((Player) event.getWhoClicked())));
                }

                event.setCancelled(true);
                event.getWhoClicked().closeInventory();
            }
        }
    }

    private String getMessage(String path, String... placeholders) {
        FileConfiguration config = Main.getPlugin(Main.class).getConfig();
        String message = config.getString(path, "&cMessage not found: " + path);

        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        return message.replace("&", "§"); // Convertir colores
    }
}
