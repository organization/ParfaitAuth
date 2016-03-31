package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class RegisterCommand extends Command {
	public RegisterCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-register";
		this.permissionName = "commands-register-permission";
		this.commandDescription = "commands-register-description";
		this.commandUsage = "commands-register-usage";
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
