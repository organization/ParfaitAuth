package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class ChangePasswordCommand extends Command {
	public ChangePasswordCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-changepassword";
		this.permissionName = "commands-changepassword-permission";
		this.commandDescription = "commands-changepassword-description";
		this.commandUsage = "commands-changepassword-usage";
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
