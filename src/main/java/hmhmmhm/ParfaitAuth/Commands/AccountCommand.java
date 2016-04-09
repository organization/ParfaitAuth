package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class AccountCommand extends ParfaitAuthCommand {
	public AccountCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO
		// /계정 유형 /account type
		// /계정 접속 /account login
		// /계정 삭제 /account del
		// /계정 통계 /account stat
		return false;
	}
}
