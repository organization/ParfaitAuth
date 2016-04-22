package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;

public class CheckUnauthorizedAccessTask extends AsyncTask {
	private UUID uuid;
	private String username;
	private Account account = null;

	public CheckUnauthorizedAccessTask(UUID uuid, String username) {
		this.uuid = uuid;
		this.username = username;
	}

	@Override
	public void onRun() {
		Account account = ParfaitAuth.getAccount(this.uuid);

		// 계정정보가 존재하지 않으면 계정을 생성합니다.
		if (account == null || !(account instanceof Account))
			return;

		this.account = account;
	}

	public void onCompletion(Server server) {
		Player player = server.getPlayer(this.username);

		// 계정정보가 존재하지 않으면 UUID계정 생성
		if (this.account == null) {
			server.getScheduler().scheduleAsyncTask(new CreateNewUUIDAccountTask(this.uuid, this.username));
			return;
		}

		// 플레이어가 접속 중이 아니면 반환
		if (player == null || !player.isConnected())
			return;

		// ID가 미포함인 정보일 경우 authorizationUUID를 돌립니다.
		if (this.account.id == null) {
			ParfaitAuth.authorizationUUID(player, this.account);
			return;
		}

		// ID가 포함된 정보일 경우 authorizationID를 돌리고,
		ParfaitAuth.authorizationID(player, this.account, false, false);
	}
}
