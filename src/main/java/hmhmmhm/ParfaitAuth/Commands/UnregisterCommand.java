package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class UnregisterCommand extends ParfaitAuthCommand {
	public UnregisterCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("unregister", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(this.getMessage("error-in-game-user-only"));
				return true;
			}

			if (ParfaitAuth.authorisedID.get((Player) sender) == null) {
				sender.sendMessage(this.getMessage("error-please-login-first"));
				return true;
			}

			ParfaitAuth.deleteAccountAsync(((Player) sender).getUniqueId());
			((Player) sender).kick(ParfaitAuthPlugin.getPlugin().getMessage("kick-successfully-unregistered"));
			return true;
		}
		return false;
	}
}
