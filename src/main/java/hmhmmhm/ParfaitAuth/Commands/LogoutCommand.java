package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class LogoutCommand extends ParfaitAuthCommand {
	public LogoutCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("logout", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(this.getMessage("error-in-game-user-only"));
				return true;
			}

			if (ParfaitAuth.authorisedID.get(((Player) sender).getUniqueId()) == null) {
				sender.sendMessage(this.getMessage("error-please-login-first"));
				return true;
			}

			Account accountData = ParfaitAuth.authorisedID.get(((Player) sender).getUniqueId());
			((Player) sender).kick(this.getMessage("kick-successfully-logout"), false);
			ParfaitAuth.release(((Player) sender).getUniqueId(), accountData, true, true);
			return true;
		}
		return false;
	}
}
