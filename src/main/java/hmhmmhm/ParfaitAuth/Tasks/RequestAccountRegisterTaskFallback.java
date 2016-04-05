package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Commands.RegisterCommand;

public class RequestAccountRegisterTaskFallback extends Task {
	private String username;
	private UUID taskUUID;

	public RequestAccountRegisterTaskFallback(String username, UUID taskUUID) {
		this.username = username;
		this.taskUUID = taskUUID;
	}

	@Override
	public void onRun(int currentTick) {
		UUID nowTaskUUID = RegisterCommand.taskMap.get(this.username);

		if (nowTaskUUID == null || !(nowTaskUUID instanceof UUID))
			return;

		if (nowTaskUUID.toString() == this.taskUUID.toString()) {
			RegisterCommand.taskMap.remove(this.username);

			Player player = Server.getInstance().getPlayer(username);

			if (!(player instanceof Player))
				return;

			player.sendMessage(ParfaitAuthPlugin.getPlugin().getMessage("error-db-request-in-progress"));
		}
	}
}
