package hmhmmhm.ParfaitAuth.Commands;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.permission.Permission;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

abstract public class Command {
	ParfaitAuthPlugin plugin;
	Server server;

	protected String commandKey;
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
		PluginCommand<Plugin> command = new PluginCommand<>(this.commandName, this.plugin);
		command.setDescription(this.commandDescription);
		command.setPermission(this.permissionName);
		command.setUsage(this.commandUsage);
		return commandMap.register(commandName, command);
	}

	protected boolean registerPermission(String permissionName, boolean isOp, String description) {
		LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("description", description);

		String DEFAULT = (isOp) ? Permission.DEFAULT_OP : Permission.DEFAULT_TRUE;
		Permission permission = Permission.loadPermission(permissionName, data, DEFAULT);
		return this.getServer().getPluginManager().addPermission(permission);
	}

	public String getMessage(String key) {
		return this.plugin.getMessage(key);
	}

	public abstract boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args);

	public Server getServer() {
		return this.server;
	}

	public ParfaitAuthPlugin getPlugin() {
		return this.plugin;
	}
}