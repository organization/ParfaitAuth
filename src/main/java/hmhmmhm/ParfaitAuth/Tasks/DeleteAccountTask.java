package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.ParfaitAuth;

public class DeleteAccountTask extends AsyncTask {
	private String uuid;

	public DeleteAccountTask(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public void onRun() {
		if (this.uuid != null)
			ParfaitAuth.deleteAccount(UUID.fromString(this.uuid));
	}
}
