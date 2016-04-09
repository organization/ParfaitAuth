package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class BanSubnetAddressCommand extends ParfaitAuthCommand {

	public BanSubnetAddressCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("ban-subnet", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
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

			// TODO 식별번호 아이디 구분, 식별번호는 양 좌우로 [ ]가 붙어야함
			// TODO d h 구분 후 타임스탬프화
		}
		return false;
	}

}
