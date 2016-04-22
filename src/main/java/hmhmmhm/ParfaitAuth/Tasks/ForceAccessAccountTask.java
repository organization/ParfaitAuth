package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.Notification;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class ForceAccessAccountTask extends AsyncTask {
	private String sender, id, serverUUID;
	private Account account;
	private int result;
	private int resultAct;
	private String gotoUUID;

	public final static int USER_ALREADY_LOGINED_ANOTHER_SERVER = 0;
	public final static int USER_ALREADY_LOGINED_THIS_SERVER = 1;
	public final static int USER_IS_OFFLINE = 2;

	public ForceAccessAccountTask(String sender, String id, String serverUUID) {
		this.sender = sender;
		this.id = id;
		this.serverUUID = serverUUID;
	}

	@Override
	public void onRun() {
		this.account = ParfaitAuth.getAccountById(this.id);

		if (this.account == null) {
			// 계정이 없으면 반환처리
			return;
		}

		// 다른서버에 로그인 한 경우
		if (this.account.logined != null && !this.account.logined.equals(this.serverUUID)) {
			this.result = ParfaitAuth.getServerStatus(UUID.fromString(this.account.logined));
			this.resultAct = USER_ALREADY_LOGINED_ANOTHER_SERVER;
			this.gotoUUID = this.account.logined;
			return;
		}

		if (this.account.logined.equals(this.serverUUID)) {
			// 이 서버에 접속한 경우
			this.resultAct = USER_ALREADY_LOGINED_THIS_SERVER;
			return;
		} else {
			// 어느서버에도 접속하지 않은경우
			this.resultAct = USER_IS_OFFLINE;
			return;
		}
	}

	@Override
	public void onCompletion(Server server) {
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();
		Player player = server.getPlayer(sender);

		// 명령어를 실행한 관리자가 없으면 반환
		if (player == null)
			return;

		// 아이디 계정을 찾지 못했으면 반환
		if (this.account == null) {
			player.sendMessage(plugin.getMessage("error-cant-find-that-id-account"));
			return;
		}

		// 유저가 다른서버에 이미 접속중이면 해당서버에 접속종료 요청
		if (this.resultAct == USER_ALREADY_LOGINED_ANOTHER_SERVER && this.result == ParfaitAuth.SERVERSTATE_IS_GREEN) {
			player.sendMessage(plugin.getMessage("status-outcom-process-force-access"));
			Notification.push(this.gotoUUID, "hmhmmhm.ParfaitAuth.Tasks.ForceAccessAccountTask",
					this.account.uuid.toString(), true);
		}

		// 유저가 이 서버에 접속중인 것으로 나타나면
		if (this.resultAct == USER_ALREADY_LOGINED_THIS_SERVER) {
			Account idAccount = ParfaitAuth.authorisedID.get(this.account.uuid);
			// 접속중인 ID계정을 찾아내면
			if (idAccount != null) {
				Player target = server.getPlayer(idAccount.nickname);
				// 유저 인스턴스가 존재하면
				if (target != null)
					target.kick(plugin.getMessage("kick-account-force-connected"), false);
			}
		}
		Account senderAccount = ParfaitAuth.getAccount(player.getUniqueId());
		ParfaitAuth.release(player.getUniqueId(), senderAccount, true, true);

		// 유저가 오프라인상태인경우
		ParfaitAuth.authorizationID(player, this.account, true, true);
	}
}