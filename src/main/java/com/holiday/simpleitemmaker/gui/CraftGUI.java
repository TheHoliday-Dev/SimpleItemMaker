package com.holiday.simpleitemmaker.gui;

import com.holiday.simpleitemmaker.Main;
import com.holiday.simpleitemmaker.build.FactoryItem;
import com.holiday.simpleitemmaker.build.PlayerCraft;
import com.holiday.simpleitemmaker.utils.MapUtils;
import de.tr7zw.nbtapi.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CraftGUI implements Listener {
    private final Plugin plugin;
    private final int[] resourceSlots = {2, 3, 4, 5, 6, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24};
    private final int craftButtonSlot = 33;
    private final int resultSlot = 40;

    public CraftGUI(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player, String recipeName) {
        FactoryItem factoryItem = MapUtils.factoryItems.stream().filter(a -> a.getName().equalsIgnoreCase(recipeName)).findFirst().orElse(null);
        if (factoryItem == null) {
            player.sendMessage("§cRecipe not found!");
            return;
        }

        String title = plugin.getConfig().getString("title-craft");
        title = ChatColor.translateAlternateColorCodes('&', title);
        title = PlaceholderAPI.setPlaceholders(player, title);

        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7");
            border.setItemMeta(meta);
        }

        List<ItemStack> ingredients = factoryItem.getIngredients();
        for (int i = 0; i < resourceSlots.length && i < ingredients.size(); i++) {
            gui.setItem(resourceSlots[i], ingredients.get(i));
        }

        // gui.setItem(resultSlot, factoryItem.getResult());

        String materialButton = plugin.getConfig().getString("button-craft.material");
        int model = plugin.getConfig().getInt("button-craft.model");
        String name = plugin.getConfig().getString("button-craft.name");
        name = ChatColor.translateAlternateColorCodes('&', name);

        ItemStack craftButton = new ItemStack(Material.valueOf(materialButton.toUpperCase()));
        ItemMeta craftMeta = craftButton.getItemMeta();
        if (craftMeta != null) {
            craftMeta.setDisplayName(name);
            craftMeta.setCustomModelData(model);
            craftButton.setItemMeta(craftMeta);
        }
        gui.setItem(craftButtonSlot, craftButton);
        player.openInventory(gui);
        PlayerCraft playerCraft = new PlayerCraft(gui, player.getOpenInventory(), player, recipeName);
        MapUtils.playerCrafts.add(playerCraft);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (MapUtils.playerCrafts.stream().noneMatch(p -> p.getPlayer().equals(player))) return;
        PlayerCraft playerCraft = MapUtils.playerCrafts.stream().filter(p -> p.getPlayer().equals(player))
                .findFirst().orElse(null);
        if (playerCraft == null) return;
        if (event.getClickedInventory() == null) return;

        if (playerCraft.getAnimationIndex() != 0) {
            event.setCancelled(true);
        }

        if (event.getClick().toString().contains("SHIFT")) {
            event.setCancelled(true);
        }

        if (!event.getClickedInventory().equals(playerCraft.getInventory())) return;


        if (event.getRawSlot() != resultSlot) {
            event.setCancelled(true);
        }

        if (event.getRawSlot() == resultSlot) {
            if (event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE) {
                event.setCancelled(true);
            }
            ItemStack item = playerCraft.getInventory().getItem(event.getRawSlot());
            if (item == null) return;
            player.getInventory().addItem(item.clone());
            item.setAmount(0);
        }

        if (event.getRawSlot() == craftButtonSlot) {
            String recipeName = playerCraft.getRecipe();
            FactoryItem factoryItem = MapUtils.factoryItems.stream().filter(a -> a.getName().equalsIgnoreCase(recipeName)).findFirst().orElse(null);
            if (factoryItem == null) return;
            if (playerCraft.getAnimationIndex() != 0) return;

            String sound = plugin.getConfig().getString("sound-craft");

            player.playSound(player, sound, SoundCategory.PLAYERS, 1, 1);

            playerCraft.startAnimation(() -> {
                if (!hasIngredients(player, factoryItem.getIngredients())) {
                    player.sendMessage(getMessage("messages.crafted-ingredients", "recipe", recipeName));
                    String titleFailed = plugin.getConfig().getString("title-failed");
                    titleFailed = ChatColor.translateAlternateColorCodes('&', titleFailed);
                    titleFailed = PlaceholderAPI.setPlaceholders(player, titleFailed);
                    playerCraft.getInventoryView().setTitle(titleFailed);
                    return;
                }

                removeIngredientsPlayer(player, factoryItem.getIngredients());

                ItemStack itemResult = playerCraft.getInventory().getItem(resultSlot);
                if (itemResult == null) {
                    playerCraft.getInventory().setItem(resultSlot, factoryItem.getResult());
                } else {
                    itemResult.setAmount(itemResult.getAmount() + factoryItem.getResult().getAmount());
                }

                player.sendMessage(getMessage("messages.crafted-success", "recipe", recipeName));
                String titleSuccess = plugin.getConfig().getString("title-success");
                titleSuccess = ChatColor.translateAlternateColorCodes('&', titleSuccess);
                titleSuccess = PlaceholderAPI.setPlaceholders(player, titleSuccess);
                playerCraft.getInventoryView().setTitle(titleSuccess);
            });
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (MapUtils.playerCrafts.stream().noneMatch(p -> p.getPlayer().equals(player))) return;
        PlayerCraft playerCraft = MapUtils.playerCrafts.stream().filter(p -> p.getPlayer().equals(player))
                .findFirst().orElse(null);
        if (playerCraft != null) {
            if (playerCraft.getInventory().getItem(resultSlot) != null) {
                player.getInventory().addItem(playerCraft.getInventory().getItem(resultSlot));
            }
            if (playerCraft.getTask() != null) {
                playerCraft.getTask().cancel();
            }
        }
        MapUtils.playerCrafts.removeIf(p -> p.getPlayer().equals(player));
    }

    private void removeIngredientsPlayer(Player player, List<ItemStack> ingredients) {

        for (ItemStack ingredient : ingredients) {
            int neededAmount = ingredient.getAmount();
            int foundAmount = 0;

            boolean isUpgrade = false;
            String nameInternal = "";
            int levelMin = 0;

            NBTItem nbtItem = new NBTItem(ingredient);
            if (nbtItem.hasTag("GAYA_SOFT_STRENGTH")) {
                int levelF = nbtItem.getInteger("GAYA_SOFT_STRENGTH");
                if (levelF != 0) {
                    isUpgrade = true;
                    levelMin = levelF;
                    nameInternal = nbtItem.getString("MMOITEMS_ITEM_ID");
                }
            }

            for (ItemStack item : player.getInventory().getContents()) {
                if (isUpgrade) {
                    if (item != null) {
                        NBTItem nbtItemP = new NBTItem(item);
                        if (nbtItemP.hasTag("MMOITEMS_ITEM_ID")) {
                            if (nbtItemP.getString("MMOITEMS_ITEM_ID").equals(nameInternal)) {
                                if (nbtItemP.hasTag("GAYA_SOFT_STRENGTH")) {
                                    int levelF = nbtItemP.getInteger("GAYA_SOFT_STRENGTH"); // Aquí se usa el NBT correcto
                                    if (levelF != 0 && levelF >= levelMin) {
                                        foundAmount += item.getAmount();
                                        if (foundAmount >= neededAmount) {
                                            item.setAmount(item.getAmount() - neededAmount);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (item != null && item.isSimilar(ingredient)) {
                    if (!isUpgrade) {
                        foundAmount += item.getAmount();
                        if (foundAmount >= neededAmount) {
                            item.setAmount(item.getAmount() - neededAmount);
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean hasIngredients(Player player, List<ItemStack> ingredients) {
        for (ItemStack ingredient : ingredients) {
            int neededAmount = ingredient.getAmount();
            int foundAmount = 0;

            boolean isUpgrade = false;
            String nameInternal = "";
            int levelMin = 0;

            NBTItem nbtItem = new NBTItem(ingredient);
            if (nbtItem.hasTag("GAYA_SOFT_STRENGTH")) {
                int levelF = nbtItem.getInteger("GAYA_SOFT_STRENGTH");
                if (levelF != 0) {
                    isUpgrade = true;
                    levelMin = levelF;
                    nameInternal = nbtItem.getString("MMOITEMS_ITEM_ID");
                }
            }

            for (ItemStack item : player.getInventory().getContents()) {
                if (isUpgrade) {
                    if (item != null) {
                        NBTItem nbtItemP = new NBTItem(item);
                        if (nbtItemP.hasTag("MMOITEMS_ITEM_ID")) {
                            if (nbtItemP.getString("MMOITEMS_ITEM_ID").equals(nameInternal)) {
                                if (nbtItemP.hasTag("GAYA_SOFT_STRENGTH")) {
                                    int levelF = nbtItemP.getInteger("GAYA_SOFT_STRENGTH"); // Aquí se usa el NBT correcto
                                    if (levelF != 0 && levelF >= levelMin) {
                                        foundAmount += item.getAmount();
                                        if (foundAmount >= neededAmount) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (item != null && item.isSimilar(ingredient)) {
                    if (!isUpgrade) {
                        foundAmount += item.getAmount();
                        if (foundAmount >= neededAmount) {
                            break;
                        }
                    }
                }
            }

            if (foundAmount < neededAmount) {
                return false;
            }
        }
        return true;
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
