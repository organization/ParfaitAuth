package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class CheckAuthorizationIDTask extends AsyncTask {
	private String username;
	private UUID serverUUID;
	private String id;
	private String pw;

	private Account idAccount = null;
	private boolean loginedForce = false;
	private boolean passwordCorrect = false;
	private boolean pwCheckPassForce;

	public CheckAuthorizationIDTask(String username, String id, String pw, UUID serverUUID, boolean pwCheckPassForce) {
		this.username = username;
		this.id = id;
		this.pw = pw;
		this.serverUUID = serverUUID;
		this.pwCheckPassForce = pwCheckPassForce;
	}

	@Override
	public void onRun() {
		String inputPasswordHash = ParfaitAuth.hash(this.pw);

		this.idAccount = ParfaitAuth.getAccountById(this.id);
		this.passwordCorrect = inputPasswordHash.equals(this.idAccount.password);

		if (this.pwCheckPassForce)
			this.passwordCorrect = true;

		// 인증서버의 오프라인 유무를 사전검사
		if ((idAccount.logined != null && !idAccount.logined.equals(this.serverUUID.toString()))) {
			switch (ParfaitAuth.getServerStatus(UUID.fromString(idAccount.logined))) {
			case ParfaitAuth.SERVERSTATE_IS_NULL:
			case ParfaitAuth.SERVERSTATE_IS_RED:
				this.loginedForce = true;
				break;
			}
		}
	}

	@Override
	public void onCompletion(Server server) {
		Player player = server.getPlayer(this.username);

		// 유저가 서버접근 중이 아니라면 중단
		if (player == null || !player.isConnected())
			return;

		// 계정을 찾을 수 없거나 비밀번호가 틀린경우
		if (this.idAccount == null || ((this.idAccount instanceof Account) && !this.passwordCorrect)) {
			ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

			if (this.idAccount == null)
				player.sendMessage(plugin.getMessage("error-cant-find-that-id-account"));

			if ((this.idAccount instanceof Account) && !this.passwordCorrect)
				player.sendMessage(plugin.getMessage("error-cant-find-that-id-account-pw-wrong"));

			Account account = ParfaitAuth.authorisedUUID.get(player.getUniqueId());
			if (account == null) {
				// 디버깅용 에러코드
				player.sendMessage(plugin.getMessage("error-db-cant-find-your-account"));
				plugin.getLogger().critical(plugin.getMessage("error-uuid-account-not-exist-user-founded"));
				plugin.getLogger().critical("Please report deveolper error code: CAIDT-onCompletion()");
				return;
			}

			// 오진아웃제 경고1 추가
			boolean needBan = account.warn(1);

			if (needBan) {
				// 오진아웃제를 초기화하고, 30분간 밴하고, kick처리
				account.fiveStrikes = 0;
				account.banMinute(30);
				player.kick();
			} else {
				player.sendMessage(plugin.getMessage("caution-if-you-are-missmatch-pw-five-time-willbe-ban"));
			}
			return;
		}

		// 계정을 찾았고 비밀번호가 맞는 경우
		ParfaitAuth.authorizationID(player, this.idAccount, false, this.loginedForce);
	}
}
