package hmhmmhm.ParfaitAuth.Tasks;

import java.util.LinkedHashMap;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class RetryAuthAlreadyLoginedAccountTask extends Task {
	private String username;
	private String id;

	private int triedCount = 0;
	private static LinkedHashMap<String, String> playerList = new LinkedHashMap<String, String>();

	public RetryAuthAlreadyLoginedAccountTask(String username, String id) {
		this.username = username;
		this.id = id;

		// 이미 반복시도중이므로 생성취소
		if (this.playerList.get(username) != null) {
			this.getHandler().cancel();
			return;
		}

		this.playerList.put(username, id);
	}

	@Override
	public void onRun(int currentTick) {
		Player player = Server.getInstance().getPlayer(username);

		// 유저가 존재하지 않으면(나갔으면) 테스크 중단
		if (player == null || !player.isConnected()) {
			this.playerList.remove(username);
			this.getHandler().cancel();
			return;
		}

		// 3번 반복하고 여전하면 인증실패로 간주하고 재접속유도
		if (this.triedCount >= 3) {
			this.playerList.remove(username);
			ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();
			player.sendMessage(plugin.getMessage("error-cant-use-that-account"));
			player.sendMessage(plugin.getMessage("error-taht-account-might-be-already-used"));
			player.sendMessage(plugin.getMessage("error-if-auth-server-has-problem-will-be-fixed"));
		}

		ParfaitAuth.preAuthorizationID(player, this.id, null, true);
		this.triedCount++;
	}

}