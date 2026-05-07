package me.topchetoeu.bedwars.commandUtility.args;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;

public class LocationArgParser implements ArgParser {

    @Override
    public ArgParserRes parse(CommandSender sender, List<String> remainingArgs) {
        if (remainingArgs.size() < 3) return ArgParserRes.fail();
        else {
            Location loc = null;
            if (sender instanceof LivingEntity) loc = ((LivingEntity)sender).getLocation();
            else if (sender instanceof BlockCommandSender) loc = ((BlockCommandSender)sender).getBlock().getLocation();
            else return ArgParserRes.error("Coordinates may not be specified by a non-located command sender.");

            String rawX = remainingArgs.get(0),
                   rawY = remainingArgs.get(1),
                   rawZ = remainingArgs.get(2);
            
            boolean relX = false, relY = false, relZ = false;

            if (rawX.startsWith("~")) {
                relX = true;
                rawX = rawX.substring(1);
                if (rawX.isEmpty()) rawX = "0";
            }
            if (rawY.startsWith("~")) {
                relY = true;
                rawY = rawY.substring(1);
                if (rawY.isEmpty()) rawY = "0";
            }
            if (rawZ.startsWith("~")) {
                relZ = true;
                rawZ = rawZ.substring(1);
                if (rawZ.isEmpty()) rawZ = "0";
            }

            double x, y, z;

            try {
                x = Double.parseDouble(rawX);
                y = Double.parseDouble(rawY);
                z = Double.parseDouble(rawZ);
            } catch(NumberFormatException e) {
                return ArgParserRes.error("Invalid number format.");
            }

            if (relX) x += loc.getX();
            if (relY) y += loc.getY();
            if (relZ) z += loc.getZ();

            return ArgParserRes.takenMany(3, new Location(loc.getWorld(), x, y, z));
        }
    }

    private void addSuggestions(String arg, Double curr, Suggestions suggestions) {
        if (arg.isEmpty()) {
            suggestions.addSuggestions("~", curr.toString(), Double.toString(Math.floor(curr)), Double.toString(Math.floor(curr) + 0.5));
        }
        else if (arg.startsWith("~")) {
            arg = arg.substring(1);

            suggestions.addSuggestions("~" + arg, "~" + ".5");
        }
        else suggestions.addSuggestion(arg);

        if (arg.length() > 0) {
            try {
                Double.parseDouble(arg);
            }
            catch (NumberFormatException e) {
                suggestions.error("Number is in an invalid format.");
            }
        }
    }

    @Override
    public void addCompleteSuggestions(CommandSender sender, List<String> args, Suggestions suggestions) {
        Location loc = null;
        if (sender instanceof LivingEntity) loc = ((LivingEntity)sender).getLocation();
        else if (sender instanceof BlockCommandSender) loc = ((BlockCommandSender)sender).getBlock().getLocation();
        else {
            suggestions.error("Only located command senders may use locations.");
            return;
        }

        double curr = loc.getZ();
        if (args.size() < 3) curr = loc.getY();
        if (args.size() < 2) curr = loc.getX();

        String arg = args.get(args.size() - 1);
        addSuggestions(arg, curr, suggestions);
    }
    
    public LocationArgParser() {
        
    }
}
