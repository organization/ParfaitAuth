package hmhmmhm.ParfaitAuth.Commands;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.TaskHandler;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.ChangeNameTask;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class ChangeNickCommand extends ParfaitAuthCommand {
	public static LinkedHashMap<UUID, Long> userUUIDMap = new LinkedHashMap<UUID, Long>();

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

			// 처음입력하는 경우 주의문 띄우기
			if (userUUIDMap.get(((Player) sender).getUniqueId()) == null) {
				Long currentTimestamp = Calendar.getInstance().getTime().getTime();

				userUUIDMap.put(((Player) sender).getUniqueId(), currentTimestamp);
				sender.sendMessage(plugin.getMessage("caution-are-you-really-change-name"));
				sender.sendMessage(plugin.getMessage("caution-check-your-name-ownership"));
				sender.sendMessage(plugin.getMessage("caution-if-you-want-change-name-hit-again"));
				return true;
			}

			// 두번째로 입력하는 경우
			if (userUUIDMap.get(((Player) sender).getUniqueId()) != null) {
				Long pastTimestamp = userUUIDMap.get(((Player) sender).getUniqueId());
				Long currentTimestamp = Calendar.getInstance().getTime().getTime();
				Long diff = TimeUnit.MILLISECONDS.toSeconds(currentTimestamp - pastTimestamp);

				// 이전보다 30초를 넘어서 입력되었다면 다시 주의문 띄우기
				if (diff >= 30) {
					userUUIDMap.put(((Player) sender).getUniqueId(), currentTimestamp);
					sender.sendMessage(plugin.getMessage("caution-are-you-really-change-name"));
					sender.sendMessage(plugin.getMessage("caution-check-your-name-ownership"));
					sender.sendMessage(plugin.getMessage("caution-if-you-want-change-name-hit-again"));
					return true;
				}
			}

			this.getServer().getScheduler()
					.scheduleAsyncTask(new ChangeNameTask(sender.getName(), player.getUniqueId().toString(), args[0]));
			return true;
		}
		return false;
	}
}
