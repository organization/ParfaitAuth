package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.event.Event;

public class NotificationReceiveEvent extends Event {
	public String identifier;
	public Object object;

	public NotificationReceiveEvent(String identifier, Object object) {
		this.identifier = identifier;
		this.object = object;
	}
}
