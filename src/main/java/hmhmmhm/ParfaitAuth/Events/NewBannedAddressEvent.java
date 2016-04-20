package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class NewBannedAddressEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public String address;
	public Long period;

	public boolean isCancelled = false;

	public static HandlerList getHandlers() {
		return handlers;
	}

	public NewBannedAddressEvent(String address, Long period) {
		this.address = address;
		this.period = period;
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
