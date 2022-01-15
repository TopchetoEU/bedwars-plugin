package me.topchetoeu.bedwars.engine.trader;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.Utility;

@SuppressWarnings("unchecked")
public class Sections {
    public static File SECTIONS_FILE;
    
    private static List<Section> sections;
    
    private static Deal deserializeDeal(Map<String, Object> raw) {
        String type = (String)raw.get("type");
        DealType dealType = DealTypes.get(type);
        
        if (dealType == null) throw new RuntimeException(String.format("Deal type %s is not recognised.", type));
        
        return dealType.parse(raw);
    }
    private static Section deserializeSection(Map<String, Object> raw) {
        List<Deal> deals = ((List<Map<?, ?>>)raw.get("deals"))
            .stream()
            .map(v -> deserializeDeal((Map<String, Object>)v))
            .collect(Collectors.toList());
        ItemStack icon = Utility.deserializeItemStack((Map<String, Object>)raw.get("iconItem"));
        
        return new Section(icon, deals);
    }
    
    public static List<Section> getSections() {
        return sections;
    }
    
    public static void init(File sectionsFile) {
        SECTIONS_FILE = sectionsFile;
        sections = YamlConfiguration
            .loadConfiguration(SECTIONS_FILE)
            .getMapList("sections")
            .stream()
            .map(v -> deserializeSection((Map<String, Object>)v))
            .collect(Collectors.toList());
    }
}
