package com.holiday.simpleitemmaker;

import com.holiday.simpleitemmaker.commands.MainCommand;
import com.holiday.simpleitemmaker.gui.CraftGUI;
import com.holiday.simpleitemmaker.gui.CreationGUI;
import com.holiday.simpleitemmaker.gui.EditGUI;
import com.holiday.simpleitemmaker.manager.FactoryManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    FactoryManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        CraftGUI craftGUI = new CraftGUI(this);
        CreationGUI creationGUI = new CreationGUI(this);
        EditGUI editGUI = new EditGUI(this);

        manager = new FactoryManager(this);

        getCommand("itemmaker").setExecutor(new MainCommand(creationGUI, craftGUI, editGUI));
    }

    @Override
    public void onDisable() {
        manager.saveAllRecipes();
    }
}
