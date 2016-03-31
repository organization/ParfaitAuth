package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class ChangeNickCommand extends Command{
	public ChangeNickCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-changenick";
		this.permissionName = "commands-changenick-permission";
		this.commandDescription = "commands-changenick-description";
		this.commandUsage = "commands-changenick-usage";
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
