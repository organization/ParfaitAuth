package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import hmhmmhm.ParfaitAuth.Account;

public class LoginEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private Player player;
	private Account account;
	public String reason = null;

	public boolean isCancelled = false;

	public static HandlerList getHandlers() {
		return handlers;
	}

	public LoginEvent(Player player, Account account) {
		this.player = player;
		this.account = account;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Account getAccount() {
		return this.account;
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
