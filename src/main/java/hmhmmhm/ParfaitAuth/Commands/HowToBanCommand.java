package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class HowToBanCommand extends ParfaitAuthCommand {

	public HowToBanCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("howtoban", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return false;
	}

}
