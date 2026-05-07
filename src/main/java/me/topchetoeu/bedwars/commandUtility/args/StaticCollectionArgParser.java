package me.topchetoeu.bedwars.commandUtility.args;

import java.util.Collections;
import java.util.Map;

public class StaticCollectionArgParser extends CollectionArgParser {
    private Map<String, Object> map;

    public Map<String, Object> getMap() {
        return Collections.unmodifiableMap(map);
    }

    public StaticCollectionArgParser(Map<String, Object> map, boolean caseInsensitive) {
        super(() -> map, caseInsensitive);
        this.map = map;
    }
}
