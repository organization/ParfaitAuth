package hmhmmhm.ParfaitAuth.Commands;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.RequestAccountRegisterTask;
import hmhmmhm.ParfaitAuth.Tasks.RequestAccountRegisterTaskFallback;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class RegisterCommand extends ParfaitAuthCommand {
	public static LinkedHashMap<String, UUID> taskMap = new LinkedHashMap<String, UUID>();

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

			// 비동기 처리위한 자료 별도 수집
			String id = args[0];
			String pw = ParfaitAuth.hash(args[1]);
			String userIp = ((Player) sender).getAddress();
			String nickname = ((Player) sender).getName();
			String timestamp = String.valueOf(Calendar.getInstance().getTime().getTime());
			UUID serverUUID = UUID.fromString((String) ParfaitAuthPlugin.getPlugin().getSettings().get("server-uuid"));
			UUID uuid = ((Player) sender).getUniqueId();
			UUID taskUUID = UUID.randomUUID();

			// 유저에게 비동기 인증 시작 메시지 전송
			sender.sendMessage(this.getMessage("status-start-register-account"));
			this.taskMap.put(sender.getName(), taskUUID);

			// 아래부터 비동기처리화
			this.getServer().getScheduler().scheduleAsyncTask(
					new RequestAccountRegisterTask(id, pw, uuid, taskUUID, timestamp, userIp, serverUUID, nickname));

			// 비동기 응답이 안 올때 6초 후에 서버미응답 표시하게처리
			this.getServer().getScheduler()
					.scheduleDelayedTask(new RequestAccountRegisterTaskFallback(sender.getName(), taskUUID), 120);
			return true;
		}
		return false;
	}

	public static void registerCallback(String username, UUID taskUUID, Account account) {
		RegisterCommand.taskMap.remove(username);
		Player player = Server.getInstance().getPlayer(username);

		if (!(player instanceof Player))
			return;

		player.sendMessage(ParfaitAuthPlugin.getPlugin().getMessage("success-id-account-was-created"));
		ParfaitAuth.authorizationID(player, account, true, true);
	}

	public static void registerFailCallback(String username, int result) {
		RegisterCommand.taskMap.remove(username);
		Player player = Server.getInstance().getPlayer(username);

		if (!(player instanceof Player))
			return;

		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		switch (result) {
		case RequestAccountRegisterTask.ID_ACCOUNT_ALREADY_EXIST:
			player.sendMessage(plugin.getMessage("error-this-account-id-already-exist"));
			break;
		case RequestAccountRegisterTask.UUID_ACCOUNT_NOT_EXIST:
			player.sendMessage(plugin.getMessage("error-uuid-account-not-exist"));
			plugin.getLogger().critical(plugin.getMessage("error-uuid-account-not-exist-user-founded"));
			break;
		case RequestAccountRegisterTask.CLIENT_IS_DEAD:
			player.sendMessage(plugin.getMessage("error-db-connect-unstable"));
			break;
		case RequestAccountRegisterTask.USER_DATA_NOT_EXIST:
			player.sendMessage(plugin.getMessage("error-db-cant-find-your-account"));
			break;
		}
	}
}