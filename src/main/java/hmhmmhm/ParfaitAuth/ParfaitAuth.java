package hmhmmhm.ParfaitAuth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.Event;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import hmhmmhm.ParfaitAuth.Events.ChangedNameEvent;
import hmhmmhm.ParfaitAuth.Events.LoginEvent;
import hmhmmhm.ParfaitAuth.Events.LogoutEvent;
import hmhmmhm.ParfaitAuth.Events.NotificationReceiveEvent;
import hmhmmhm.ParfaitAuth.Tasks.CheckAuthorizationIDTask;
import hmhmmhm.ParfaitAuth.Tasks.CheckUnauthorizedAccessTask;
import hmhmmhm.ParfaitAuth.Tasks.CreateNewUUIDAccountTask;
import hmhmmhm.ParfaitAuth.Tasks.DeleteAccountTask;
import hmhmmhm.ParfaitAuth.Tasks.RetryAuthAlreadyLoginedAccountTask;
import hmhmmhm.ParfaitAuth.Tasks.UpdateAccountTask;

import mongodblib.MongoDBLib;
import static java.util.Arrays.asList;

import java.lang.Character.UnicodeBlock;
import java.lang.reflect.Field;

public class ParfaitAuth {
	/**
	 * 파르페오스의 기본세팅문서가 담길 DB 컬렉션 명입니다.<br>
	 * 다른 이름의 컬렉션을 사용하고 싶으면 이 변수를 변경합니다.
	 */
	public static String parfaitAuthCollectionName = "hmhmmhm.ParfaitAuth";
	/**
	 * 파르페오스의 유저문서가 담길 DB 컬렉션 명입니다.<br>
	 * 다른 이름의 컬렉션을 사용하고 싶으면 이 변수를 변경합니다.
	 */
	public static String accountCollectionName = "hmhmmhm.ParfaitAuth.Account";

	/**
	 * 플러그인이 사용중인 데이터베이스 버전명을 나타냅니다.<br>
	 * 출시 이후 DB에 큰 변동사항이 발생하면 이 값이 1씩 올라갑니다.
	 */
	final static public int DATABASE_VERSION = 1;

	/* 각 함수에서 반환용으로 사용되는 결과값들 입니다. */
	final static public int CLIENT_IS_DEAD = 0;
	final static public int SUCCESS = 1;
	final static public int ALREADY_EXIST_ACCOUNT = 2;
	final static public int NOT_EXIST_ACCOUNT = 3;
	final static public int USER_DATA_NOT_EXIST = 4;
	final static public int ALREADY_INITIALIZED_DATABASE = 5;
	final static public int UPDATED_DATABASE = 6;
	final static public int CAUTION_PLUGIN_IS_OUTDATE = 7;

	/* 해당 서버의 온라인 여부를 반환할때 쓰는 결과값들입니다. */
	final static public int SERVERSTATE_IS_NULL = 8;
	final static public int SERVERSTATE_IS_GREEN = 9;
	final static public int SERVERSTATE_IS_RED = 10;

	/* 어떤 인증도 아직 거치지 않은 유저들이 여기 기록됩니다. */
	public static LinkedHashMap<UUID, Player> unauthorised = new LinkedHashMap<>();
	/* UUID로만 인증을 받은 유저들이 여기 기록됩니다. */
	public static LinkedHashMap<UUID, Account> authorisedUUID = new LinkedHashMap<>();
	/* ID계정을 통해 인증 받은 유저들이 여기 기록됩니다. */
	public static LinkedHashMap<UUID, Account> authorisedID = new LinkedHashMap<>();

	/* 차단된 네트워크주소 명단이 여기 담깁니다. */
	public static LinkedHashMap<String, Long> bannedAddress = new LinkedHashMap<>();

	/* unauthorized_1141 과 같은 닉네임 생성시 사용되는 인덱스 입니다. */
	public static int unauthorizedUserCount = 0;

	/* 서버의 외부아이피:포트 정보가 여기에 담깁니다. */
	public static String externalAddress = null;

	/* 서버의 UUID(플러그인 최초기동시 캡쳐한 UUID)값이 여기에 담깁니다. */
	public static UUID parfaitAuthUUID = null;

	/* 서버의 랜덤닉네임 명단이 여기에 담깁니다. */
	public static Config randomName = null;

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
	 * 비동기로 계정을 삭제합니다.
	 * 
	 * @param uuid
	 */
	public static void deleteAccountAsync(UUID uuid) {
		Server.getInstance().getScheduler().scheduleAsyncTask(new DeleteAccountTask(uuid.toString()));
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

		// serverstate 문서를 가져옵니다.
		List<Document> documents = collection.find(new Document("_id", "serverstate")).into(new ArrayList<Document>());
		Document serverstate = (documents.size() == 0) ? null : documents.get(0);

		// nameindex 문서를 가져옵니다.
		documents = collection.find(new Document("_id", "nameindex")).into(new ArrayList<Document>());
		Document nameindex = (documents.size() == 0) ? null : documents.get(0);

		// DB버전이 플러그인에 프로그래밍 된 DB보다 더 높은지 확인합니다.
		// 더 높으면 어떤 작업도 실행하지 않고 바로 반환합니다.
		if (serverstate != null)
			if ((int) serverstate.get("initVersion") > ParfaitAuth.DATABASE_VERSION)
				return ParfaitAuth.CAUTION_PLUGIN_IS_OUTDATE;

		List<Document> insertDocuments = new ArrayList<Document>();

		// serverstate 문서를 추가합니다.
		// Insert serverstate document
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

		// nameindex 문서를 추가합니다.
		// Insert nameindex document
		if (nameindex == null) {
			Document nameindex_document = new Document();
			nameindex_document.put("_id", "nameindex");
			nameindex_document.put("unauth_index", 0);
			insertDocuments.add(nameindex_document);
		}

		// TODO 만약 나중에 DB에 내장해야할 자료가 추가로 생기면
		// 여기에 해당코드를 추가하고 DB버전을 업데이트 해야합니다.

		// DB에 업로드 해야할 문서가 존재하면 DB에 업로드합니다.
		if (insertDocuments.size() != 0)
			collection.insertMany(insertDocuments);

		// DB버전을 업데이트 했는지 여부를 반환합니다.
		if (serverstate != null) {
			// TODO 향후 DB버전이 개선되면 여기에 업데이트 코드를 적습니다.

			if ((int) serverstate.get("initVersion") < ParfaitAuth.DATABASE_VERSION)
				return ParfaitAuth.UPDATED_DATABASE;
		}

		return ParfaitAuth.SUCCESS;
	}

	/**
	 * 접근이 차단되어있는 네트워크주소 목록을 가져옵니다.
	 * 
	 * @param address
	 * @return
	 */
	public static LinkedHashMap<String, Long> getBannedAddress() {
		LinkedHashMap<String, Long> list = new LinkedHashMap<>();

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		// bannedaddress 문서를 가져옵니다.
		List<Document> documents = collection.find(new Document("_id", "bannedaddress"))
				.into(new ArrayList<Document>());
		Document bannedaddress = (documents.size() == 0) ? null : documents.get(0);

		if (bannedaddress == null)
			return null;

		// 키는 밴처리된 네트워크주소, 밸류는 밴해제할 타임스탬프
		for (Entry<String, Object> entry : bannedaddress.entrySet()) {
			Long value;
			// String 으로 저장된 타임스탬프를 Long으로 변환시도 합니다.
			try {
				value = Long.valueOf((String) entry.getValue());
			} catch (NumberFormatException e) {
				continue;
			}
			list.put(entry.getKey(), value);
		}

		return list;
	}

	/**
	 * 해당되는 네트워크주소를 차단목록에 추가합니다.
	 * 
	 * @param address
	 * @return
	 */
	public static int addBannedAddress(String address, Long period) {
		// bannedaddress 리스트를 가져옵니다.
		LinkedHashMap<String, Long> originList = ParfaitAuth.getBannedAddress();

		if (originList == null)
			originList = new LinkedHashMap<>();

		originList.put(address, period);

		// 업로드할 bannedaddress 문서를 만듭니다.
		Document document = new Document("_id", "bannedaddress");
		for (Entry<String, Long> entry : originList.entrySet())
			document.put(entry.getKey(), entry.getValue());

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		// 문서를 업로드합니다.
		UpdateResult result = collection.updateOne(new Document("_id", "bannedaddress"),
				new Document("$set", document));

		return result.getModifiedCount() == 0 ? ParfaitAuth.SERVERSTATE_IS_NULL : ParfaitAuth.SUCCESS;
	}

	/**
	 * 해당되는 네트워크주소를 차단목록에서 삭제합니다.
	 * 
	 * @param address
	 * @return
	 */
	public static int deleteBannedAddress(String address) {
		// bannedaddress 리스트를 가져옵니다.
		LinkedHashMap<String, Long> originList = ParfaitAuth.getBannedAddress();

		if (originList == null)
			originList = new LinkedHashMap<>();

		originList.remove(address);

		// 업로드할 bannedaddress 문서를 만듭니다.
		Document document = new Document("_id", "bannedaddress");
		for (Entry<String, Long> entry : originList.entrySet())
			document.put(entry.getKey(), entry.getValue());

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		// 문서를 업로드합니다.
		UpdateResult result = collection.updateOne(new Document("_id", "bannedaddress"),
				new Document("$set", document));

		return result.getModifiedCount() == 0 ? ParfaitAuth.SERVERSTATE_IS_NULL : ParfaitAuth.SUCCESS;
	}

	/**
	 * 자료를 해당 UUID를 가진 서버에 전송합니다.<br>
	 * 식별자를 통해서 자료전달자간 서로 식별해야합니다.
	 * 
	 * @param serverDocument
	 * @param serverUUID
	 * @param identifier
	 * @param object
	 * @return
	 */
	public static int pushNotification(Document serverDocument, String serverUUID, String identifier, Object object) {
		String updated = (String) serverDocument.get("updated");

		// 해당 타임스탬프가 존재하지 않는다면 반환합니다.
		if (updated == null)
			return ParfaitAuth.SERVERSTATE_IS_NULL;

		// 서버핑 갱신일자가 10초내가 아니면 오프라인으로 간주하고 반환처리합니다.
		long timestamp = Long.valueOf(updated);
		if (!ParfaitAuth.checkServerPingGreen(timestamp))
			return ParfaitAuth.SERVERSTATE_IS_RED;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		// 지정된 서버의 문서를 가져옵니다.
		List<Document> documents = collection.find(new Document("_id", serverUUID)).into(new ArrayList<Document>());
		Document serverstate = (documents.size() == 0) ? null : documents.get(0);

		// 서버상태문서가 없으면 반환합니다.
		if (serverstate == null)
			return ParfaitAuth.SERVERSTATE_IS_NULL;

		// 인덱스에 맞춰서 push-*로 문서 넣고 인덱스를 올리고 업로드합니다.
		int index = (int) serverstate.get("index");
		serverstate.put("push-" + index, new Document().append("key", identifier).append("value", object));
		serverstate.put("index", ++index);

		UpdateResult result = collection.updateOne(new Document("_id", serverUUID), new Document("$set", serverstate));
		return (result.getMatchedCount() == 1) ? ParfaitAuth.SUCCESS : ParfaitAuth.SERVERSTATE_IS_NULL;
	}

	public static ArrayList<Event> pullNotification() {
		UUID serverUUID = ParfaitAuth.getParfaitAuthUUID();

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		// 이 서버의 서버상태문서를 가져옵니다.
		List<Document> documents = collection.find(new Document("_id", serverUUID.toString()))
				.into(new ArrayList<Document>());
		Document serverstate = (documents.size() == 0) ? null : documents.get(0);

		// 서버상태문서가 없으면 반환합니다.
		if (serverstate == null)
			return null;

		int index = (int) serverstate.get("index");

		// 인덱스가 0이면 들어온 문서가 없는 것으로 간주해서 반환합니다.
		if (index == 0)
			return null;

		// 반환할 이벤트를 모으는 리스트입니다.
		ArrayList<Event> events = new ArrayList<Event>();

		for (int i = 0; i <= (index - 1); i++) {
			// push-인덱스값 의 형태로 문서들을 읽어옵니다.
			Object value = serverstate.get("push-" + i);

			// 저장된 값이 문서 아닐 경우 다음으로 넘어갑니다.
			if (value == null || !(value instanceof Document))
				continue;

			Document document = (Document) value;
			String key = (String) document.get("key");
			String json = (String) document.get("value");
			Object object = JSON.parse(json);

			// 읽어온 문서가 정상적이면 이벤트에 추가합니다.
			if (key != null && json != null)
				events.add(new NotificationReceiveEvent(key, object));

			// 읽어온 문서는 삭제처리합니다.
			serverstate.remove("push-" + i);
		}

		// 들어온 모든 정보를 읽어왔으므로 인덱스를 0으로 만듭니다.
		serverstate.put("index", 0);

		// 읽어온 정보가 있으면 변경된값을 DB에 업로드합니다.
		if (events.size() != 0)
			collection.replaceOne(new Document("_id", serverUUID.toString()), serverstate);

		return events;
	}

	/**
	 * DB에 있는 서버상태문서를 UUID를 이용해 찾습니다.
	 * 
	 * @param uuid
	 * @return
	 */
	public static Document getServerDocument(String uuid) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환합니다.
		if (!ParfaitAuth.checkClientOnline())
			return null;

		// 해당서버의 서버상태문서를 가져옵니다.
		MongoDatabase db = MongoDBLib.getDatabase();
		List<Document> documents = db.getCollection(ParfaitAuth.parfaitAuthCollectionName)
				.find(new Document("_id", uuid)).into(new ArrayList<Document>());

		return (documents.size() == 0) ? null : documents.get(0);
	}

	/**
	 * DB에 있는 서버상태문서들을 가져옵니다.
	 * 
	 * @return ArrayList
	 */
	public static LinkedHashMap<String, Document> getAllServers() {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환합니다.
		if (!ParfaitAuth.checkClientOnline())
			return null;

		MongoDatabase db = MongoDBLib.getDatabase();
		// 해당 컬렉션을 모두 읽어옵니다.
		List<Document> documents = db.getCollection(ParfaitAuth.parfaitAuthCollectionName).find()
				.into(new ArrayList<Document>());

		// 서버상태문서만 분별해서 맵에 추가 후 반환합니다.
		LinkedHashMap<String, Document> foundedDocuments = new LinkedHashMap<String, Document>();
		for (Document document : documents) {
			if (document.get("updated") == null || document.get("index") == null)
				continue;
			foundedDocuments.put((String) document.get("_id"), document);
		}
		return foundedDocuments;
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
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환합니다.
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		// 해당서버의 서버상태문서를 가져옵니다.
		MongoDatabase db = MongoDBLib.getDatabase();
		List<Document> documents = db.getCollection(ParfaitAuth.parfaitAuthCollectionName)
				.find(new Document("_id", uuid.toString())).into(new ArrayList<Document>());

		// _id는 겹칠 수 없기에 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document serverstate = (documents.size() == 0) ? null : documents.get(0);

		// 갱신일자 정보가 없으면 서버문서가 없는 것으로 간주합니다.
		if (serverstate == null || serverstate.get("updated") == null)
			return ParfaitAuth.SERVERSTATE_IS_NULL;

		// 갱신일자 정보를 타임스탬프로 읽어옵니다.
		long serverTimestamp = Long.valueOf((String) serverstate.get("updated"));

		// 10초마다 서버핑을 보내게 처리하며
		// 20초이상 서버핑 업데이트가 이뤄지지 않았으면 사망처리합니다.
		// ( 즉 서버가 갑자기 크래시되도 10초안에 다른서버 접속이 허용됩니다. )
		return (ParfaitAuth.checkServerPingGreen(serverTimestamp)) ? ParfaitAuth.SERVERSTATE_IS_GREEN
				: ParfaitAuth.SERVERSTATE_IS_RED;
	}

	/**
	 * DB에 저장되어있는 계정 유형 통계를 표시합니다.
	 * 
	 * @return List<Document>
	 */
	public static List<Document> getAccountStatistics() {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환합니다.
		if (!ParfaitAuth.checkClientOnline())
			return null;

		MongoDatabase db = MongoDBLib.getDatabase();

		// accountType 키의 밸류값이 어떤게 있는지에 대한 통계를 가져옵니다.
		List<Document> documents = db.getCollection(ParfaitAuth.accountCollectionName)
				.aggregate(asList(new Document("$group",
						new Document("accountType", "$borough").append("count", new Document("$sum", 1)))))
				.into(new ArrayList<Document>());
		return documents;
	}

	/**
	 * 서버의 타임스탬프 핑을 현재와 비교해서 20초가 지났는지 확인합니다.
	 * 
	 * @param serverTimestamp
	 * @return boolean
	 */
	public static boolean checkServerPingGreen(long serverTimestamp) {
		long currentTimestamp = Calendar.getInstance().getTime().getTime();
		long diff = TimeUnit.MILLISECONDS.toSeconds(currentTimestamp - serverTimestamp);
		return (diff >= 20) ? false : true;
	}

	/**
	 * 서버상태문서에 10초마다 타임스탬프를 갱신합니다.
	 * 
	 * @param uuid
	 * @return
	 */
	public static int updateServerStatus(UUID uuid) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환합니다.
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getDatabase();
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		// 현재시간을 타임스탬프로 만든 후 문자열로 저장합니다.
		String currentTimestamp = String.valueOf(Calendar.getInstance().getTime().getTime());

		// 서버상태문서를 가져옵니다.
		ArrayList<Document> documents = collection.find(new Document("_id", uuid.toString()))
				.into(new ArrayList<Document>());
		Document serverstate = (documents.size() == 0) ? null : documents.get(0);

		if (serverstate == null || !(serverstate instanceof Document)) {
			// 서버상태문서 없으면 생성합니다.
			serverstate = new Document("_id", uuid.toString()).append("updated", currentTimestamp).append("index", 0);
			collection.insertOne(serverstate);
			return ParfaitAuth.SUCCESS;
		} else {
			// 서버상태문서 타임스탬프를 갱신처리 합니다.
			serverstate.put("updated", currentTimestamp);

			// 갱신되는 서버가 이서버이고 외부네트워크주소가 존재하면 서버에 업로드
			if (ParfaitAuth.getParfaitAuthUUID().toString().equals(uuid.toString()))
				if (ParfaitAuth.externalAddress != null)
					serverstate.put("externalAddress", ParfaitAuth.externalAddress);

			UpdateResult result = collection.updateOne(new Document("_id", uuid.toString()),
					new Document("$set", serverstate));

			return (result.getMatchedCount() == 1) ? ParfaitAuth.SUCCESS : ParfaitAuth.SERVERSTATE_IS_NULL;
		}
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
		// 비인가자 명단에 추가합니다.
		ParfaitAuth.unauthorised.put(player.getUniqueId(), player);

		// 비동기로 DB에서 정보를 가져온 후 처리합니다.
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

		// 아이디와 암호가 일치하는지 확인합니다.
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
		// 계정정보가 없거나 유저가 접속중이 아니면 반환합니다.
		if (accountData == null)
			return false;
		if (player == null || !player.isConnected())
			return false;

		if (accountData.id != null) {
			// ID정보가 존재하면 ID계정으로 인증합니다.
			ParfaitAuth.authorizationID(player, accountData, false, false);
		} else {
			// ID정보가 없으면 UUID계정으로 인증합니다.
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
			// 단순히 자료전송이 늦는거면 5초 안에 복구됩니다.

			player.sendMessage(plugin.getMessage("error-that-account-already-used-by-db"));
			player.sendMessage(plugin.getMessage("error-will-be-retry-in-3-seconds"));

			// 자동로그인 혹은 명령어 입력시 불편함을 덜하기 위해서
			// 자동반복 테스크로 3초마다 3번 재확인하도록 합니다.
			Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(
					new RetryAuthAlreadyLoginedAccountTask(player.getName(), accountData.id), 60, 60);
			return false;
		}

		// IP가 이전과 다른 경우 재로그인 유도
		if (!ipForce && ((accountData.lastIp != null) && (player.getAddress() != accountData.lastIp))) {
			player.sendMessage(plugin.getMessage("caution-account-founded-but-your-new-ip"));
			player.sendMessage(plugin.getMessage("caution-you-need-to-run-login-command"));
			player.sendMessage(plugin.getMessage("caution-requesting-for-temp-uuid-account"));

			// 이전계정의 UUID 정보말소
			accountData.uuid = null;
			Server.getInstance().getScheduler().scheduleAsyncTask(new CreateNewUUIDAccountTask(accountData));
			return false;
		}

		LoginEvent loginEvent = new LoginEvent(player, accountData);
		Server.getInstance().getPluginManager().callEvent(loginEvent);

		if (loginEvent.isCancelled()) {
			if (loginEvent.reason != null) {
				player.sendMessage(loginEvent.reason);
			} else {
				player.sendMessage(plugin.getMessage("error-login-is-cancelled-by-other-plugin"));
			}
			return true;
		}

		// ID계정으로 로그인 처리합니다.
		accountData.login(player);
		ParfaitAuth.unauthorised.remove(player.getUniqueId());
		ParfaitAuth.authorisedUUID.remove(player.getUniqueId());
		ParfaitAuth.authorisedID.put(player.getUniqueId(), accountData);

		// 닉네임을 계정자료에 있는 닉네임으로 변경합니다.
		changePlayerName(player, accountData.nickname);

		// IP자동로그인이 아니라면
		if (!ipForce) {
			// ID계정으로 로그인되었다고 알립니다.
			player.sendMessage(plugin.getMessage("success-id-account-was-logined"));
		} else {
			// 자동로그인 되었다고 알립니다.
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
		// 비인가자/ID인가자 명단에서 제거합니다.
		ParfaitAuth.unauthorised.remove(player.getUniqueId());
		ParfaitAuth.authorisedUUID.put(player.getUniqueId(), accountData);

		changePlayerName(player, accountData.nickname);

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
		Server.getInstance().getPluginManager().callEvent(new LogoutEvent(uuid, accountData));

		// 인가 비인가 명단에서 모두 제외합니다.
		ParfaitAuth.unauthorised.remove(uuid);
		ParfaitAuth.authorisedID.remove(uuid);
		ParfaitAuth.authorisedUUID.remove(uuid);

		// 로그아웃 처리합니다.
		accountData.logout(logout);

		// DB에 계정정보를 업로드합니다.
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

		// 랜덤닉네임 인덱스를 가져오고 즉시 값을 1 추가해 올립니다.
		Document searchQuery = new Document("_id", "nameindex");
		Document increase = new Document("unauth_index", 1);
		Document updateQuery = new Document("$inc", increase);
		Document result = collection.findOneAndUpdate(searchQuery, updateQuery);
		int index = (int) result.get("unauth_index");

		// 가져온 랜던닉네임 생성횟수를 토대로 닉네임을 생성합니다.
		return ParfaitAuth.getRandomNameLocal(index);
	}

	/**
	 * 랜덤닉네임 생성횟수에 따른 랜덤닉네임을 가져옵니다.
	 * 
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public static String getRandomNameLocal(int index) {
		// 서버에 저장된 랜덤닉네임 명단을 가져옵니다. (*저장된 JSON으로 커스텀가능)
		// 랜덤닉네임 명단을 임의로 변경할 시엔 모든서버에 변경한 명단을 적용해야합니다.
		// (특정서버만 꼭 다르게 사용해야한다면 다른서버와 명단이 절대 겹치지 않게 해야합니다.)
		ArrayList<String> names = (ArrayList<String>) randomName.get("names");

		// 생성횟수를 저장된닉네임수로 나눈 몫을 이용해 닉네임을
		// 정하고 나머지값을 숫자로 나타냅니다.
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
			// 클라이언트의 주소값을 가져올 수 있는지 확인합니다.
			return MongoDBLib.getClient().getAddress() != null ? true : false;
		} catch (IllegalStateException e) {
			// 아예 실패하면 false를 반환합니다.
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

	/**
	 * 누킷서버의 기본UUID는 켜고 끌때마다 바뀌므로 식별자로 사용불가능하고,<br>
	 * 파르페오스는 별도로 최초동작시의 서버 UUID를 캡쳐합니다.
	 * 
	 * @return
	 */
	public static UUID getParfaitAuthUUID() {
		return ParfaitAuth.parfaitAuthUUID;
	}

	/**
	 * 해당 문자열을 아이디로 사용가능한지 확인합니다.
	 * 
	 * @param id
	 * @return boolean
	 */
	public static boolean checkRightId(String id) {
		int len = id.length();

		if (len > 16 || len < 3)
			return false;

		for (int i = 0; i < len; i++) {
			char c = id.charAt(i);
			// English and under bar allow
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')
				continue;
			return false;
		}
		return true;
	}

	/**
	 * 해당 문자열을 닉네임으로 사용가능한지 확인합니다.
	 * 
	 * @param id
	 * @return boolean
	 */
	public static boolean checkRightName(String name) {
		int len = name.length();

		if (len > 10 || len < 2)
			return false;

		for (int i = 0; i < len; i++) {
			char c = name.charAt(i);
			// English and under bar allow
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')
				continue;

			if (ParfaitAuth.checkLangMatch(Server.getInstance().getLanguage().getLang(), c))
				continue;

			return false;
		}
		return true;
	}

	/**
	 * 해당 문자열을 닉네임으로 사용가능한지 확인합니다.
	 * 
	 * @param id
	 * @return boolean
	 */
	public static boolean checkRightPassword(String pw) {
		int len = pw.length();

		if (len > 40 || len < 6)
			return false;
		return true;
	}

	/**
	 * 해당 닉네임의 복잡도를 반환합니다.<br>
	 * 차단처리 하기 힘든 닉네임 검색시 사용됩니다.
	 * 
	 * @param name
	 * @return
	 */
	public static int getNickNameDifficult(String name) {
		int count = 0;
		int len = name.length();

		if (len > 10)
			count += (len - 10);

		for (int i = 0; i < len; i++) {
			char c = name.charAt(i);
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')) {
				if (ParfaitAuth.checkLangMatch(Server.getInstance().getLanguage().getLang(), c))
					continue;
				count++;
			}
		}
		return count;
	}

	/**
	 * 해당서버의 언어권에 맞는 언어로 된 글자를 허용합니다.
	 * 
	 * @param language
	 * @param ch
	 * @return
	 */
	public static boolean checkLangMatch(String language, char ch) {
		UnicodeBlock block = UnicodeBlock.of(ch);

		switch (language) {
		case "kor":
			if (UnicodeBlock.HANGUL_SYLLABLES == block || UnicodeBlock.HANGUL_JAMO == block
					|| UnicodeBlock.HANGUL_COMPATIBILITY_JAMO == block)
				return true;

			// IF OTHER COUNTRY DEVELOPER NEED SOME USE
			// NATIONAL LANGUAGE NICKNAME IN THEIR SERVER
			// PASTE HERE ABOUT NATIONAL LANGUAGE CHECK CODE
			// YOU MIGHT BE USE 'UnicodeBlock' CLASS
		}

		return false;
	}

	public static boolean changePlayerName(Player player, String toName) {
		String oldName = player.getName();

		setProtectedValue(player, "username", TextFormat.clean(toName));
		setProtectedValue(player, "displayName", toName);
		player.setNameTag(toName);
		setProtectedValue(player, "iusername", toName.toLowerCase());

		Server.getInstance().getPluginManager().callEvent(new ChangedNameEvent(player, oldName, toName));
		return true;
	}

	public static boolean setProtectedValue(Object object, String key, Object value) {
		Class<?> myClass = object.getClass();
		Field myField;
		try {
			myField = getField(myClass, key);
		} catch (NoSuchFieldException e) {
			return false;
		}
		myField.setAccessible(true);
		try {
			myField.set(object, value);
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IllegalAccessException e) {
			return false;
		}
		return true;
	}

	public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}
}