package hmhmmhm.ParfaitAuth.Commands;

import java.util.UUID;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.TaskHandler;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.PlayerIdentifier;
import hmhmmhm.ParfaitAuth.Tasks.BanReleaseTask;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class BanReleaseCommand extends ParfaitAuthCommand {
	public BanReleaseCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("ban-release", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// /r <필터> <검색어> <사유>
		// 해당 유저의 계정정보를 찾고 차단을 해제합니다.
		// (사용가능필터, 아이디, 닉네임, 식별번호, 아이피, 서브넷)
		// (검색어를 정확하게 입력해야합니다.)

		if (command.getName().toLowerCase() == this.commandName) {
			if (args.length < 3 || (!args[0].equals(this.getMessage(this.commandKey + "-sub-id"))
					&& !args[0].equals(this.getMessage(this.commandKey + "-sub-nick"))
					&& !args[0].equals(this.getMessage(this.commandKey + "-sub-identy"))
					&& !args[0].equals(this.getMessage(this.commandKey + "-sub-ip"))
					&& !args[0].equals(this.getMessage(this.commandKey + "-sub-subnet")))) {
				SendMessageTask task = new SendMessageTask(sender, this.commandKey + "-help-");
				TaskHandler handler = this.getServer().getScheduler().scheduleRepeatingTask(task, 10);
				task.setHandler(handler);
				return true;
			}
			BanReleaseTask task = new BanReleaseTask();

			// /r 아이디 검색어 사유
			if (args[0].equals(this.getMessage(this.commandKey + "-sub-id")))
				task.id = args[1];

			// /r 닉네임 검색어 사유
			if (args[0].equals(this.getMessage(this.commandKey + "-sub-nick")))
				task.nick = args[1];

			// /r 아이피 검색어 사유
			if (args[0].equals(this.getMessage(this.commandKey + "-sub-ip")))
				task.ip = args[1];

			// /r 서브넷 검색어 사유
			if (args[0].equals(this.getMessage(this.commandKey + "-sub-subnet")))
				task.subnet = args[1];

			// /r 식별번호 검색어 사유
			if (args[0].equals(this.getMessage(this.commandKey + "-sub-identy"))) {
				// 식별번호 확인
				String identifierString = null;
				int identifierInt;

				// [1]과 같은 형태로 입력되면 숫자만 분리
				if (args[1].split("[").length == 2 && args[1].split("[")[1].split("]").length == 1)
					identifierString = args[1].split("[")[1].split("]")[0];

				if (identifierString == null)
					identifierString = args[1];

				// 정수형으로 변환
				try {
					identifierInt = Integer.valueOf(identifierString);
				} catch (NumberFormatException e) {
					sender.sendMessage(plugin.getMessage("error-cant-find-player-identifier"));
					return true;
				}

				// 식별번호에서 플레이어 얻기
				UUID playerUUID = PlayerIdentifier.get(identifierInt);
				if (playerUUID == null) {
					sender.sendMessage(plugin.getMessage("error-cant-find-player-identifier"));
					return true;
				}

				task.uuid = playerUUID.toString();
			}

			// 사유확인
			String cause = "";

			// shift 0 1 진행후 2부터 끝까지 합치기
			for (int index = 2; index <= (args.length - 1); index++) {
				cause += args[index];

				// 문자열에 공백추가
				if (index != 2)
					cause += " ";
			}

			task.cause = cause;
			task.sender = sender.getName();
			this.getServer().getScheduler().scheduleAsyncTask(task);
			return true;
		}
		return false;
	}
}