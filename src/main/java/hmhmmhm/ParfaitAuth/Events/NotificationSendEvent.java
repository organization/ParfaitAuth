package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class NotificationSendEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public String serverUUID = null;
	public String identifier;
	public Object object;

	public boolean isCancelled = false;

    public static HandlerList getHandlers() {
        return handlers;
    }

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
