package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;

public class NotificationSendEvent extends Event implements Cancellable {
	public String serverUUID = null;
	public String identifier;
	public Object object;

	public boolean isCancelled = false;

	public NotificationSendEvent(String identifier, Object object) {
		this.identifier = identifier;
		this.object = object;
	}

	public NotificationSendEvent(String serverUUID, String identifier, Object object) {
		this.serverUUID = serverUUID;
		this.identifier = identifier;
		this.object = object;
	}

	public boolean isCancelled() {
		return this.isCancelled;
	}

	public void setCancelled() {
		this.isCancelled = true;
	}

	public void setCancelled(boolean forceCancel) {
		this.isCancelled = forceCancel;
	}
}
