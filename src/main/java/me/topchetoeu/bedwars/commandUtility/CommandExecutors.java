package me.topchetoeu.bedwars.commandUtility;

import org.bukkit.command.CommandSender;

public class CommandExecutors {
	private static String join(String[] arr, String separator) {
		if (arr.length == 0) return "";
		if (arr.length == 1) return arr[0];
		
		String res = arr[0];
		
		for (int i = 1; i < arr.length; i++) {
			res += separator + arr[i];
		}
		
		return res;
	}
	
	public static CommandExecutor help(Command mainCmd) {
		return new CommandExecutor() {
			@Override
			public void execute(CommandSender sender, Command cmd, String alias, String[] args) {
				Command currCmd = mainCmd;
				String path = "/" + mainCmd.getName();
				
				for (String arg : args) {
					currCmd = currCmd.getAttachedCommand(arg);
					if (currCmd == null) {
						String msg = "Help can't be provided for the command.";
						sender.sendMessage(msg);
						return;
					}
					
					path += " " + currCmd.getName();
				}
				
				sender.sendMessage(path + ": " + (currCmd.getHelpMessage() == null ?
												 "no help provided" :
												 currCmd.getHelpMessage())
				);
				if (currCmd.getAliases().length != 0)
					sender.sendMessage("Aliases: " + join(currCmd.getAliases(), ", "));
				
				if (currCmd.getAttachedCommands().length > 0) {
					sender.sendMessage("Commands: ");
					for (Command subCmd : currCmd.getAttachedCommands()) {
						sender.sendMessage(path + " " + subCmd.getName() + ": " + subCmd.getHelpMessage());
					}
				}
			}
		};
	}
	public static CommandExecutor message(String msg) {
		return new CommandExecutor() {
			
			@Override
			public void execute(CommandSender sender, Command cmd, String alias, String[] args) {
				sender.sendMessage(msg);
			}
		};
	}
}
