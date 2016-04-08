package hmhmmhm.ParfaitAuth;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.util.JSON;

import cn.nukkit.Server;
import hmhmmhm.ParfaitAuth.Tasks.NotificationPushTask;

public class Notification {
	/**
	 * 자료를 활성화된 모든서버에 전송합니다.<br>
	 * 식별자를 통해서 자료전달자간 서로 식별해야합니다.
	 * 
	 * @param identifier
	 * @param object
	 */
	public static void push(String identifier, Object object) {
		Notification.push(identifier, object, true);
	}

	/**
	 * 자료를 활성화된 모든서버에 전송합니다.<br>
	 * 식별자를 통해서 자료전달자간 서로 식별해야합니다.
	 * 
	 * @param identifier
	 * @param object
	 * @param async
	 */
	public static void push(String identifier, Object object, boolean async) {
		if (async) {
			Server.getInstance().getScheduler().scheduleAsyncTask(new NotificationPushTask(identifier, object));
		} else {
			String json = JSON.serialize(object);

			if (identifier == null || json == null)
				return;

			LinkedHashMap<String, Document> serverList = ParfaitAuth.getAllServers();

			for (Entry<String, Document> entry : serverList.entrySet()) {
				String serverUUID = entry.getKey();
				Document serverDocument = entry.getValue();

				ParfaitAuth.pushNotification(serverDocument, serverUUID, identifier, json);
			}
		}
		return;
	}

	/**
	 * 자료를 원하는 서버에 전송합니다.<br>
	 * 식별자를 통해서 자료전달자간 서로 식별해야합니다.
	 * 
	 * @param serveruuid
	 * @param identifier
	 * @param object
	 * @param async
	 */
	public static void push(String serveruuid, String identifier, Object object, boolean async) {
		if (async) {
			Server.getInstance().getScheduler()
					.scheduleAsyncTask(new NotificationPushTask(serveruuid, identifier, object));
		} else {
			String json = JSON.serialize(object);

			if (identifier == null || json == null)
				return;

			Document serverDocument = ParfaitAuth.getServerDocument(serveruuid);
			ParfaitAuth.pushNotification(serverDocument, serveruuid, identifier, json);
		}
		return;
	}
}