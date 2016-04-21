package hmhmmhm.ParfaitAuth.Events;

import java.util.UUID;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import hmhmmhm.ParfaitAuth.Account;

public class LogoutEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private UUID uuid;
	private Account account;

	public static HandlerList getHandlers() {
		return handlers;
	}

	public LogoutEvent(UUID uuid, Account account) {
		this.uuid = uuid;
		this.account = account;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public Account getAccount() {
		return this.account;
	}
}
