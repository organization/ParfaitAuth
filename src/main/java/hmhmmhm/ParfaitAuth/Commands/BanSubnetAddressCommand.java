package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class BanSubnetAddressCommand extends ParfaitAuthCommand {

	public BanSubnetAddressCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("ban-subnet", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
		return false;
	}

}
