package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class AccountFindCommand extends ParfaitAuthCommand{

	public AccountFindCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account-find", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
		return false;
	}

}
