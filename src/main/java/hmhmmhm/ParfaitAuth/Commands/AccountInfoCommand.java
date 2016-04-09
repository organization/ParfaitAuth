package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class AccountInfoCommand extends ParfaitAuthCommand {
	public AccountInfoCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account-info", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
		// /a <필터> <검색어>
		// 밴처리를 위해 해당 유저의 계정정보를 찾습니다.
		// (사용가능필터, 아이디, 닉네임, 식별번호)
		// (아이디 혹은 닉네임을 정확하게 입력해야합니다.)
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
		}
		return false;
	}
}
