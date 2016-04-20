package hmhmmhm.ParfaitAuth.Commands;

import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.PlayerIdentifier;
import hmhmmhm.ParfaitAuth.Tasks.BanAccountTask;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class BanAccountCommand extends ParfaitAuthCommand {
	public BanAccountCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("ban-account", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// /b <필터> <검색명> <기간> <사유>
		// 해당 유저의 계정을 차단합니다.
		// (기간에 숫자만 입력시 분단위가 되며)
		// (앞에 h, d를 붙일 수 있고, 각각 시간,일자단위)
		// (사유는 반드시 적어야하며 띄어쓰기가 가능함)

		if (command.getName().toLowerCase() == this.commandName) {
			if (args[0] == null || args[1] == null || args[2] == null) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
				return true;
			}

			// /b 식별번호 <기간> <사유>
			if (args[0] == this.getPlugin().getMessage(this.commandName + "-sub-identy")) {
				// 식별번호 확인
				String identifierString = null;
				int identifierInt;

				// [1]과 같은 형태로 입력되면 숫자만 분리
				if (args[1].split("[")[1] != null && args[1].split("[")[1].split("]")[0] != null)
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

				// 기간 확인
				String periodString = null;
				int periodInt = 0;

				if (periodString == null)
					periodString = args[2];

				// 정수형으로 변환
				try {
					periodInt = Integer.valueOf(periodString);
				} catch (NumberFormatException e) {
					// 시간과 일단위 분단위로 변경
					try {
						if (args[2].split("h")[1] != null) {
							periodString = args[2].split("h")[1];
							periodInt = Integer.valueOf(periodString);
							periodInt *= 60;
						}

						if (args[2].split("d")[1] != null) {
							periodString = args[2].split("d")[1];
							periodInt = Integer.valueOf(periodString);
							periodInt *= 60;
							periodInt *= 24;
						}
					} catch (NumberFormatException e1) {
						sender.sendMessage(plugin.getMessage("error-wrong-period"));
						return true;
					}
				}

				// 사유확인
				String cause = "";

				// 공백으로 나뉜 사유추가
				for (int index = 3; index <= (args.length - 1); index++) {
					cause += args[index];

					// 문자열에 공백추가
					if (index != 3)
						cause += " ";
				}

				// 비동기로 밴처리
				BanAccountTask task = new BanAccountTask();
				task.uuid = playerUUID.toString();
				task.period = periodInt;
				task.cause = cause;
				task.serverUUID = ParfaitAuth.getParfaitAuthUUID().toString();
				server.getScheduler().scheduleAsyncTask(task);
				return true;
			}

			// /b 아이디 <기간> <사유>
			if (args[0] == this.getPlugin().getMessage(this.commandName + "-sub-id")) {

				// 기간 확인
				String periodString = null;
				int periodInt = 0;

				if (periodString == null)
					periodString = args[1];

				// 정수형으로 변환
				try {
					periodInt = Integer.valueOf(periodString);
				} catch (NumberFormatException e) {
					// 시간과 일단위 분단위로 변경
					try {
						if (args[1].split("h")[1] != null) {
							periodString = args[1].split("h")[1];
							periodInt = Integer.valueOf(periodString);
							periodInt *= 60;
						}

						if (args[1].split("d")[1] != null) {
							periodString = args[1].split("d")[1];
							periodInt = Integer.valueOf(periodString);
							periodInt *= 60;
							periodInt *= 24;
						}
					} catch (NumberFormatException e1) {
						sender.sendMessage(plugin.getMessage("error-wrong-period"));
						return true;
					}
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

				// 비동기로 밴처리
				BanAccountTask task = new BanAccountTask();
				task.id = args[1];
				task.period = periodInt;
				task.cause = cause;
				task.serverUUID = ParfaitAuth.getParfaitAuthUUID().toString();
				server.getScheduler().scheduleAsyncTask(task);
				return true;
			}

			/// /b 닉네임 <기간> <사유>
			if (args[0] == this.getPlugin().getMessage(this.commandName + "-sub-nick")) {

				// 기간 확인
				String periodString = null;
				int periodInt = 0;

				if (periodString == null)
					periodString = args[1];

				// 정수형으로 변환
				try {
					periodInt = Integer.valueOf(periodString);
				} catch (NumberFormatException e) {
					// 시간과 일단위 분단위로 변경
					try {
						if (args[1].split("h")[1] != null) {
							periodString = args[1].split("h")[1];
							periodInt = Integer.valueOf(periodString);
							periodInt *= 60;
						}

						if (args[1].split("d")[1] != null) {
							periodString = args[1].split("d")[1];
							periodInt = Integer.valueOf(periodString);
							periodInt *= 60;
							periodInt *= 24;
						}
					} catch (NumberFormatException e1) {
						sender.sendMessage(plugin.getMessage("error-wrong-period"));
						return true;
					}
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

				// 비동기로 밴처리
				BanAccountTask task = new BanAccountTask();
				task.name = args[1];
				task.period = periodInt;
				task.cause = cause;
				task.serverUUID = ParfaitAuth.getParfaitAuthUUID().toString();
				server.getScheduler().scheduleAsyncTask(task);
				return true;
			}
		}
		return false;
	}
}
