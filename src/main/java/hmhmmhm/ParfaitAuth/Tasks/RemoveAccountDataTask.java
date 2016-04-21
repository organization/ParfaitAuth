package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.Notification;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class RemoveAccountDataTask extends AsyncTask {
	private String sender, id, serverUUID;
	private Account account;
	private int result;

	private boolean isOtherServer = false;

	public RemoveAccountDataTask(String sender, String id, String serverUUID) {
		this.sender = sender;
		this.id = id;
		this.serverUUID = serverUUID;
	}

	@Override
	public void onRun() {
		this.account = ParfaitAuth.getAccountById(this.id);

		if (this.account == null)
			return;

		// 다른서버에 접속중이면 해당서버에 요청전송
		if (this.account.logined != null && !this.account.logined.equals(this.serverUUID)) {
			if (ParfaitAuth
					.getServerStatus(UUID.fromString(this.account.logined)) == ParfaitAuth.SERVERSTATE_IS_GREEN) {
				Notification.push(this.account.logined, "hmhmmhm.ParfaitAuth.Tasks.RemoveAccountDataTask", id, true);
				this.isOtherServer = true;
				return;
			}
		}

		this.result = ParfaitAuth.deleteAccount(this.account.uuid);
	}

	@Override
	public void onCompletion(Server server) {
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();
		Player player = server.getPlayer(this.sender);

		if (player == null)
			return;

		if (this.isOtherServer) {
			player.sendMessage(plugin.getMessage("status-requested-remove-account-to-another-server"));
			return;
		}

		if (this.account == null) {
			player.sendMessage(plugin.getMessage("error-cant-find-that-id-account"));
			return;
		}

		switch (this.result) {
		case ParfaitAuth.CLIENT_IS_DEAD:
		case ParfaitAuth.NOT_EXIST_ACCOUNT:
			player.sendMessage(plugin.getMessage("error-cant-find-that-id-account"));
			break;
		case ParfaitAuth.SUCCESS:
			player.sendMessage(plugin.getMessage("success-that-account-was-deleted"));
		}
	}
}
