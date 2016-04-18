package hmhmmhm.ParfaitAuth.Tasks;

import java.util.ArrayList;
import java.util.UUID;

import com.mongodb.util.JSON;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.Notification;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class ChangeAccountTypeTask extends AsyncTask {
	private String id, serverUUID;
	private String sender = null;
	private int type;
	private int result;
	private int resultAct;
	private String gotoUUID;
	private Account account = null;
	private boolean force;

	public final static int USER_ALREADY_LOGINED_ANOTHER_SERVER = 0;
	public final static int USER_ALREADY_LOGINED_THIS_SERVER = 1;
	public final static int CHANGE_COMPLETE = 2;

	public ChangeAccountTypeTask(String sender, String id, String serverUUID, int type, boolean force) {
		this.id = id;
		this.serverUUID = serverUUID;
		this.type = type;
		this.force = force;
	}

	@Override
	public void onRun() {
		this.account = ParfaitAuth.getAccountById(this.id);

		// 유저 계정정보가 없는 경우
		if (this.account == null)
			return;

		if (!this.force) {
			// 다른서버에 로그인 한 경우
			if (this.account.logined != null && this.account.logined != this.serverUUID) {
				this.result = ParfaitAuth.getServerStatus(UUID.fromString(this.account.logined));
				this.resultAct = USER_ALREADY_LOGINED_ANOTHER_SERVER;
				this.gotoUUID = this.account.logined;
				return;
			}

			if (this.account.logined == this.serverUUID) {
				this.resultAct = USER_ALREADY_LOGINED_THIS_SERVER;
				return;
			}
		}

		this.account.accountType = this.type;
		ParfaitAuth.updateAccount(this.account.uuid, this.account.convertToDocument());

		this.resultAct = CHANGE_COMPLETE;
	}

	@Override
	public void onCompletion(Server server) {
		if (this.force)
			return;

		Player player = server.getPlayer(this.sender);
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		// 명령실행한 유저가 접속중이 아니면 리턴
		if (player == null)
			return;

		// 해당 아이디가 존재하지 않을경우
		if (this.account == null) {
			player.sendMessage(plugin.getMessage("error-cant-find-that-id-account"));
			return;
		}

		// 해당 유저가 다른서버에 있을경우 해당서버에 비동기로 처리요청 전송
		if (this.resultAct == USER_ALREADY_LOGINED_ANOTHER_SERVER) {
			if (this.result == ParfaitAuth.SERVERSTATE_IS_GREEN) {
				ArrayList<Object> data = new ArrayList<>();
				data.add(id);
				data.add(type);

				Notification.push(this.gotoUUID, "hmhmmhm.ParfaitAuth.Tasks.ChangeAccountTypeTask", data, true);
				player.sendMessage(plugin.getMessage("status-requested-change-account-type-to-another-server"));
				return;
			}
		}

		// 유저가 이 서버에 접속한 상태일경우
		if (this.resultAct == USER_ALREADY_LOGINED_THIS_SERVER) {
			Account loginedAccountData = ParfaitAuth.authorisedID.get(this.account.uuid);
			if (loginedAccountData != null) {
				loginedAccountData.accountType = this.type;
				player.sendMessage(plugin.getMessage("success-account-type-changed"));
			} else {
				// 이서버에서 비정상 종료된 경우 타입정보 덮어쓰기
				player.sendMessage(plugin.getMessage("caution-account-was-unstable-logouted-rewrited-type-data"));
				server.getScheduler().scheduleAsyncTask(new ChangeAccountTypeTask(null, id, serverUUID, type, true));
			}
			return;
		}

		if (this.resultAct == CHANGE_COMPLETE)
			player.sendMessage(plugin.getMessage("success-account-type-changed"));
	}
}
