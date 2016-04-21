package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import org.bson.Document;

import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.ParfaitAuth;

public class UpdateAccountTask extends AsyncTask {
	private UUID uuid;
	private Document document;

	public UpdateAccountTask(UUID uuid, Document document) {
		this.uuid = uuid;
		this.document = document;
	}

	@Override
	public void onRun() {
		ParfaitAuth.updateAccount(this.uuid, this.document);
	}
}