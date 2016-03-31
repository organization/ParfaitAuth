package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class FindAccountCommand extends Command {
	public FindAccountCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-findaccount";
		this.permissionName = "commands-findaccount-permission";
		this.commandDescription = "commands-findaccount-description";
		this.commandUsage = "commands-findaccount-usage";
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
