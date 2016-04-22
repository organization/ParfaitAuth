package hmhmmhm.ParfaitAuth.Tasks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class ChangeNameTask extends AsyncTask {
	private String sender = null;
	private String uuid = null;
	private String toName = null;

	private boolean isCanBeUsed = false;

	public ChangeNameTask(String sender, String uuid, String toName) {
		this.sender = sender;
		this.uuid = uuid;
		this.toName = toName;
	}

	@Override
	public void onRun() {
		// 이미 있는 닉네임인지 체크합니다.
		Account checkNameAccount = ParfaitAuth.getAccountByNickName(this.toName);

		if (checkNameAccount != null) {
			this.isCanBeUsed = false;
			return;
		}

		this.isCanBeUsed = true;
	}

	public void onCompletion(Server server) {
		Player player = server.getPlayer(this.sender);
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		if (player == null)
			return;

		// 닉네임이 이미 사용중이면 반환합니다.
		if (!!this.isCanBeUsed) {
			player.sendMessage(plugin.getMessage("error-failed-change-name-that-name-already-exist"));
			return;
		}

		Account account = ParfaitAuth.authorisedID.get(this.uuid);

		// 현재 ID계정으로 접속중이 아니라면 반환합니다.
		if (account == null) {
			player.sendMessage(plugin.getMessage("error-please-login-first"));
			return;
		}

		account.nickname = this.toName;
		account.setModified();
		account.upload();

		player.kick(plugin.getMessage("kick-successfully-account-name-changed"), false);
	}
}
