package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class SendAccountInfoTask extends AsyncTask {
	public String id = null;
	public String nick = null;
	public String identy = null;
	public String sender = null;

	// 조회된 계정정보가 여기 담깁니다.
	private Account account = null;

	@Override
	public void onRun() {
		if (this.sender == null)
			return;

		if (this.id != null)
			this.account = ParfaitAuth.getAccountById(this.id);

		if (this.nick != null)
			this.account = ParfaitAuth.getAccountByNickName(this.nick);

		if (this.identy != null)
			this.account = ParfaitAuth.getAccount(UUID.fromString(this.identy));
	}

	public void onCompletion(Server server) {
		Player player = server.getPlayer(sender);
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		if (player == null || this.account == null)
			return;

		player.sendMessage(plugin.getMessage("commands-account-info-show-start"));
		player.sendMessage(plugin.getMessage("commands-account-info-idnick").replace("%id", this.account.id)
				.replace("%nick", this.account.nickname));
		player.sendMessage(
				plugin.getMessage("commands-account-info-lastdate").replace("%lastdate", account.getLastDate()));
		player.sendMessage(plugin.getMessage("commands-account-info-lastip").replace("%lastip", account.lastIp));
		player.sendMessage(
				plugin.getMessage("commands-account-info-account-type").replace("%type", account.getAccountType()));
		player.sendMessage(plugin.getMessage("commands-account-info-logined").replace("%logined", account.logined));
		player.sendMessage(plugin.getMessage("commands-account-info-unblock-period").replace("%unblockperiod",
				account.getUnblockPeriod()));
	}
}
