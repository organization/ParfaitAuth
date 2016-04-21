package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.PlayerIdentifier;
import hmhmmhm.ParfaitAuth.Tasks.BanAddressTask;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class BanSubnetAddressCommand extends ParfaitAuthCommand {

	public BanSubnetAddressCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("ban-subnet", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// /s <식별번호|서브넷> <기간> <사유>
		// 해당 서브넷 혹은 유저의 서브넷을 차단합니다.
		// (기간에 숫자만 입력시 분단위가 되며)
		// (앞에 h, d를 붙일 수 있고, 각각 시간,일자단위)
		// (사유는 반드시 적어야하며 띄어쓰기가 가능함)

		if (command.getName().toLowerCase() == this.commandName) {
			if (args[0] == null || args[1] == null || args[2] == null) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
				return true;
			}

			// IP나 UUID중 하나를 확인
			String identifierUUID = null;
			String address = null;

			// 식별번호 확인
			String identifierString = null;
			int identifierInt = 0;

			if (args[0].split("[").length == 2 && args[0].split("[")[1].split("]").length == 1)
				identifierString = args[0].split("[")[1].split("]")[0];

			if (identifierString == null)
				identifierString = args[0];

			// 정수형으로 변환
			try {
				identifierInt = Integer.valueOf(identifierString);
				identifierUUID = PlayerIdentifier.get(identifierInt).toString();
			} catch (NumberFormatException e) {
				if (args[0].split(".").length == 2) {
					address = args[0];
				} else {
					sender.sendMessage(plugin.getMessage("error-cant-find-player-identifier-or-address"));
					return true;
				}
			}

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
					if (args[1].split("h").length == 2) {
						periodString = args[1].split("h")[1];
						periodInt = Integer.valueOf(periodString);
						periodInt *= 60;
					}

					if (args[1].split("d").length == 2) {
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

			BanAddressTask task = new BanAddressTask();

			task.uuid = identifierUUID;
			task.address = address;
			task.periodInt = periodInt;
			task.cause = cause;

			this.getServer().getScheduler().scheduleAsyncTask(task);
			return true;
		}
		return false;
	}

}
