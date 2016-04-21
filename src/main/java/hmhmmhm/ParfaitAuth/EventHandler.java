package hmhmmhm.ParfaitAuth;

import java.util.ArrayList;
import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryOpenEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemConsumeEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Events.NotificationReceiveEvent;
import hmhmmhm.ParfaitAuth.Tasks.BanAccountTask;
import hmhmmhm.ParfaitAuth.Tasks.ChangeAccountTypeTask;
import hmhmmhm.ParfaitAuth.Tasks.RemoveAccountDataTask;

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
		if (event.isCancelled())
			return;

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
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 움직이지 못하게 합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 상호작용할 수 없게 합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 아이템을 버릴 수 없게 합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 아이템을 주울 수 없게 합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 데미지를 받을 수 없게 합니다.
		if (event.getEntity() instanceof Player) {
			if (ParfaitAuth.unauthorised.get(((Player) event.getEntity()).getUniqueId()) != null) {
				event.setCancelled();
				return;
			}
		}

		// 인증을 거치지 않았으면 데미지를 줄 수 없게 합니다.
		if (event instanceof EntityDamageByEntityEvent) {
			if (ParfaitAuth.unauthorised
					.get(((Player) ((EntityDamageByEntityEvent) event).getDamager()).getUniqueId()) != null) {
				event.setCancelled();
				return;
			}
		}
	}

	@cn.nukkit.event.EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 블럭을 캘 수 없게합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 블럭을 배치할 수 없게합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}
	}

	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 인벤토리창을 열 수 없게 합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerPreLoginEvent(PlayerPreLoginEvent event){
		if (event.isCancelled())
			return;
		
		ParfaitAuth.unauthorizedAccess(event.getPlayer());
	}
	
	@cn.nukkit.event.EventHandler
	public void onPlayerLoginEvent(PlayerLoginEvent event) {
		if (event.isCancelled())
			return;
		
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

	@SuppressWarnings("unchecked")
	@cn.nukkit.event.EventHandler
	public void onNotificationReceiveEvent(NotificationReceiveEvent event) {
		switch (event.identifier) {

		// 타서버 관리자가 이서버의 유저의 계정유형을 바꾸는 명령실행시
		case "hmhmmhm.ParfaitAuth.Tasks.ChangeAccountTypeTask":
			ArrayList<Object> changeAccountTypeTaskData = (ArrayList<Object>) event.object;

			if (changeAccountTypeTaskData == null)
				return;

			String id = (String) changeAccountTypeTaskData.get(0);
			int type = (int) changeAccountTypeTaskData.get(1);

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

		// 타서버 관리자가 이서버의 유저를 차단하려는 경우
		case "hmhmmhm.ParfaitAuth.Tasks.BanAccountTask":
			ArrayList<Object> banAccountTaskData = (ArrayList<Object>) event.object;

			if (banAccountTaskData == null)
				return;

			String uuid1 = (String) banAccountTaskData.get(0);
			String id1 = (String) banAccountTaskData.get(1);
			String name = (String) banAccountTaskData.get(2);
			String cause = (String) banAccountTaskData.get(4);

			int period;
			try {
				period = Integer.valueOf((String) banAccountTaskData.get(3));
			} catch (NumberFormatException e) {
				return;
			}

			BanAccountTask task = new BanAccountTask();
			task.uuid = uuid1;
			task.id = id1;
			task.name = name;
			task.cause = cause;
			task.period = period;
			task.serverUUID = ParfaitAuth.getParfaitAuthUUID().toString();
			this.getServer().getScheduler().scheduleAsyncTask(task);
			break;

		// 타서버 관리자가 이서버의 유저계정을 삭제하려는 경우
		case "hmhmmhm.ParfaitAuth.Tasks.RemoveAccountDataTask":
			String id2 = (String) event.object;
			this.getServer().getScheduler()
					.scheduleAsyncTask(new RemoveAccountDataTask("", id2, ParfaitAuth.getParfaitAuthUUID().toString()));
			break;

		// 타서버 관리자가 네트워크주소를 추가로 차단한 경우
		case "hmhmmhm.ParfaitAuth.Tasks.BanAddressTask":
			ArrayList<Object> banAddressTask = (ArrayList<Object>) event.object;
			String address = (String) banAddressTask.get(0);
			String period1 = (String) banAddressTask.get(1);

			if (address != null) {
				try {
					plugin.addedBannedAddress(address, Long.valueOf(period1));
				} catch (NumberFormatException e) {
					return;
				}
			}
			break;
		}
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerChatEvent(PlayerChatEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 채팅을 할 수 없게 합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}

		// AccountFindCommand 용
		if (lastChatList.size() == 20) {
			lastChatList.remove(0);
			lastChatList.add(event.getPlayer());
			return;
		}
		lastChatList.add(event.getPlayer());
	}

	@cn.nukkit.event.EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled())
			return;

		// 인증을 거치지 않았으면 명령어 사용불가능하게 합니다.
		if (ParfaitAuth.unauthorised.get(event.getPlayer().getUniqueId()) != null) {
			event.setCancelled();
			return;
		}
	}

	public String getMessage(String key) {
		return this.plugin.getMessage(key);
	}

	public Server getServer() {
		return this.server;
	}
}