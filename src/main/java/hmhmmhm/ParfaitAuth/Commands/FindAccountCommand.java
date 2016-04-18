package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class FindAccountCommand extends ParfaitAuthCommand {
	public FindAccountCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("findaccount", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			sender.sendMessage(this.getMessage("status-that-function-currently-not-support"));
			return true;
		}
		return false;
	}
}