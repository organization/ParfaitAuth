package hmhmmhm.ParfaitAuth.Tasks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class RemoveAccountDataTask extends AsyncTask {
	private String sender, id, serverUUID;
	private Account account;
	private int result;

	public RemoveAccountDataTask(String sender, String id, String serverUUID) {
		this.sender = sender;
		this.id = id;
		this.serverUUID = serverUUID;
	}

	@Override
	public void onRun() {
		// TODO Auto-generated method stub
		this.account = ParfaitAuth.getAccountById(this.id);

		if (this.account == null)
			return;

		this.result = ParfaitAuth.deleteAccount(this.account.uuid);
	}

	@Override
	public void onCompletion(Server server) {
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();
		Player player = server.getPlayer(this.sender);

		if (player == null)
			return;

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
