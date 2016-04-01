package hmhmmhm.ParfaitAuth.Commands;

import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

abstract public class ParfaitAuthCommand extends Command {
	public ParfaitAuthCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
	}

	protected void load(String commandPrefix, boolean isOp) {
		this.commandName = this.getMessage("commands-" + commandPrefix);
		this.permissionName = this.getMessage("commands-" + commandPrefix + "-permission");
		this.commandDescription = this.getMessage("commands-" + commandPrefix + "-description");
		this.commandUsage = this.getMessage("commands-" + commandPrefix + "-usage");

		this.registerCommand();
		this.registerPermission(this.permissionName, isOp, this.commandDescription);
	}
}
