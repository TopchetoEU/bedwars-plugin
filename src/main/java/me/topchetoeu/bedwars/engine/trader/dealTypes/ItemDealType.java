package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.trader.Deal;
import me.topchetoeu.bedwars.engine.trader.DealType;
import me.topchetoeu.bedwars.engine.trader.DealTypes;

public class ItemDealType implements DealType {
	
	@SuppressWarnings("unchecked")
	@Override
	public Deal parse(Map<String, Object> map) {
		boolean implemented = !map.containsKey("implemented") || (boolean)map.get("implemented");
		int price = (Integer)map.get("price");
		Material priceType = Material.getMaterial(((String)map.get("priceType")).toUpperCase());
		ItemStack type = Utility.deserializeItemStack((Map<String, Object>)map.get("item"));
		
		return new ItemDeal(type, price, priceType, implemented);
	}

	@Override
	public String getId() {
		return "item";
	}
	
	public static void init() {
		DealTypes.register(new ItemDealType());
	}
}
