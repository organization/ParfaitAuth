package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class BanReleaseCommand extends ParfaitAuthCommand {
	public BanReleaseCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("ban-release", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
		// /r <필터> <검색어> <사유>
		// 해당 유저의 계정정보를 찾고 차단을 해제합니다.
		// (사용가능필터, 아이디, 닉네임, 식별번호, 아이피, 서브넷)
		// (검색어를 정확하게 입력해야합니다.)
		if (command.getName().toLowerCase() == this.commandName) {
			if (args[0] == null) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-id")) {
				// TODO 아이디
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-nick")) {
				// TODO 닉네임
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-identy")) {
				// TODO 식별번호
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-ip")) {
				// TODO 아이피
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-subnet")) {
				// TODO 서브넷
				return true;
			}
		}
		return false;
	}
}