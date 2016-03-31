package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class LoginCommand extends Command {
	public LoginCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-login";
		this.permissionName = "commands-login-permission";
		this.commandDescription = "commands-login-description";
		this.commandUsage = "commands-login-usage";
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