package me.topchetoeu.bedwars.engine.trader;

import java.util.Hashtable;

public class DealTypes {
	private static Hashtable<String, DealType> dealTypes = new Hashtable<>();
	
	private DealTypes() {
		
	}
	
	public static boolean register(DealType type) {
		if (dealTypes.contains(type.getId())) return false;
		dealTypes.put(type.getId(), type);
		return true;
	}
	public static DealType get(String id) {
		return dealTypes.get(id);
	}
}
