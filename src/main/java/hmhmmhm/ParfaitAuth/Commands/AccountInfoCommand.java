package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class AccountInfoCommand extends ParfaitAuthCommand {
	public AccountInfoCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account-info", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
		return false;
	}
}
