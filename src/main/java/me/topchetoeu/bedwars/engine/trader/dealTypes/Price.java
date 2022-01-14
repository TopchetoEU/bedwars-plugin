package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Map;

import org.bukkit.Material;

public class Price {
	private int price;
	private Material priceType;
	private String displayName;
	
	public int getPrice() {
		return price;
	}
	public Material getPriceType() {
		return priceType;
	}
	public String getDisplayName() {
		return displayName;
	}
	
	public static Price deserialize(Map<String, Object> map) {
		int price = (Integer)map.get("price");
		Material priceType = Material.getMaterial(((String)map.get("priceType")).toUpperCase());
		String displayName = (String)map.get("displayName");
		
		return new Price(price, priceType, displayName);
	}
	
	public Price(int price, Material priceType, String displayName) {
		this.price = price;
		this.priceType = priceType;
		this.displayName = displayName;
	}
}
