package me.topchetoeu.bedwars.commandUtility;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Command {
	private String[] aliases;
	private String name;
	private String helpMessage;
	private CommandExecutor fallbackExecutor;
	private HashSet<Command> attachedCommands = new HashSet<>();
	
	private JavaPlugin parent = null;
	
	public String[] getAliases() {
		return aliases;
	}
	
	public String getName() {
		return name;
	}
	
	public String getHelpMessage() {
		return helpMessage;
	}
	public Command setHelpMessage(String val) {
		helpMessage = val;
		return this;
	}
	
	public CommandExecutor getFallbackExecutor() {
		return fallbackExecutor;
	}
	public Command setExecutor(CommandExecutor val) {
		fallbackExecutor = val;
		return this;
	}
	
	public Command attachCommand(Command cmd) {
		if (cmd == null) throw new NullArgumentException("cmd");
		attachedCommands.add(cmd);
		return this;
	}
	public Command detachCommand(Command cmd) {
		if (cmd == null) throw new NullArgumentException("cmd");
		attachedCommands.remove(cmd);
		return this;
	}
	public boolean commandAttached(Command cmd) {
		if (cmd == null) return false;
		return attachedCommands.contains(cmd);
	}
	public Command[] getAttachedCommands() {
		return attachedCommands.toArray(Command[]::new);
	}
	
	public void execute(CommandSender sender, String alias, String[] args) {
		Command cmd;
		if (args.length == 0) cmd = null;
		else cmd = getAttachedCommand(args[0]);
		
		String[] newArgs;
		if (args.length <= 1) newArgs = new String[0];
		else {
			newArgs = new String[args.length - 1];
			System.arraycopy(args, 1, newArgs, 0, args.length - 1);
		}
		
		if (cmd != null)
			cmd.execute(sender, args[0], newArgs);
		else if (fallbackExecutor != null) fallbackExecutor.execute(
			sender, this,
			alias, args
		);
		else sender.sendMessage("This command doesn't do anything :(");
	}
	
	public Command register(JavaPlugin pl) {
		if (pl == parent) throw new IllegalArgumentException("The command is already attached to the given plugin");
		if (pl == null) throw new NullArgumentException("pl");
		parent = pl;
		pl.getCommand(name).setAliases(Arrays.asList(aliases));
		pl.getCommand(name).setExecutor(new org.bukkit.command.CommandExecutor() {
			
			@Override
			public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String alias, String[] args) {
				execute(sender, alias, args);
				return true;
			}
		});
	
		return this;
	}
	
	public Command getAttachedCommand(String alias) {
		String newAlias = alias.toLowerCase();
		for (Command command : attachedCommands) {
			if (command.name.equals(newAlias) || Arrays.stream(command.aliases).anyMatch(v -> v.equals(newAlias)))
				return command;
		}
		return null;
	}

	public Command(String name, String alias) {
		this.name = name;
		this.aliases = new String[] { alias };
	}
	public Command(String name, String... aliases) {
		this.name = name;
		this.aliases = aliases;
	}
	public Command(String name, CommandExecutor executor, String... aliases) {
		this.name = name;
		this.aliases = aliases;
		this.fallbackExecutor = executor;
	}
	public Command(String name, String alias, Command... commands) {
		this.name = name;
		this.aliases = new String[] { alias };
		
		for (Command cmd : commands) {
			attachCommand(cmd);
		}
	}
	public Command(String name, String[] aliases, Command... commands) {
		this.name = name;
		this.aliases = aliases;
		
		for (Command cmd : commands) {
			attachCommand(cmd);
		}
	}
	public Command(String name, String[] aliases, CommandExecutor executor, Command... commands) {
		this.name = name;
		this.aliases = aliases;
		fallbackExecutor = executor;
		
		for (Command cmd : commands) {
			attachCommand(cmd);
		}
	}
	
}
