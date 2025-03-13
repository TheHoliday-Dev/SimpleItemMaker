package com.holiday.simpleitemmaker.commands;

import com.holiday.simpleitemmaker.build.FactoryItem;
import com.holiday.simpleitemmaker.gui.CraftGUI;
import com.holiday.simpleitemmaker.utils.MapUtils;
import com.holiday.simpleitemmaker.Main;
import com.holiday.simpleitemmaker.gui.CreationGUI;
import com.holiday.simpleitemmaker.gui.EditGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {
    CreationGUI creationGUI;
    CraftGUI craftGUI;
    EditGUI editGUI;

    public MainCommand(CreationGUI creationGUI, CraftGUI craftGUI, EditGUI editGUI) {
        this.creationGUI = creationGUI;
        this.craftGUI = craftGUI;
        this.editGUI = editGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                player.sendMessage(getMessage("messages.command-usage"));
                return true;
            }

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("create")) {
                if (!player.hasPermission("itemmarker.admin")) return true;
                if (args.length < 2) {
                    player.sendMessage(getMessage("messages.recipe-no"));
                    return true;
                }
                String recipeName = args[1];

                if (MapUtils.factoryItems.stream().anyMatch(n -> n.getName().equalsIgnoreCase(recipeName))) {
                    player.sendMessage(getMessage("messages.recipe-exists", "recipe", recipeName));
                    return true;
                }

                creationGUI.openGUI(player, recipeName);
                player.sendMessage(getMessage("messages.recipe-creation-started", "recipe", recipeName));
                return true;
            } else if (subCommand.equalsIgnoreCase("craft")) {
                if (args.length < 2) {
                    player.sendMessage(getMessage("messages.recipe-no"));
                    return true;
                }
                String recipeName = args[1];
                craftGUI.openGUI(player, recipeName);
                player.sendMessage(getMessage("messages.recipe-crafting-started", "recipe", recipeName));
                return true;
            } else if (subCommand.equalsIgnoreCase("reload")) {
                if (!player.hasPermission("itemmarker.admin")) return true;
                player.sendMessage(getMessage("messages.reload-plugin"));
                Main.getPlugin(Main.class).reloadConfig();
                return true;
            } else if (subCommand.equalsIgnoreCase("data")) {
                if (!player.hasPermission("itemmarker.admin")) return true;
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.getItemMeta() != null) {
                    player.sendMessage(getMessage("messages.item-model", "model", "" + itemStack.getItemMeta().getCustomModelData()));
                }
                return true;
            } else if (subCommand.equalsIgnoreCase("edit")) {
                if (!player.hasPermission("itemmarker.admin")) return true;
                if (args.length < 2) {
                    player.sendMessage(getMessage("messages.recipe-no"));
                    return true;
                }
                String recipeName = args[1];

                if (MapUtils.factoryItems.stream().noneMatch(n -> n.getName().equalsIgnoreCase(recipeName))) {
                    player.sendMessage(getMessage("messages.recipe-not-found", "recipe", recipeName));
                    return true;
                }

                editGUI.openEditGUI(player, recipeName);
                player.sendMessage(getMessage("messages.recipe-edit-started", "recipe", recipeName));
                return true;
            } else if (subCommand.equalsIgnoreCase("delete")) {
                if (!player.hasPermission("itemmarker.admin")) return true;
                if (args.length < 2) {
                    player.sendMessage(getMessage("messages.recipe-no"));
                    return true;
                }
                String recipeName = args[1];
                if (MapUtils.factoryItems.stream().noneMatch(n -> n.getName().equalsIgnoreCase(recipeName))) {
                    player.sendMessage(getMessage("messages.recipe-not-found", "recipe", recipeName));
                    return true;
                }
                FactoryItem factoryItem = MapUtils.factoryItems.stream().filter(n -> n.getName().equalsIgnoreCase(recipeName))
                        .findFirst().orElse(null);

                MapUtils.factoryItems.remove(factoryItem);
                FileConfiguration config = Main.getPlugin(Main.class).getConfig();
                config.set("recipes." + recipeName, null);
                Main.getPlugin(Main.class).saveConfig();
                player.sendMessage(getMessage("messages.recipe-deleted", "recipe", recipeName));
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("create");
            suggestions.add("craft");
            suggestions.add("reload");
            suggestions.add("data");
            suggestions.add("edit");
            suggestions.add("delete");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("craft") || args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("delete")) {
                suggestions.addAll(MapUtils.factoryItems.stream().map(FactoryItem::getName).collect(Collectors.toList()));
            }
        }
        return suggestions;
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
