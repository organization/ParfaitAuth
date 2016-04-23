package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import hmhmmhm.ParfaitAuth.Account;

public class TemporaryUUIDAccountDeletedEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Account oldAccount;
	private Account newAccount;

	public static HandlerList getHandlers() {
		return handlers;
	}

	public TemporaryUUIDAccountDeletedEvent(Account oldAccount, Account newAccount) {
		this.oldAccount = oldAccount;
		this.newAccount = newAccount;
	}

	public Account getOldAccount() {
		return this.oldAccount;
	}

	public Account getNewAccount() {
		return this.newAccount;
	}
}
