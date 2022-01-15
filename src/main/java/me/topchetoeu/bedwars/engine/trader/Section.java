package me.topchetoeu.bedwars.engine.trader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

public class Section {
    private ArrayList<Deal> items = new ArrayList<>();
    private ItemStack icon;
    
    public Deal getDeal(int i) {
        if (i < 0 || i >= items.size()) return null;
        return items.get(i);
    }
    
    public Section addDeal(Deal d) {
        items.add(d);
        return this;
    }
    public void removeDeal(Deal d) {
        items.remove(d);
    }
    
    public List<Deal> getDealPage(int size, int n) {
        return items.stream().skip(size * n).limit(size).collect(Collectors.toList());
    }
    public List<Deal> getDeals() {
        return new ArrayList<>(items);
    }
    
    public ItemStack getIcon() {
        return icon;
    }
    public void setIcon(ItemStack item) {
        icon = item;
    }
    
    public Section(ItemStack icon) {
        this.items = new ArrayList<>();
        this.icon = icon;
    }
    public Section(ItemStack icon, List<Deal> deals) {
        this.items = new ArrayList<>(deals);
        this.icon = icon;
    }
}
