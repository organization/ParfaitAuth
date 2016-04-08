package hmhmmhm.ParfaitAuth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.util.JSON;

public class Notification {
	public static void push(String identifier, Object object) {
		Notification.push(identifier, object, true);
	}

	public static void push(String identifier, Object object, boolean async) {
		String json = JSON.serialize(object);

		if (identifier == null || json == null)
			return;

		// TODO 아래 비동기로 변경해야함
		// TODO PUSH
		LinkedHashMap<String, Document> serverList = ParfaitAuth.getAllServers();

		for (Entry<String, Document> entry : serverList.entrySet()) {
			String serverUUID = entry.getKey();
			Document serverDocument = entry.getValue();

			ParfaitAuth.pushNotification(serverDocument, serverUUID, identifier, object);
		}
		return;
	}

	public static void push(String serveruuid, String identifier, Object object, boolean async) {
		String json = JSON.serialize(object);

		if (serveruuid == null || identifier == null || json == null)
			return;

		// TODO PUSH
		return;
	}
}