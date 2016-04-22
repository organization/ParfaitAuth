package hmhmmhm.ParfaitAuth.Tasks;

import java.util.ArrayList;
import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.Notification;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class BanAccountTask extends AsyncTask {
	public String uuid = null;
	public String id = null;
	public String name = null;

	public int period;
	public String cause = null;
	public String sender = null;
	public String serverUUID = null;
	public boolean checkServerPingAlive = false;

	public Account account = null;

	@Override
	public void onRun() {
		if (this.uuid != null)
			this.account = ParfaitAuth.getAccount(UUID.fromString(this.uuid));

		if (this.id != null)
			this.account = ParfaitAuth.getAccountById(this.id);

		if (this.name != null)
			this.account = ParfaitAuth.getAccountByNickName(name);

		// 다른서버에 접속중이면 해당서버 핑상태 체크
		if (this.account != null && this.account.logined != null)
			if (this.serverUUID != this.account.logined)
				this.checkServerPingAlive = ParfaitAuth
						.getServerStatus(UUID.fromString(this.account.logined)) == ParfaitAuth.SERVERSTATE_IS_GREEN
								? true : false;
	}

	public void onCompletion(Server server) {
		Player player = server.getPlayer(sender);
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();
		if (player == null)
			return;

		if (this.account == null) {
			player.sendMessage(plugin.getMessage("error-cant-find-that-account"));
			return;
		}

		// 이서버에 접속중이면
		if (this.account.logined.equals(ParfaitAuth.getParfaitAuthUUID().toString())) {
			// 서버에서 킥처리
			Player target = server.getPlayer(this.account.nickname);
			if (target != null)
				target.kick(plugin.getMessage("kick-account-has-banned"), false);

			this.account.banMinute(this.period);
			this.account.setBanCause(this.cause);
			this.account.upload();
			player.sendMessage(plugin.getMessage("success-account-banned"));
			return;
		}

		// 다른서버에 접속중이면
		if (this.account.logined != null && this.checkServerPingAlive) {
			ArrayList<String> data = new ArrayList<String>();
			data.add(this.uuid);
			data.add(this.id);
			data.add(this.name);
			data.add(String.valueOf(this.period));
			data.add(this.cause);
			Notification.push(this.account.logined, "hmhmmhm.ParfaitAuth.Tasks.BanAccountTask", data, true);
			player.sendMessage(plugin.getMessage("success-account-ban-process-sent"));
			return;
		}

		// 오프라인 상태면
		this.account.banMinute(this.period);
		this.account.setBanCause(this.cause);
		this.account.upload();
		player.sendMessage(plugin.getMessage("success-account-banned"));
	}
}
