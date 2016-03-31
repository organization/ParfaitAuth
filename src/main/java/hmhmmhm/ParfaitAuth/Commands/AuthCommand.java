package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class AuthCommand extends Command {
	public AuthCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-auth";
		this.permissionName = "commands-auth-permission";
		this.commandDescription = "commands-auth-description";
		this.commandUsage = "commands-auth-usage";
		this.registerCommand();
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			// TODO
			return true;
		}
		return false;
	}
}