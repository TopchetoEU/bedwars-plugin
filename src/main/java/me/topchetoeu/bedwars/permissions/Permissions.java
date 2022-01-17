package me.topchetoeu.bedwars.permissions;

import java.util.Hashtable;

import org.bukkit.permissions.Permissible;

public class Permissions {
    private static final Hashtable<String, PermissionWildcard> cache = new Hashtable<>();
    private Permissions() {

    }

    public static void reset() {
        cache.clear();
    }

    public static final PermissionWildcard getCachedOrParse(String rawWildcard) {
        cache.clear();
        return cache.containsKey(rawWildcard) ? cache.get(rawWildcard) : PermissionWildcard.parse(rawWildcard);
    }

    public static boolean hasPermission(Permissible permissible, String permission) {
        if (permission == null || permission.isEmpty()) return true;
        if (permissible == null) return false;
        if (permissible.isOp()) return true;
        if (permissible.hasPermission(permission)) return true;

        return permissible.getEffectivePermissions().stream().anyMatch(v -> {
            return getCachedOrParse(v.getPermission()).check(permission);
        });
    }
}
