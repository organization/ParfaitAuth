package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class LanguageCommand extends Command {
	public LanguageCommand(ParfaitAuthPlugin plugin) {
		super(plugin);

		this.commandName = "commands-language";
		this.permissionName = "commands-language-permission";
		this.commandDescription = "commands-language-description";
		this.commandUsage = "commands-language-usage";
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
