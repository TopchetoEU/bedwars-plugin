package me.topchetoeu.bedwars.commandUtility.args;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerArgParser extends CollectionArgParser {

    public PlayerArgParser(boolean caseInsensitive) {
        super(() -> Bukkit.getOnlinePlayers().stream().collect(Collectors.toMap(Player::getName, v->v)), caseInsensitive);
    }
}
