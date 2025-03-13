package com.holiday.simpleitemmaker.manager;

import com.holiday.simpleitemmaker.build.FactoryItem;
import com.holiday.simpleitemmaker.utils.MapUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class FactoryManager {
    Plugin plugin;

    public FactoryManager(Plugin plugin) {
        this.plugin = plugin;

        loadAllRecipes();
    }

    public void saveAllRecipes() {
        for (FactoryItem factoryItem : MapUtils.factoryItems) {
            String path = "recipes." + factoryItem.getName();

            plugin.getConfig().set(path + ".result", serializeItemStack(factoryItem.getResult()));
            plugin.getConfig().set(path + ".ingredients", factoryItem.getIngredients().stream()
                    .map(this::serializeItemStack)
                    .toList());
        }
        plugin.saveConfig();
    }

    void loadAllRecipes() {
        if (!plugin.getConfig().contains("recipes")) return;

        if (!plugin.getConfig().contains("recipes") || plugin.getConfig().getConfigurationSection("recipes") == null) {
            return;
        }

        for (String key : plugin.getConfig().getConfigurationSection("recipes").getKeys(false)) {
            String path = "recipes." + key;

            ItemStack result = deserializeItemStack(plugin.getConfig().getString(path + ".result"));
            List<ItemStack> ingredients = plugin.getConfig().getStringList(path + ".ingredients").stream()
                    .map(this::deserializeItemStack)
                    .toList();

            FactoryItem factoryItem = new FactoryItem(key, ingredients, result);
            MapUtils.factoryItems.add(factoryItem);
        }
    }

    private String serializeItemStack(ItemStack item) {
        if (item == null) return null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
            bukkitObjectOutputStream.writeObject(item);
            bukkitObjectOutputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
            BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
            ItemStack item = (ItemStack) bukkitObjectInputStream.readObject();
            bukkitObjectInputStream.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
