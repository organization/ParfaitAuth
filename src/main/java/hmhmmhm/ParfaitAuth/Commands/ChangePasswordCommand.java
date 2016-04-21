package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.TaskHandler;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
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
			if (args.length == 0) {
				SendMessageTask task = new SendMessageTask(sender, this.commandKey + "-help-");
				TaskHandler handler = this.getServer().getScheduler().scheduleRepeatingTask(task, 10);
				task.setHandler(handler);
				return true;
			}

			if (!(sender instanceof Player)) {
				sender.sendMessage(this.getMessage("error-in-game-user-only"));
				return true;
			}

			// ID계정정보 가져오기
			Account account = ParfaitAuth.authorisedID.get(((Player) sender).getUniqueId());

			// 현재 ID계정으로 접속중이 아니라면 반환합니다.
			if (account == null) {
				sender.sendMessage(plugin.getMessage("error-please-login-first"));
				return true;
			}

			if (!ParfaitAuth.checkRightPassword(args[0])) {
				sender.sendMessage(plugin.getMessage("error-password-length-must-be-6~40"));
				return true;
			}

			account.password = ParfaitAuth.hash(args[0]);
			account.setModified();

			sender.sendMessage(plugin.getMessage("success-password-was-changed"));
			return true;
		}
		return false;
	}
}
