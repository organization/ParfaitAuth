package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.PlayerIdentifier;
import hmhmmhm.ParfaitAuth.Tasks.SendAccountInfoTask;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class AccountInfoCommand extends ParfaitAuthCommand {
	public AccountInfoCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account-info", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// /a <필터> <검색어>
		// 밴처리를 위해 해당 유저의 계정정보를 찾습니다.
		// (사용가능필터, 아이디, 닉네임, 식별번호)
		// (아이디 혹은 닉네임을 정확하게 입력해야합니다.)

		if (command.getName().toLowerCase() == this.commandName) {
			if (args[0] == null || args[1] == null) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
				return true;
			}

			// /a 아이디 <검색어>
			if (args[0] == this.getMessage(this.commandName + "-sub-id")) {
				sender.sendMessage(this.getPlugin().getMessage("status-start-the-async-db-request"));
				SendAccountInfoTask task = new SendAccountInfoTask();

				task.id = args[1];
				task.sender = sender.getName();

				this.getServer().getScheduler().scheduleAsyncTask(task);
				return true;
			}

			// /a 닉네임 <검색어>
			if (args[0] == this.getMessage(this.commandName + "-sub-nick")) {
				sender.sendMessage(this.getPlugin().getMessage("status-start-the-async-db-request"));
				SendAccountInfoTask task = new SendAccountInfoTask();

				task.nick = args[1];
				task.sender = sender.getName();

				this.getServer().getScheduler().scheduleAsyncTask(task);
				return true;
			}

			// /a 식별번호 <검색어>
			if (args[0] == this.getMessage(this.commandName + "-sub-identy")) {
				sender.sendMessage(this.getPlugin().getMessage("status-start-the-async-db-request"));
				SendAccountInfoTask task = new SendAccountInfoTask();

				int index = Integer.valueOf(args[1].split("[")[1].split("]")[0]);
				task.identy = PlayerIdentifier.get(index).toString();
				task.sender = sender.getName();

				this.getServer().getScheduler().scheduleAsyncTask(task);
				return true;
			}
		}
		return false;
	}
}
