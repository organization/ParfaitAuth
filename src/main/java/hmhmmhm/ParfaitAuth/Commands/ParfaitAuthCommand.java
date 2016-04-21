package hmhmmhm.ParfaitAuth.Commands;

import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

abstract public class ParfaitAuthCommand extends Command {
	public ParfaitAuthCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
	}

	protected void load(String commandPrefix, boolean isOp) {
		this.commandKey = "commands-" + commandPrefix;
		this.commandName = this.getMessage(this.commandKey);
		this.permissionName = this.getMessage(this.commandKey + "-permission");
		this.commandDescription = this.getMessage(this.commandKey + "-description");
		this.commandUsage = this.getMessage(this.commandKey + "-usage");

		this.registerCommand();
		this.registerPermission(this.permissionName, isOp, this.commandDescription);
	}
}
