package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class LogoutCommand extends Command{
	public LogoutCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-logout";
		this.permissionName = "commands-logout-permission";
		this.commandDescription = "commands-logout-description";
		this.commandUsage = "commands-logout-usage";
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
