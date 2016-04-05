package hmhmmhm.ParfaitAuth;

import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.ProtocolInfo;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class EventHandler implements Listener {
	ParfaitAuthPlugin plugin;

	public EventHandler(ParfaitAuthPlugin parfaitAuthPlugin) {
		this.plugin = parfaitAuthPlugin;
	}
	
	@cn.nukkit.event.EventHandler
	public void onDataPacketReceiveEvent(DataPacketReceiveEvent event){
		switch(event.getPacket().pid()){
		case ProtocolInfo.LOGIN_PACKET:
			break;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerLoginEvent(PlayerLoginEvent event) {
		event.getPlayer().sendMessage(this.getMessage("status-start-get-account-data"));
	}

	public String getMessage(String key) {
		return this.plugin.getMessage(key);
	}
}