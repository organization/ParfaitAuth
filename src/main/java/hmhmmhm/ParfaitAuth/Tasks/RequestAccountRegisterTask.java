package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.Commands.RegisterCommand;

public class RequestAccountRegisterTask extends AsyncTask {
	private String id, pw, timestamp, userIp, nickname;
	private UUID uuid, taskUUID, serverUUID;

	private int resultState;
	private Account result;
	public static final int CLIENT_IS_DEAD = 0;
	public static final int SUCCESS = 1;
	public static final int ID_ACCOUNT_ALREADY_EXIST = 2;
	public static final int UUID_ACCOUNT_NOT_EXIST = 3;
	public static final int USER_DATA_NOT_EXIST = 4;

	public RequestAccountRegisterTask(String id, String pw, UUID uuid, UUID taskUUID, String timestamp, String userIp,
			UUID serverUUID, String nickname) {
		this.id = id;
		this.pw = pw;
		this.uuid = uuid;
		this.taskUUID = taskUUID;
		this.timestamp = timestamp;
		this.userIp = userIp;
		this.serverUUID = serverUUID;
		this.nickname = nickname;
	}

	@Override
	public void onRun() {
		Account idAccountCheck = ParfaitAuth.getAccountById(id);
		Account uuidAccountCheck = ParfaitAuth.getAccount(uuid);

		// 아이디 계정이 이미 존재하는 경우
		if (idAccountCheck != null) {
			this.resultState = RequestAccountRegisterTask.ID_ACCOUNT_ALREADY_EXIST;
			return;
		}

		// UUID 계정이 발급되지 않은 경우 (*예외상황)
		// 무언가 심각한 에러로 초기 계정발급이 되지 않았을경우
		if (uuidAccountCheck == null || !(uuidAccountCheck instanceof Account)) {
			this.resultState = RequestAccountRegisterTask.UUID_ACCOUNT_NOT_EXIST;
			return;
		}

		if (uuidAccountCheck != null && uuidAccountCheck instanceof Account) {
			// 이미 아이디로 로그인한 상태일 경우
			if (uuidAccountCheck.id != null)
				this.resultState = RequestAccountRegisterTask.ID_ACCOUNT_ALREADY_EXIST;

			// 가입 진행
			Account newAccount = new Account();
			newAccount.id = this.id;
			newAccount.lastDate = this.timestamp;
			newAccount.lastIp = this.userIp;
			newAccount.logined = this.serverUUID.toString();
			newAccount.nickname = this.nickname;
			newAccount.password = this.pw;
			newAccount.uuid = this.uuid;

			int result = ParfaitAuth.updateAccount(this.uuid, newAccount.convertToDocument());
			switch (result) {
			case ParfaitAuth.CLIENT_IS_DEAD:
				this.resultState = RequestAccountRegisterTask.CLIENT_IS_DEAD;
				break;
			case ParfaitAuth.USER_DATA_NOT_EXIST:
				this.resultState = RequestAccountRegisterTask.USER_DATA_NOT_EXIST;
				break;
			case ParfaitAuth.SUCCESS:
				this.resultState = RequestAccountRegisterTask.SUCCESS;
				this.result = newAccount;
				break;
			}
			return;
		}
	}

	@Override
	public void onCompletion(Server server) {
		if (this.resultState == RequestAccountRegisterTask.SUCCESS) {
			RegisterCommand.registerCallback(this.nickname, this.taskUUID, this.result);
		} else {
			RegisterCommand.registerFailCallback(this.nickname, this.resultState);
		}
	}

}
