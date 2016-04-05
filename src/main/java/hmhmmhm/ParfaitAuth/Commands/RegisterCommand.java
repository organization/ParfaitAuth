package hmhmmhm.ParfaitAuth.Commands;

import java.util.LinkedHashMap;
import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.RequestAccountRegisterTask;
import hmhmmhm.ParfaitAuth.Tasks.RequestAccountRegisterTaskFallback;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class RegisterCommand extends ParfaitAuthCommand {
	private static LinkedHashMap<String, UUID> taskMap = new LinkedHashMap<String, UUID>();

	public RegisterCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("register", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			// 인게임 유저가 아닐경우
			if (!(sender instanceof Player)) {
				sender.sendMessage(this.getMessage("error-in-game-user-only"));
				return true;
			}

			// 입력한 계정명이나 암호가 잘못되거나 없을 경우
			if (args[0] == null || args[1] == null || args.length != 2 || args[0].length() < 4
					|| args[1].length() < 6) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, "commands-register-help-"), 40);
				return true;
			}

			String id = args[0];
			String pw = ParfaitAuth.hash(args[1]);
			UUID uuid = ((Player) sender).getUniqueId();
			UUID taskUUID = UUID.randomUUID();

			sender.sendMessage(this.getMessage("status-start-register-account"));
			this.taskMap.put(sender.getName(), taskUUID);

			// 아래부터 비동기처리화
			this.getServer().getScheduler().scheduleAsyncTask(new RequestAccountRegisterTask(id, pw, uuid, taskUUID));

			// 비동기 응답이 안 올때 5초 후에 서버미응답 표시하게처리
			this.getServer().getScheduler()
					.scheduleDelayedTask(new RequestAccountRegisterTaskFallback(sender.getName(), taskUUID), 100);
			return true;
		}
		return false;
	}
}