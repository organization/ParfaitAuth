package hmhmmhm.ParfaitAuth.Tasks;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.util.JSON;

import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.ParfaitAuth;

public class NotificationPushTask extends AsyncTask {
	private String identifier;
	private Object object;
	private String serveruuid = null;

	public NotificationPushTask(String identifier, Object object) {
		this.identifier = identifier;
		this.object = object;
	}

	public NotificationPushTask(String serveruuid, String identifier, Object object) {
		this.identifier = identifier;
		this.object = object;
		this.serveruuid = serveruuid;
	}

	@Override
	public void onRun() {
		String json = JSON.serialize(this.object);

		if (serveruuid == null) {
			LinkedHashMap<String, Document> serverList = ParfaitAuth.getAllServers();

			for (Entry<String, Document> entry : serverList.entrySet()) {
				String serverUUID = entry.getKey();
				Document serverDocument = entry.getValue();

				ParfaitAuth.pushNotification(serverDocument, serverUUID, this.identifier, json);
			}
		} else {
			Document serverDocument = ParfaitAuth.getServerDocument(this.serveruuid);
			ParfaitAuth.pushNotification(serverDocument, this.serveruuid, this.identifier, json);
		}
	}

}
