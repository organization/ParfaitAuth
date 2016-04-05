package hmhmmhm.ParfaitAuth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
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
	public static LinkedHashMap<UUID, Player> authorisedUUID = new LinkedHashMap<UUID, Player>();
	public static LinkedHashMap<UUID, Player> authorisedID = new LinkedHashMap<UUID, Player>();

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
		long diff = currentTimestamp - serverTimestamp;

		// System.out.println("[DEBUG] show serverTimestamp:" +
		// serverTimestamp);
		// System.out.println("[DEBUG] show currentTimestamp:" +
		// currentTimestamp);
		// System.out.println("[DEBUG] show diff:" + (diff % 60));
		// 10초마다 서버핑을 보내게 처리하며
		// 20초이상 서버핑 업데이트가 이뤄지지 않았으면 사망처리합니다.
		// ( 즉 서버가 갑자기 크래시되도 10초안에 다른서버 접속이 허용됩니다. )
		return ((diff % 60) >= 20) ? ParfaitAuth.SERVERSTATE_IS_GREEN : ParfaitAuth.SERVERSTATE_IS_RED;
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
		player.sendMessage(ParfaitAuthPlugin.getPlugin().getMessage("status-start-get-account-data"));
		ParfaitAuth.unauthorised.put(player.getUniqueId(), player);

		// 아래 싹 다 비동기로 처리
		// CheckUnauthorizedAccessTask
		// TODO UUID로 계정정보를 찾습니다.
		// TODO ID가 포함된 정보일 경우 authorizationID를 돌리고,
		// TODO ID가 미포함인 정보일 경우 authorizationUUID를 돌립니다.

		// TODO 계정정보가 존재하지 않으면 계정을 생성합니다.
		// CreateNewUUIDAccountTask
		// TODO 임시 UUID 계정이 생성 되었습니다!
		// TODO 반환된 정보로 authorizationUUID를 돌립니다.
	}

	/**
	 * ID와 PW로 ID계정 인증요청시 호출되는 함수입니다.<br>
	 * 자동으로 예외처리까지 진행하며 비동기처리로 진행됩니다.
	 * 
	 * @param player
	 * @param id
	 * @param pw
	 */
	public static void preAuthorizationID(Player player, String id, String pw) {
		// 아래 싹 다 비동기로 처리
		// CheckAuthorizationIDTask
		// TODO UUID로 현재 사용 중인 어카운트 정보를 받습니다.
		// TODO ID로 요청받은 어카운트 정보를 찾습니다.

		// 계정을 찾을 수 없거나 비밀번호가 틀린경우
		// TODO 어카운트 정보가 없으면 찾을 수 없다고 합니다.
		// TODO 어카운트 비밀번호 해시가 안 맞으면 비밀번호가 틀리다고 합니다.

		// 브루트포스가 5회 이상이면
		// TODO additionalData 의 auth_bruteforce 를 삭제처리합니다.
		// TODO 해당 유저를 킥처리합니다.
		// TODO 해당 아이피를 차단처리합니다.

		// 브루트포스가 5회 미만이면
		// TODO 해당 UUID계정의 additionalData에 'auth_bruteforce'를 +1한 후 계정정보
		// 업데이트합니다.
		// TODO 5번 이상 틀리면 30분간 서버접근이 차단된다는 경고를 띄웁니다.

		// 계정을 찾았고 비밀번호가 맞는 경우
		// TODO logined가 이 서버일 경우 이 서버가 크래시되었던 것으로 간주하고 로그인포스 true 처리.
		// TODO logined가 null나 false가 아닌 경우 해당 서버의 핑상태를 확인후 죽었으면 로그인포스를
		// true처리합니다.
		// TODO authorizationID 를 호출합니다.
	}

	/**
	 * 인증정보가 ID계정으로 확인된 유저에게 권한부여를 진행합니다.<br>
	 * 비인가명단에서 삭제처리한 후 현재 계정에 대한 설명이 진행됩니다.
	 * 
	 * @param player
	 * @param accountData
	 */
	public static boolean authorizationID(Player player, Account accountData, boolean ipForce, boolean loginedForce) {
		// loginedForce는 인증서버의 오프라인 유무를 사전검사한 것입니다.
		if (!loginedForce) {
			// accountData상 이미 로그인 중일 경우
			// 이 경우에 이전 서버가 크래시되었다면 10초 안에 복구될 것이고,
			// 단순히 자료전송이 늦는거면 5초 안에 복구될 것..

			// RetryAuthAlreadyLoginedAccountTask
			// TODO * 해당 계정정보는 현재 DB에서 정보가 사용되고 있습니다!
			// TODO * 5초 뒤에 DB에 인증요청을 다시 보냅니다...

			// TODO 3번 반복하고 여전하면 인증실패로 간주하고 재접속유도

			// TODO * 계정에 접근할 수 없습니다! 재접속 해주세요!
			// TODO * 계정이 타인에의해 접속되어있는 것일 수도 있습니다.
		}

		if (!ipForce) {
			// IP가 이전과 다른 경우
			// TODO * 아이디계정이 확인되었으나, 새 IP가 감지되었습니다.
			// TODO * /로그인 <아이디> <비밀번호> 를 진행하셔야합니다.
			// TODO * 인증전까지 사용될 임시 UUID 계정을 발급 요청중...

			// TODO 이전계정의 UUID 정보말소 후 업데이트처리
			// TODO CreateNewUUIDAccountTask(oldAccountData)
		}

		// lastIP 업데이트
		// logined 업데이트
		// lastDate 업데이트

		if (!ipForce) {
			// TODO * 아이디 계정으로 로그인 되었습니다.
		} else {
			// TODO * 아이디 계정으로 자동 로그인 되었습니다.
			// TODO * 해당 위치에서 더이상 사용을 원하지 않으면 /로그아웃 해주세요!
		}
		// TODO * /인증안내 /인증 을 통해 자세한 정보를 확인가능합니다.

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
		// TODO 임시 UUID 계정으로 자동 로그인 되었습니다.
		// TODO (/인증 입력을 통해 계정인증 명령어 설명을 확인가능합니다.)
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
		// 아래는 예외처리도 같이해야함

		// TODO accountData 의 logined를 null 처리합니다.

		if (logout) {
			// TODO accountData 의 lastIp를 null 처리
		}

		// TODO 서버에 계정정보 업데이트
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
}