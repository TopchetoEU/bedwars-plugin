package me.topchetoeu.bedwars.commands.args;

import java.util.Hashtable;
import java.util.Map;

public class EnumArgParser extends StaticCollectionArgParser {

    private static Map<String, Object> getCol(Class<? extends Enum<?>> enumType, boolean caseInsensitive) {
        Map<String, Object> map = new Hashtable<>();
        if (enumType.getEnumConstants() != null) {
            for (Enum<?> c : enumType.getEnumConstants()) {
                if (caseInsensitive) map.put(c.name().toLowerCase(), c);
                else map.put(c.name(), c);
            }
        }

        return map;
    }

    public EnumArgParser(Class<? extends Enum<?>> enumType, boolean caseInsensitive) {
        super(getCol(enumType, caseInsensitive), caseInsensitive);
    }

}
