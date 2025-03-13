package com.holiday.simpleitemmaker.build;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FactoryItem {
    String name;

    List<ItemStack> resources;
    ItemStack result;

    public FactoryItem(String name, List<ItemStack> resources, ItemStack result) {
        this.name = name;
        this.resources = resources;
        this.result = result;
    }

    public void setResources(List<ItemStack> resources) {
        this.resources = resources;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public ItemStack getResult() {
        return result;
    }

    public List<ItemStack> getIngredients() {
        return resources;
    }
}
