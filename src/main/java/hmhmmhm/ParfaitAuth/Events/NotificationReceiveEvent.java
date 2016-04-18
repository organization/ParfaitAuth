package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class NotificationReceiveEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public String identifier;
	public Object object;

	public static HandlerList getHandlers() {
		return handlers;
	}

	public NotificationReceiveEvent(String identifier, Object object) {
		this.identifier = identifier;
		this.object = object;
	}
}
