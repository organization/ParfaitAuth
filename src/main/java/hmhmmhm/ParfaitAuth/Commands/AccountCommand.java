package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class AccountCommand extends ParfaitAuthCommand {
	public AccountCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			if (args[0] == null) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-type")) {
				// TODO "commands-account-sub-type": "유형"
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-login")) {
				// TODO "commands-account-sub-login": "접속"
				return true;
			}
			if (args[0] == this.getMessage(this.commandName + "-sub-del")) {
				// TODO "commands-account-sub-del": "삭제"
				return true;
			}
			if (args[0] == this.getMessage(this.commandName + "-sub-stat")) {
				// TODO "commands-account-sub-stat": "통계"
				return true;
			}
		}
		return false;
	}
}
