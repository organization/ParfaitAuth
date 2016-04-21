package hmhmmhm.ParfaitAuth.Events;

import java.util.UUID;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class RegisterEvent extends Event implements Cancellable {
	public String id, pw;
	public UUID uuid;

	public String reason = null;

	private static final HandlerList handlers = new HandlerList();
	public boolean isCancelled = false;

	public RegisterEvent(String id, String pw, UUID uuid) {
		this.id = id;
		this.pw = pw;
		this.uuid = uuid;
	}

	public static HandlerList getHandlers() {
		return handlers;
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

	public void setCancelledReason(String reason) {
		this.reason = reason;
	}
}
