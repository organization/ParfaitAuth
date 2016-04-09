package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class BanReleaseCommand extends ParfaitAuthCommand {
	public BanReleaseCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("ban-release", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
		return false;
	}
}