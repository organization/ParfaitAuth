package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

abstract public class Command {
	ParfaitAuthPlugin plugin;
	Server server;

	protected String commandName;
	protected String permissionName;
	protected String commandDescription;
	protected String commandUsage;

	public Command(ParfaitAuthPlugin plugin) {
		this.plugin = plugin;
		this.server = plugin.getServer();
	}

	protected boolean registerCommand() {
		SimpleCommandMap commandMap = this.getServer().getCommandMap();

		PluginCommand<Plugin> command = new PluginCommand<>(this.getMessage(this.commandName), this.plugin);
		command.setDescription(this.getMessage(this.commandDescription));
		command.setPermission(this.getMessage(this.permissionName));
		command.setUsage(this.getMessage(this.commandUsage));
		return commandMap.register(commandName, command);
	}

	public String getMessage(String key) {
		return this.plugin.getMessage(key);
	}

	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		return false;
	}

	public Server getServer() {
		return this.server;
	}

	public ParfaitAuthPlugin getPlugin() {
		return this.plugin;
	}
}