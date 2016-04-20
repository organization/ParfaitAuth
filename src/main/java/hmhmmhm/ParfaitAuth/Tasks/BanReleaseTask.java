package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class BanReleaseTask extends AsyncTask {
	public String id = null;
	public String nick = null;
	public String uuid = null;
	public String ip = null;
	public String subnet = null;
	public String cause = null;

	public String sender = null;
	public boolean success = false;

	@Override
	public void onRun() {
		if (this.id != null || this.nick != null || this.uuid != null) {
			Account account = null;

			if (this.id != null)
				account = ParfaitAuth.getAccountById(this.id);

			if (this.nick != null)
				account = ParfaitAuth.getAccountByNickName(this.nick);

			if (this.uuid != null)
				account = ParfaitAuth.getAccount(UUID.fromString(this.uuid));

			if (account != null) {
				account.banned = null;
				account.setLastBanReleaseCause(this.cause);
				account.upload();
				this.success = true;
			}
		}

		if (this.ip != null)
			this.success = ParfaitAuth.deleteBannedAddress(this.ip) == ParfaitAuth.SUCCESS;

		if (this.subnet != null)
			this.success = ParfaitAuth.deleteBannedAddress(this.subnet) == ParfaitAuth.SUCCESS;
	}

	public void onCompletion(Server server) {
		Player player = server.getPlayer(this.sender);
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		if (player == null)
			return;

		String message = success ? plugin.getMessage("success-ban-released")
				: plugin.getMessage("error-cant-find-that-account");

		player.sendMessage(message);
	}
}
