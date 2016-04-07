package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.ParfaitAuth;

public class CheckAuthorizationIDTask extends AsyncTask {
	private String username;
	private UUID uuid;
	private String id;
	private String pw;

	public CheckAuthorizationIDTask(String username, UUID uuid, String id, String pw) {
		this.username = username;
		this.uuid = uuid;
		this.id = id;
		this.pw = pw;
	}

	@Override
	public void onRun() {
		// CheckAuthorizationIDTask
		Account idAccount = ParfaitAuth.getAccountById(this.id);
		String inputPasswordHash = ParfaitAuth.hash(this.pw);

		// 계정을 찾을 수 없거나 비밀번호가 틀린경우
		if (idAccount == null || ((idAccount instanceof Account) && (inputPasswordHash != idAccount.password))) {
			// TODO 어카운트 정보가 없으면 찾을 수 없다고 합니다.
			// TODO 어카운트 비밀번호 해시가 안 맞으면 비밀번호가 틀리다고 합니다.
			
			Account uuidAccount = ParfaitAuth.getAccount(this.uuid);

			// 브루트포스가 5회 이상이면
			// TODO additionalData 의 auth_bruteforce 를 삭제처리합니다.
			// TODO 해당 유저를 킥처리합니다.
			// TODO 해당 아이피를 차단처리합니다.

			// 브루트포스가 5회 미만이면
			// TODO 해당 UUID계정의 additionalData에 'auth_bruteforce'를 +1한 후 계정정보
			// 업데이트합니다.
			// TODO 5번 이상 틀리면 30분간 서버접근이 차단된다는 경고를 띄웁니다.
		}

		// 계정을 찾았고 비밀번호가 맞는 경우
		// TODO logined가 이 서버일 경우 이 서버가 크래시되었던 것으로 간주하고 로그인포스 true 처리.
		// TODO logined가 null나 false가 아닌 경우 해당 서버의 핑상태를 확인후 죽었으면 로그인포스를
		// true처리합니다.
		// TODO authorizationID 를 호출합니다.
	}

}
