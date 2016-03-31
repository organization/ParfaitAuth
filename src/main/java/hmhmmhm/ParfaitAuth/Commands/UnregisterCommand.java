package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class UnregisterCommand extends Command {
	public UnregisterCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-unregister";
		this.permissionName = "commands-unregister-permission";
		this.commandDescription = "commands-unregister-description";
		this.commandUsage = "commands-unregister-usage";
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
