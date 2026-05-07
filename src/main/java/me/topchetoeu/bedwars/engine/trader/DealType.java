package me.topchetoeu.bedwars.engine.trader;

import java.util.Map;

public interface DealType {
    public Deal parse(Map<String, Object> map);
    public String getId();
}
