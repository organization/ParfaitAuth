package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.AccountStatisticsTask;
import hmhmmhm.ParfaitAuth.Tasks.ChangeAccountTypeTask;
import hmhmmhm.ParfaitAuth.Tasks.ForceAccessAccountTask;
import hmhmmhm.ParfaitAuth.Tasks.RemoveAccountDataTask;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class AccountCommand extends ParfaitAuthCommand {
	public AccountCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			// 유저가 관리자 계정인지 확인합니다.
			if (!sender.isOp()) {
				Account account = ParfaitAuth.authorisedID.get(((Player) sender).getUniqueId());
				if (account == null || account.accountType != Account.TYPE_ADMIN) {
					sender.sendMessage(this.getPlugin().getMessage("error-permission-not-authorized"));
					return true;
				}
			}

			if (args[0] == null) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
				return true;
			}

			// "commands-account-sub-type": "유형"
			if (args[0] == this.getMessage(this.commandName + "-sub-type")) {
				// 계정 유형 아이디 1 의 형식이 아닐경우
				if (args[1] == null || args[2] == null) {
					this.getServer().getScheduler()
							.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "type-help-"), 5);
					return true;
				}

				int accounttype = Integer.getInteger(args[2]);

				// 계정 유형 맞는지 확인
				switch (accounttype) {
				case Account.TYPE_DEFAULT:
				case Account.TYPE_GUEST:
				case Account.TYPE_ADMIN:
				case Account.TYPE_BUILDER:
				case Account.TYPE_NORMAL:
				case Account.TYPE_OVER_POWER:
					break;
				default:
					sender.sendMessage(this.getMessage("error-cant-find-that-accounttype"));
					return true;
				}

				// 인게임 유저가 아닐경우
				if (!(sender instanceof Player)) {
					sender.sendMessage(this.getMessage("error-in-game-user-only"));
					return true;
				}

				sender.sendMessage(this.getMessage("status-start-change-account-type-async"));

				// 비동기로 명령수행
				this.getServer().getScheduler().scheduleAsyncTask(new ChangeAccountTypeTask(sender.getName(), args[1],
						ParfaitAuth.getParfaitAuthUUID().toString(), accounttype, false));
				return true;
			}

			// "commands-account-sub-login": "접속"
			if (args[0] == this.getMessage(this.commandName + "-sub-login")) {
				// 계정 접속 아이디

				if (args[1] == null) {
					this.getServer().getScheduler()
							.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "type-help-"), 5);
					return true;
				}

				// ForceAccessAccountTask
				this.getServer().getScheduler().scheduleAsyncTask(new ForceAccessAccountTask(sender.getName(), args[1],
						ParfaitAuth.getParfaitAuthUUID().toString()));
				return true;
			}

			// "commands-account-sub-del": "삭제"
			if (args[0] == this.getMessage(this.commandName + "-sub-del")) {
				// 계정 삭제 아이디

				if (args[1] == null) {
					this.getServer().getScheduler()
							.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "type-help-"), 5);
					return true;
				}

				// RemoveAccountDataTask
				this.getServer().getScheduler().scheduleAsyncTask(new RemoveAccountDataTask(sender.getName(), args[1],
						ParfaitAuth.getParfaitAuthUUID().toString()));
				return true;
			}

			// "commands-account-sub-stat": "통계"
			if (args[0] == this.getMessage(this.commandName + "-sub-stat")) {
				this.getServer().getScheduler().scheduleAsyncTask(new AccountStatisticsTask(sender.getName()));
				return true;
			}
		}
		return false;
	}
}
