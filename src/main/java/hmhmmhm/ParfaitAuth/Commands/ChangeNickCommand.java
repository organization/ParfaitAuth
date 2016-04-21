package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.TaskHandler;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.ChangeNameTask;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class ChangeNickCommand extends ParfaitAuthCommand {
	public ChangeNickCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("changenick", false);
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

			// 인게임유저만 가능
			if (!(sender instanceof Player)) {
				sender.sendMessage(plugin.getMessage("error-in-game-user-only"));
				return true;
			}

			Player player = (Player) sender;

			// ID계정으로 인증된 유저만가능
			if (ParfaitAuth.authorisedID.get(player.getUniqueId()) == null) {
				sender.sendMessage(plugin.getMessage("error-please-login-first"));
				return true;
			}

			// 잘못된 닉네임 입력됨
			if (!ParfaitAuth.checkRightName(args[0])) {
				sender.sendMessage(plugin.getMessage("error-cant-use-that-name-wrong-char-founded"));
				return true;
			}

			this.getServer().getScheduler()
					.scheduleAsyncTask(new ChangeNameTask(sender.getName(), player.getUniqueId().toString(), args[0]));
			return true;
		}
		return false;
	}
}
