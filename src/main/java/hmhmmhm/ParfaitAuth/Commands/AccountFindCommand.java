package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class AccountFindCommand extends ParfaitAuthCommand {

	public AccountFindCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account-find", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
		// f <필터> <검색어>
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

			if (args[0] == this.getMessage(this.commandName + "-sub-chat")) {
				// TODO 채팅
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-logout")) {
				// TODO 나감
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-login")) {
				// TODO 접속
				return true;
			}

			if (args[0] == this.getMessage(this.commandName + "-sub-diff")) {
				// TODO 복잡닉
				return true;
			}
		}
		return false;
	}
}
