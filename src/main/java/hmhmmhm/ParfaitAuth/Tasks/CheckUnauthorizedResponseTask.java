package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class CheckUnauthorizedResponseTask extends Task {
	private String username;
	private UUID uuid;

	private int triedCount = 0;

	public CheckUnauthorizedResponseTask(String username, UUID uuid) {
		this.username = username;
		this.uuid = uuid;
	}

	@Override
	public void onRun(int currentTick) {
		Player player = Server.getInstance().getPlayer(this.username);

		if (player == null) {
			this.cancel();
			return;
		}

		if (this.triedCount >= 3) {
			player.kick(ParfaitAuthPlugin.getPlugin().getMessage("kick-authorization-failed"), false);
			this.cancel();
			return;
		}

		if (ParfaitAuth.unauthorised.get(this.uuid) != null)
			ParfaitAuth.unauthorizedAccess(player);

		this.triedCount++;
	}

}
