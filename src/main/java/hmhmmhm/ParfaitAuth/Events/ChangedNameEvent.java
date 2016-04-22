package hmhmmhm.ParfaitAuth.Events;

import cn.nukkit.Player;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class ChangedNameEvent extends Event{
	private static final HandlerList handlers = new HandlerList();

	private Player player;
	private String oldName;
	private String newName;
	
	public ChangedNameEvent(Player player, String oldName, String newName){
		this.player = player;
		this.oldName = oldName;
		this.newName = newName;
	}
	
	public static HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer(){
		return this.player;
	}
	
	public String getOldName(){
		return this.oldName;
	}
	
	public String getNewName(){
		return this.newName;
	}
}
