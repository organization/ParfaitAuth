package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class UnregisterEvent extends Event implements Cancellable {
	private Player player;
	public String reason = null;

	private static final HandlerList handlers = new HandlerList();
	public boolean isCancelled = false;

	public UnregisterEvent(Player player) {
		this.player = player;
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

	public Player getPlayer() {
		return this.player;
	}
}
