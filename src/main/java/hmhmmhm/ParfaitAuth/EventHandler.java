package hmhmmhm.ParfaitAuth;

import java.util.ArrayList;
import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Events.NotificationReceiveEvent;
import hmhmmhm.ParfaitAuth.Tasks.ChangeAccountTypeTask;

public class EventHandler implements Listener {
	private ParfaitAuthPlugin plugin;
	private Server server;

	// AccountFindCommand 에서 쓰는 식별번호 검색용 리스트
	public static ArrayList<Player> lastChatList = new ArrayList<Player>();
	public static ArrayList<Player> lastLoginList = new ArrayList<Player>();
	public static ArrayList<Player> lastLogoutList = new ArrayList<Player>();

	public EventHandler(ParfaitAuthPlugin parfaitAuthPlugin) {
		this.plugin = parfaitAuthPlugin;
		this.server = this.plugin.getServer();
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

		// AccountFindCommand 용
		if (lastLoginList.size() == 20) {
			lastLoginList.remove(0);
			lastLoginList.add(event.getPlayer());
			return;
		}
		lastLoginList.add(event.getPlayer());
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		// AccountFindCommand 용
		if (lastLogoutList.size() == 20) {
			lastLoginList.remove(0);
			lastLogoutList.add(event.getPlayer());
			return;
		}
		lastLogoutList.add(event.getPlayer());
	}

	@cn.nukkit.event.EventHandler
	public void onNotificationReceiveEvent(NotificationReceiveEvent event) {
		switch (event.identifier) {
		// 타서버 관리자가 이서버의 유저의 계정유형을 바꾸는 명령실행시
		case "hmhmmhm.ParfaitAuth.Tasks.ChangeAccountTypeTask":
			ArrayList<Object> data = (ArrayList<Object>) event.object;

			if (data == null)
				return;

			String id = (String) data.get(0);
			int type = (int) data.get(1);

			plugin.getLogger().info(
					plugin.getMessage("status-outcom-process-change-account-type") + " ID:" + id + " TYPE:" + type);

			this.getServer().getScheduler().scheduleAsyncTask(
					new ChangeAccountTypeTask(null, id, ParfaitAuth.getParfaitAuthUUID().toString(), type, false));
			break;

		// 타서버 관리자가 이서버의 유저의 계정을 이용하려는 경우
		case "hmhmmhm.ParfaitAuth.Tasks.ForceAccessAccountTask":
			String uuid = (String) event.object;
			Account account = ParfaitAuth.authorisedID.get(UUID.fromString(uuid));

			if (account != null) {
				Player player = this.getServer().getPlayer(account.nickname);
				if (player != null)
					player.kick(plugin.getMessage("kick-account-force-connected"));
			}
			break;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerChatEvent(PlayerChatEvent event) {
		if (event.isCancelled())
			return;

		// AccountFindCommand 용
		if (lastChatList.size() == 20) {
			lastChatList.remove(0);
			lastChatList.add(event.getPlayer());
			return;
		}
		lastChatList.add(event.getPlayer());
	}

	public String getMessage(String key) {
		return this.plugin.getMessage(key);
	}

	public Server getServer() {
		return this.server;
	}

	// TODO 비인가자가 사용해선 안될 모든 이벤트 함수화 후 차단처리
}