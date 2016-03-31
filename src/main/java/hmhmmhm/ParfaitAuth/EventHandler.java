package hmhmmhm.ParfaitAuth;

import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerLoginEvent;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class EventHandler implements Listener {
	ParfaitAuthPlugin plugin;

	public EventHandler(ParfaitAuthPlugin parfaitAuthPlugin) {
		this.plugin = parfaitAuthPlugin;
	}

	@cn.nukkit.event.EventHandler
	public void PlayerLogin(PlayerLoginEvent event) {
		event.getPlayer().sendMessage(this.getMessage("status-start-get-account-data"));
		//TODO
	}

	public String getMessage(String key) {
		return this.plugin.getMessage(key);
	}
}