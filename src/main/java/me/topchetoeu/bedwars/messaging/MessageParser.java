package me.topchetoeu.bedwars.messaging;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.topchetoeu.bedwars.engine.Team;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageParser {
    private class ChatStyle {
        public ChatColor color = ChatColor.WHITE;
        public final HashSet<ChatColor> styles = new HashSet<>();

        @Override
        public String toString() {
            return color.toString() + String.join("", styles.stream().map(v -> v.toString()).toList());
        }
    }

    private final Hashtable<String, Object> variables = new Hashtable<>();
    private final String raw;
    private final char colorChar = '&';

    public MessageParser variable(String name, Object value) {
        variables.put(name, value);
        return this;
    }
    public MessageParser variables(Map<String, Object> map) {
        variables.putAll(map);
        return this;
    }

    private ChatStyle getStyle(String str, ChatStyle _default) {
        boolean styling = false;
        ChatStyle style = new ChatStyle();
        style.color = _default.color;
        style.styles.addAll(_default.styles);

        for (int i = 0; i < str.length(); i++) {
            char curr = str.charAt(i);
            if (styling) {
                switch (curr) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                        style.color = ChatColor.getByChar(curr);
                        style.styles.clear();
                        style.styles.addAll(_default.styles);
                        break;
                    case 'k':
                    case 'l':
                    case 'm':
                    case 'n':
                    case 'o':
                        style.styles.add(ChatColor.getByChar(curr));
                        break;
                    case 'r':
                        style.styles.clear();
                        style.styles.addAll(_default.styles);
                        style.color = _default.color;
                        break;
                    default:
                        break;
                }
                styling = false;
            }
            else if (curr == '§') styling = true;
        }

        return style;
    }

    private String replaceVariables() {
        if (raw.length() < 2) return raw;
        String raw = this.raw + ';';
        raw = raw.replace(colorChar, '§');
        raw = raw.replace("§§", Character.toString(colorChar));

        boolean varMode = false;
        StringBuilder varName = new StringBuilder();
        StringBuilder currText = new StringBuilder();
        ChatStyle _default = new ChatStyle();
        _default.color = ChatColor.WHITE;

        for (int i = 1; i < raw.length(); i++) {
            char curr = raw.charAt(i - 1);
            char next = raw.charAt(i);
            boolean appendChar = false;

            if (curr == '{') {
                if (next == '{') {
                    appendChar = true;
                    i++;
                }
                else varMode = true;
            }
            else if (curr == '}') {
                if (next == '}') {
                    appendChar = true;
                    i++;
                }
                else {
                    Object val = variables.get(varName.toString());
                    String strVal = "undefined";

                    if (val != null) {
                        if (val instanceof BaseComponent[]) strVal = BaseComponent.toLegacyText((BaseComponent[])val);
                        else strVal = val.toString();
                    }
                    ChatStyle color = getStyle(currText.toString(), _default);


                    currText.append(strVal.replace(colorChar, '§').replace("§§", Character.toString(colorChar)));
                    currText.append(color);
                    varName.setLength(0);
                    varMode = false;
                }
            }
            else appendChar = true;

            if (appendChar) {
                if (varMode) varName.append(curr);
                else currText.append(curr);
            }
        }

        return currText.toString();
    }

    public BaseComponent[] parse() {
        if (raw == null) return null;
        return TextComponent.fromLegacyText(replaceVariables());
    }

    public void send(CommandSender sender) {
        BaseComponent[] msg = parse();
        if (msg == null) msg = new BaseComponent[] { new TextComponent("Missing message from config.") };
        sender.spigot().sendMessage(msg);
    }
    public void send(Team team) {
        BaseComponent[] msg = parse();
        if (msg == null) msg = new BaseComponent[] { new TextComponent("Missing message from config.") };
        team.sendMessage(msg);
    }
    public void broadcast() {
        BaseComponent[] msg = parse();
        if (msg == null) msg = new BaseComponent[] { new TextComponent("Missing message from config.") };
        Bukkit.spigot().broadcast(msg);
    }

    public MessageParser(String msg) {
        this.raw = msg;
    }
}
