package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class UpdateServerStatusTask extends Task {
	public static UUID uuid;

	public UpdateServerStatusTask(UUID uuid) {
		UpdateServerStatusTask.uuid = uuid;
	}

	@Override
	public void onRun(int currentTick) {
		Server.getInstance().getScheduler().scheduleAsyncTask(new AsyncTask() {
			@Override
			public void onRun() {
				ParfaitAuth.updateServerStatus(UpdateServerStatusTask.uuid);
			}
		});
	}
}
