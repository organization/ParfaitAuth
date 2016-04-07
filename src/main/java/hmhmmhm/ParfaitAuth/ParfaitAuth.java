package hmhmmhm.ParfaitAuth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import hmhmmhm.ParfaitAuth.Tasks.CheckAuthorizationIDTask;
import hmhmmhm.ParfaitAuth.Tasks.CheckUnauthorizedAccessTask;
import hmhmmhm.ParfaitAuth.Tasks.CreateNewUUIDAccountTask;
import hmhmmhm.ParfaitAuth.Tasks.RetryAuthAlreadyLoginedAccountTask;
import hmhmmhm.ParfaitAuth.Tasks.UpdateAccountTask;
import mongodblib.MongoDBLib;

public class ParfaitAuth {
	public static String parfaitAuthCollectionName = "hmhmmhm.ParfaitAuth";
	public static String accountCollectionName = "hmhmmhm.ParfaitAuth.Account";

	final static public int DATABASE_VERSION = 1;

	final static public int CLIENT_IS_DEAD = 0;
	final static public int SUCCESS = 1;
	final static public int ALREADY_EXIST_ACCOUNT = 2;
	final static public int NOT_EXIST_ACCOUNT = 3;
	final static public int USER_DATA_NOT_EXIST = 4;

	final static public int ALREADY_INITIALIZED_DATABASE = 5;
	final static public int UPDATED_DATABASE = 6;
	final static public int CAUTION_PLUGIN_IS_OUTDATE = 7;

	final static public int SERVERSTATE_IS_NULL = 8;
	final static public int SERVERSTATE_IS_GREEN = 9;
	final static public int SERVERSTATE_IS_RED = 10;

	public static LinkedHashMap<UUID, Player> unauthorised = new LinkedHashMap<UUID, Player>();
	public static LinkedHashMap<UUID, Account> authorisedUUID = new LinkedHashMap<UUID, Account>();
	public static LinkedHashMap<UUID, Account> authorisedID = new LinkedHashMap<UUID, Account>();

	/* unauthorized_1141 과 같은 닉네임 생성시 사용되는 인덱스 입니다. */
	public static int unauthorizedUserCount = 0;

	/**
	 * 유저의 UUID를 검색해서 유저 계정자료를 얻어옵니다.
	 * 
	 * @param uuid
	 * @return Account | null
	 */
	public static Account getAccount(UUID uuid) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return null;

		MongoDatabase db = MongoDBLib.getDatabase();
		List<Document> documents = db.getCollection(ParfaitAuth.accountCollectionName)
				.find(new Document("uuid", uuid.toString())).into(new ArrayList<Document>());

		// 계정의 uuid는 겹칠 수 없기에 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document accountDocument = (documents.size() == 0) ? null : documents.get(0);

		if (accountDocument == null)
			return null;

		return new Account(accountDocument);
	}

	/**
	 * 유저의 닉네임을 검색해서 유저 계정자료를 얻어옵니다.
	 * 
	 * @param nickname
	 * @return Account | null
	 */
	public static Account getAccountByNickName(String nickname) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return null;

		MongoDatabase db = MongoDBLib.getDatabase();
		List<Document> documents = db.getCollection(ParfaitAuth.accountCollectionName)
				.find(new Document("nickname", nickname)).into(new ArrayList<Document>());

		// 닉네임은 겹치지 않게 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document accountDocument = (documents.size() == 0) ? null : documents.get(0);

		if (accountDocument == null)
			return null;

		return new Account(accountDocument);
	}

	/**
	 * 유저의 아이디를 검색해서 유저 계정자료를 얻어옵니다.
	 * 
	 * @param nickname
	 * @return Account | null
	 */
	public static Account getAccountById(String id) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return null;

		MongoDatabase db = MongoDBLib.getDatabase();
		List<Document> documents = db.getCollection(ParfaitAuth.accountCollectionName).find(new Document("id", id))
				.into(new ArrayList<Document>());

		// 닉네임은 겹치지 않게 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document accountDocument = (documents.size() == 0) ? null : documents.get(0);

		if (accountDocument == null)
			return null;

		return new Account(accountDocument);
	}

	/**
	 * 계정을 생성합니다. 결과메시지가 반환되니 예외처리 하셔야합니다.<br>
	 * <br>
	 * 클라이언트가 오프라인상태일때 ParfaitAuth.CLIENT_IS_DEAD<br>
	 * 이미 가입된 계정이 존재할때 ParfaitAuth.ALREADY_EXIST_ACCOUNT<br>
	 * 자료를 성공적으로 넣었으면 ParfaitAuth.SUCCESS 이 반환됩니다.
	 * 
	 * @param uuid
	 * @param nickname
	 * @return integer
	 */
	public static int addAccount(UUID uuid, String nickname) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		// 계정자료가 이미 존재한다면 계정생성하지 않고 반환처리
		if (ParfaitAuth.getAccount(uuid) != null)
			return ParfaitAuth.ALREADY_EXIST_ACCOUNT;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.accountCollectionName);

		Account account = new Account();
		account.uuid = uuid;
		account.nickname = nickname;

		collection.insertOne(account.convertToDocument());
		return ParfaitAuth.SUCCESS;
	}

	/**
	 * 계정을 삭제합니다. 결과메시지가 반환되니 예외처리 하셔야합니다.<br>
	 * <br>
	 * 클라이언트가 오프라인상태일때 ParfaitAuth.CLIENT_IS_DEAD<br>
	 * 이미 가입된 계정이 없을때 ParfaitAuth.NOT_EXIST_ACCOUNT<br>
	 * 자료를 성공적으로 삭제했으면 ParfaitAuth.SUCCESS 이 반환됩니다.
	 * 
	 * @param uuid
	 * @return integer
	 */
	public static int deleteAccount(UUID uuid) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		// 계정자료가 이미 존재하지 않으면 계정삭제하지 않고 반환처리
		if (ParfaitAuth.getAccount(uuid) == null)
			return ParfaitAuth.NOT_EXIST_ACCOUNT;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.accountCollectionName);

		collection.deleteOne(new Document("uuid", uuid.toString()));
		return ParfaitAuth.SUCCESS;
	}

	/**
	 * 계정정보를 갱신합니다. 결과메시지가 반환되니 예외처리 하셔야합니다.<br>
	 * <br>
	 * 클라이언트가 오프라인상태일때 ParfaitAuth.CLIENT_IS_DEAD<br>
	 * 가입된 계정이 없을때 ParfaitAuth.USER_DATA_NOT_EXIST<br>
	 * 자료를 성공적으로 갱신했으면 ParfaitAuth.SUCCESS 이 반환됩니다.
	 * 
	 * @param uuid
	 * @param document
	 * @return int
	 */
	public static int updateAccount(UUID uuid, Document document) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.accountCollectionName);

		UpdateResult result = collection.updateOne(new Document("uuid", uuid.toString()),
				new Document("$set", document));
		return (result.getMatchedCount() == 1) ? ParfaitAuth.SUCCESS : ParfaitAuth.USER_DATA_NOT_EXIST;
	}

	/**
	 * _id정보로 계정정보를 갱신합니다. 결과메시지가 반환되니 예외처리 하셔야합니다.<br>
	 * <br>
	 * 클라이언트가 오프라인상태일때 ParfaitAuth.CLIENT_IS_DEAD<br>
	 * 가입된 계정이 없을때 ParfaitAuth.USER_DATA_NOT_EXIST<br>
	 * 자료를 성공적으로 갱신했으면 ParfaitAuth.SUCCESS 이 반환됩니다.
	 * 
	 * @param uuid
	 * @param document
	 * @return int
	 */
	public static int updateAccount_id(Object _id, Document document) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.accountCollectionName);

		UpdateResult result = collection.updateOne(new Document("_id", _id), new Document("$set", document));
		return (result.getMatchedCount() == 1) ? ParfaitAuth.SUCCESS : ParfaitAuth.USER_DATA_NOT_EXIST;
	}

	/**
	 * 계정정보를 비동기로 DB에 갱신합니다.<br>
	 * 연결상태가 양호할때만 쓰는게 좋습니다.
	 * 
	 * @param uuid
	 * @param document
	 */
	public static void updateAccountAsync(UUID uuid, Document document) {
		Server.getInstance().getScheduler().scheduleAsyncTask(new UpdateAccountTask(uuid, document));
	}

	/**
	 * 데이터베이스 기반정보를 데이터베이스에 입력합니다.<br>
	 * 결과메시지가 반환되니 예외처리 하셔야합니다.<br>
	 * <br>
	 * 클라이언트가 오프라인상태일때 ParfaitAuth.CLIENT_IS_DEAD<br>
	 * DB버전이 플러그인 DB버전보다 높을 때 ParfaitAuth.CAUTION_PLUGIN_IS_OUTDATE<br>
	 * DB 기반자료를 업데이트 했을 때 ParfaitAuth.UPDATED_DATABASE<br>
	 * 기반자료가 이미 존재하면 ParfaitAuth.SUCCESS 이 반환됩니다.
	 * 
	 * @return integer
	 */
	public static int initialDatabase() {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		List<Document> documents = collection.find(new Document("_id", "serverstate")).into(new ArrayList<Document>());
		Document serverstate = (documents.size() == 0) ? null : documents.get(0);

		documents = collection.find(new Document("_id", "nameindex")).into(new ArrayList<Document>());
		Document nameindex = (documents.size() == 0) ? null : documents.get(0);

		if (serverstate != null)
			if ((int) serverstate.get("initVersion") > ParfaitAuth.UPDATED_DATABASE)
				return ParfaitAuth.CAUTION_PLUGIN_IS_OUTDATE;

		List<Document> insertDocuments = new ArrayList<Document>();

		// serverstate
		if (serverstate == null || serverstate.get("initVersion") == null
				|| (int) serverstate.get("initVersion") < ParfaitAuth.DATABASE_VERSION) {
			Document serverstate_document = new Document();
			serverstate_document.put("_id", "serverstate");
			serverstate_document.put("initVersion", ParfaitAuth.DATABASE_VERSION);

			Long timestamp = Calendar.getInstance().getTime().getTime();
			serverstate_document.put("initTime", (new Timestamp(timestamp)).toString());
			serverstate_document.put("initTimestamp", String.valueOf(timestamp));
			insertDocuments.add(serverstate_document);

		}

		// nameindex
		if (nameindex == null) {
			Document nameindex_document = new Document();
			nameindex_document.put("_id", "nameindex");
			nameindex_document.put("unauth_index", 0);
			insertDocuments.add(nameindex_document);
		}

		// TODO 만약 나중에 DB에 내장해야할 자료가 생기면 여기에 추가하고 DB버전업

		if (insertDocuments.size() != 0)
			collection.insertMany(insertDocuments);

		if (serverstate != null)
			if ((int) serverstate.get("initVersion") < ParfaitAuth.DATABASE_VERSION)
				return ParfaitAuth.UPDATED_DATABASE;

		return ParfaitAuth.SUCCESS;
	}

	/**
	 * 서버의 UUID를 받아서 해당 서버와 DB의 연결상태를 확인합니다.<br>
	 * 결과메시지가 반환되니 예외처리 하셔야합니다.<br>
	 * <br>
	 * 클라이언트가 오프라인상태일때 ParfaitAuth.CLIENT_IS_DEAD<br>
	 * 서버상태 문서가 없을 때 ParfaitAuth.SERVERSTATE_IS_NULL<br>
	 * 서버상태가 양호하면 ParfaitAuth.SERVERSTATE_IS_GREEN<br>
	 * 서버상태가 오프라인이면 ParfaitAuth.SERVERSTATE_IS_RED 이 반환됩니다.
	 * 
	 * @param uuid
	 * @return integer
	 */
	public static int getServerStatus(UUID uuid) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getDatabase();
		List<Document> documents = db.getCollection(ParfaitAuth.parfaitAuthCollectionName)
				.find(new Document("_id", "serverstate")).into(new ArrayList<Document>());

		// _id는 겹칠 수 없기에 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document serverstate = (documents.size() == 0) ? null : documents.get(0);

		if (serverstate == null)
			return ParfaitAuth.SERVERSTATE_IS_NULL;

		long serverTimestamp = Long.valueOf(serverstate.get(uuid.toString()).toString());
		long currentTimestamp = Calendar.getInstance().getTime().getTime();
		long diff = TimeUnit.MILLISECONDS.toSeconds(currentTimestamp - serverTimestamp);

		// System.out.println("[DEBUG] show serverTimestamp:" +
		// serverTimestamp);
		// System.out.println("[DEBUG] show currentTimestamp:" +
		// currentTimestamp);
		// System.out.println("[DEBUG] show diff:" + (diff % 60));
		// 10초마다 서버핑을 보내게 처리하며
		// 20초이상 서버핑 업데이트가 이뤄지지 않았으면 사망처리합니다.
		// ( 즉 서버가 갑자기 크래시되도 10초안에 다른서버 접속이 허용됩니다. )
		return (diff >= 20) ? ParfaitAuth.SERVERSTATE_IS_GREEN : ParfaitAuth.SERVERSTATE_IS_RED;
	}

	public static int updateServerStatus(UUID uuid) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		String currentTimestamp = String.valueOf(Calendar.getInstance().getTime().getTime());
		UpdateResult result = collection.updateOne(new Document("_id", "serverstate"),
				new Document("$set", new Document(uuid.toString(), currentTimestamp)));
		return (result.getMatchedCount() == 1) ? ParfaitAuth.SUCCESS : ParfaitAuth.SERVERSTATE_IS_NULL;
	}

	/**
	 * 어떠한 유저연결이 들어오면 이 함수로 등록시킵니다.<br>
	 * 비인가연결된 유저의 거의 모든 이벤트는 차단처리되며<br>
	 * DB에서의 UUID계정처리 완료 또는 아이디계정 로그인이 되면<br>
	 * 자동으로 권한부여가 진행되고 비인가명단에서 삭제됩니다.
	 * 
	 * @param player
	 */
	public static void unauthorizedAccess(Player player) {
		if (player == null || !player.isConnected())
			return;

		player.sendMessage(ParfaitAuthPlugin.getPlugin().getMessage("status-start-get-account-data"));
		ParfaitAuth.unauthorised.put(player.getUniqueId(), player);

		Server.getInstance().getScheduler()
				.scheduleAsyncTask(new CheckUnauthorizedAccessTask(player.getUniqueId(), player.getName()));
	}

	/**
	 * ID와 PW로 ID계정 인증요청시 호출되는 함수입니다.<br>
	 * 자동으로 예외처리까지 진행하며 비동기처리로 진행됩니다.
	 * 
	 * @param player
	 * @param id
	 * @param pw
	 */
	public static void preAuthorizationID(Player player, String id, String pw, boolean pwCheckPassForce) {
		if (player == null || !player.isConnected())
			return;
		player.sendMessage(ParfaitAuthPlugin.getPlugin().getMessage("status-start-get-id-account-data"));

		Server.getInstance().getScheduler().scheduleAsyncTask(new CheckAuthorizationIDTask(player.getName(), id, pw,
				ParfaitAuth.getParfaitAuthUUID(), pwCheckPassForce));
	}

	/**
	 * 계정정보를 확인해서 ID인증 혹은 UUID인증을 거칩니다
	 * 
	 * @param player
	 * @param accountData
	 * @return
	 */
	public static boolean authorization(Player player, Account accountData) {
		if (accountData == null)
			return false;
		if (player == null || !player.isConnected())
			return false;

		if (accountData.id != null) {
			ParfaitAuth.authorizationID(player, accountData, false, false);
		} else {
			ParfaitAuth.authorizationUUID(player, accountData);
		}
		return true;
	}

	/**
	 * 인증정보가 ID계정으로 확인된 유저에게 권한부여를 진행합니다.<br>
	 * 비인가명단에서 삭제처리한 후 현재 계정에 대한 설명이 진행됩니다.
	 * 
	 * @param player
	 * @param accountData
	 */
	public static boolean authorizationID(Player player, Account accountData, boolean ipForce, boolean loginedForce) {
		// loginedForce의 용도는 통상적으로는
		// 인증서버의 오프라인 유무를 사전검사해서
		// 이전 인증서버가 사망시 로그인 여부를 따지지 않기 위해 쓰입니다.

		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		// 이전 인증서버가 살아있고 현재 접속중이며, 접속했던 서버가 이서버가 아닐때
		if (!loginedForce && !(accountData.logined == ParfaitAuth.getParfaitAuthUUID().toString())) {
			// accountData상 이미 로그인 중일 경우
			// 이 경우에 이전 서버가 크래시되었다면 10초 안에 복구될 것이고,
			// 단순히 자료전송이 늦는거면 5초 안에 복구될 것..

			player.sendMessage(plugin.getMessage("error-that-account-already-used-by-db"));
			player.sendMessage(plugin.getMessage("error-will-be-retry-in-3-seconds"));

			// 자동로그인 혹은 명령어 입력시 불편함을 덜하기 위해서
			// 자동반복 테스크로 3초마다 3번 재확인하도록 합니다.
			Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(
					new RetryAuthAlreadyLoginedAccountTask(player.getName(), accountData.id), 60, 60);
			return false;
		}

		// IP가 이전과 다른 경우
		if (!ipForce && ((accountData.lastIp != null) && (player.getAddress() != accountData.lastIp))) {
			player.sendMessage(plugin.getMessage("caution-account-founded-but-your-new-ip"));
			player.sendMessage(plugin.getMessage("caution-you-need-to-run-login-command"));
			player.sendMessage(plugin.getMessage("caution-requesting-for-temp-uuid-account"));

			// 이전계정의 UUID 정보말소
			accountData.uuid = null;
			Server.getInstance().getScheduler().scheduleAsyncTask(new CreateNewUUIDAccountTask(accountData));
			return false;
		}

		accountData.login(player);

		ParfaitAuth.unauthorised.remove(player.getUniqueId());
		ParfaitAuth.authorisedUUID.remove(player.getUniqueId());
		ParfaitAuth.authorisedID.put(player.getUniqueId(), accountData);

		if (!ipForce) {
			player.sendMessage(plugin.getMessage("success-id-account-was-logined"));
		} else {
			player.sendMessage(plugin.getMessage("success-id-account-was-auto-logined"));
			player.sendMessage(plugin.getMessage("info-you-can-use-logout-command"));
		}
		player.sendMessage(plugin.getMessage("info-you-can-use-auth-command"));

		return true;
	}

	/**
	 * 인증정보가 UUID계정으로 확인된 유저에게 권한부여를 진행합니다.<br>
	 * 비인가명단에서 삭제처리한 후 현재 계정에 대한 설명이 진행됩니다.
	 * 
	 * @param player
	 * @param accountData
	 */
	public static boolean authorizationUUID(Player player, Account accountData) {
		ParfaitAuth.unauthorised.remove(player.getUniqueId());
		ParfaitAuth.authorisedUUID.put(player.getUniqueId(), accountData);

		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();
		player.sendMessage(plugin.getMessage("success-uuid-account-login-complete"));
		player.sendMessage(plugin.getMessage("info-you-can-use-auth-command"));
		return true;
	}

	/**
	 * 유저연결이 끊겼을때 유저에 관한 모든 인증을 해제합니다.<br>
	 * 또한 유저의 데이터는 모두 DB로 전송합니다.<br>
	 * 
	 * @param uuid
	 * @param accountData
	 */
	public static void release(UUID uuid, Account accountData, boolean async, boolean logout) {
		ParfaitAuth.unauthorised.remove(uuid);
		ParfaitAuth.authorisedID.remove(uuid);
		ParfaitAuth.authorisedUUID.remove(uuid);

		// accountData 의 logined를 null 처리합니다.
		accountData.logout(logout);

		// 서버에 계정정보 업데이트
		if (async) {
			accountData.upload();
		} else {
			ParfaitAuth.updateAccount(uuid, accountData.convertToDocument());
		}
	}

	/**
	 * DB에서 랜덤하면서 겹치지 않는 닉네임을 가져옵니다.
	 * 
	 * @return String
	 */
	public static String getRandomName() {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return null;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		Document searchQuery = new Document("_id", "nameindex");
		Document increase = new Document("unauth_index", 1);
		Document updateQuery = new Document("$inc", increase);

		Document result = collection.findOneAndUpdate(searchQuery, updateQuery);
		int index = (int) result.get("unauth_index");

		return ParfaitAuth.getRandomNameLocal(index);
	}

	/**
	 * 방문번호에 따른 랜덤 닉네임을 가져옵니다.
	 * 
	 * @return String
	 */
	public static String getRandomNameLocal(int index) {
		Config randomName = ParfaitAuthPlugin.getPlugin().getRandomName();
		ArrayList names = (ArrayList) randomName.get("names");

		int size = names.size();
		int name_number = index / size;
		int name_index = index - (size * name_number);

		return "*" + names.get(name_index) + "_" + name_number;
	}

	/**
	 * 클라이언트가 온라인 상태인지 확인합니다.
	 * 
	 * @return Boolean
	 */
	public static boolean checkClientOnline() {
		try {
			return MongoDBLib.getClient().getAddress() != null ? true : false;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	/**
	 * 암호 해시 생성에 쓰이는 함수입니다.
	 * 
	 * @param String
	 * @return String
	 */
	public static String hash(String str) {
		String SHA = "";
		try {
			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			sh.update(str.getBytes());
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			SHA = sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			SHA = str;
		}
		return SHA;
	}

	public static UUID getParfaitAuthUUID() {
		return UUID.fromString((String) ParfaitAuthPlugin.getPlugin().getSettings().get("server-uuid"));
	}
}