package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;

public class RequestAccountRegisterTask extends AsyncTask {
	private String id, pw;
	private UUID uuid, taskUUID;

	private int result;
	public static int SUCCESS = 1;
	public static int ID_ACCOUNT_ALREADY_EXIST = 2;
	public static int UUID_ACCOUNT_NOT_EXIST = 3;

	public RequestAccountRegisterTask(String id, String pw, UUID uuid, UUID taskUUID) {
		this.id = id;
		this.pw = pw;
		this.uuid = uuid;
		this.taskUUID = taskUUID;
	}

	@Override
	public void onRun() {
		Account idAccountCheck = ParfaitAuth.getAccountById(id);
		Account uuidAccountCheck = ParfaitAuth.getAccount(uuid);

		// 아이디 계정이 이미 존재하는 경우
		if (idAccountCheck != null) {
			this.result = RequestAccountRegisterTask.ID_ACCOUNT_ALREADY_EXIST;
			return;
		}

		// UUID 계정이 발급되지 않은 경우 (*예외상황)
		// 무언가 심각한 에러로 초기 계정발급이 되지 않았을경우
		if (uuidAccountCheck == null || !(uuidAccountCheck instanceof Account)) {
			this.result = RequestAccountRegisterTask.UUID_ACCOUNT_NOT_EXIST;
			return;
		}

		if (uuidAccountCheck != null && uuidAccountCheck instanceof Account) {
			// 이미 아이디로 로그인한 상태일 경우
			if (uuidAccountCheck.id != null)
				this.result = RequestAccountRegisterTask.ID_ACCOUNT_ALREADY_EXIST;

			// 가입 진행
			Account newAccount = new Account();
			newAccount.id = this.id;
			newAccount.lastDate = "1"; //TODO 타임스탬프
			newAccount.lastIp = ""; // TODO 유저 IP
			newAccount.logined = ""; // TODO 현재서버  IP
			newAccount.nickname = ""; // TODO 닉네임
			newAccount.password = this.pw;
			newAccount.uuid = this.uuid;
			//String timestamp, String userIP, String serverIP, String nickname
			ParfaitAuth.updateAccount(this.uuid, newAccount.convertToDocument());
			return;
		}
	}

}
