package hmhmmhm.ParfaitAuth;

import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class EventHandler implements Listener {
	ParfaitAuthPlugin plugin;

	public EventHandler(ParfaitAuthPlugin parfaitAuthPlugin) {
		this.plugin = parfaitAuthPlugin;
	}

	@cn.nukkit.event.EventHandler
	public void onDataPacketReceiveEvent(DataPacketReceiveEvent event) {
		DataPacket packet = event.getPacket();

		switch (packet.pid()) {
		case ProtocolInfo.LOGIN_PACKET:
			// 비인가자 닉네임 unauthorized_0 과 같이 변경
			if (packet instanceof LoginPacket)
				((LoginPacket) packet).username = "unauthorized_" + ParfaitAuth.unauthorizedUserCount++;
			break;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerLoginEvent(PlayerLoginEvent event) {
		ParfaitAuth.unauthorizedAccess(event.getPlayer());
	}

	public String getMessage(String key) {
		return this.plugin.getMessage(key);
	}

	// TODO 비인가자가 사용해선 안될 모든 이벤트 함수화 후 차단처리
}