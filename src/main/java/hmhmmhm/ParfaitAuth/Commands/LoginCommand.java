package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.TaskHandler;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class LoginCommand extends ParfaitAuthCommand {
	public LoginCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("login", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			if (args.length < 2) {
				SendMessageTask task = new SendMessageTask(sender, this.commandKey + "-help-");
				TaskHandler handler = this.getServer().getScheduler().scheduleRepeatingTask(task, 10);
				task.setHandler(handler);
				return true;
			}

			if (!(sender instanceof Player)) {
				sender.sendMessage(this.getMessage("error-in-game-user-only"));
				return true;
			}

			if (ParfaitAuth.authorisedID.get((Player) sender) != null) {
				sender.sendMessage(this.getMessage(""));
				return true;
			}

			ParfaitAuth.preAuthorizationID((Player) sender, args[0], args[1], false);
			return true;
		}
		return false;
	}
}