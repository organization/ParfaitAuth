package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class ChangePasswordCommand extends ParfaitAuthCommand {
	public ChangePasswordCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("changepassword", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			// TODO
			if (args[0] == null || args[1] == null) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
				return true;
			}

			if (!(sender instanceof Player)) {
				sender.sendMessage(this.getMessage("error-in-game-user-only"));
				return true;
			}

			
			return true;
		}
		return false;
	}
}
