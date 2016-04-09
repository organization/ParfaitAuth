package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class HowToBanCommand extends ParfaitAuthCommand {

	public HowToBanCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("howtoban", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			this.getServer().getScheduler()
					.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
			return true;
		}
		return false;
	}

}
