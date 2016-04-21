package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.TaskHandler;
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
			SendMessageTask task = new SendMessageTask(sender, this.commandKey + "-help-");
			TaskHandler handler = this.getServer().getScheduler().scheduleRepeatingTask(task, 10);
			task.setHandler(handler);
			return true;
		}
		return false;
	}

}