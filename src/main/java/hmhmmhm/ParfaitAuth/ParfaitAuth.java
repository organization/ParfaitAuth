package hmhmmhm.ParfaitAuth;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import mongodblib.MongoDBLib;
import mongodblib.MongoDBLibPlugin;

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

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		FindIterable<Document> iterable = db.getCollection(ParfaitAuth.accountCollectionName)
				.find(new Document("_id", uuid.toString()));

		// _id는 겹칠 수 없기에 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document accountDocument = iterable.first();

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

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		FindIterable<Document> iterable = db.getCollection(ParfaitAuth.accountCollectionName)
				.find(new Document("nickname", nickname));

		// 닉네임은 겹치지 않게 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document accountDocument = iterable.first();

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

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
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

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.accountCollectionName);

		collection.deleteOne(new Document("_id", uuid.toString()));
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

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.accountCollectionName);

		UpdateResult result = collection.updateOne(new Document("_id", uuid.toString()), document);
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

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		FindIterable<Document> iterable = collection.find(new Document("_id", "serverstate"));
		Document serverstate = iterable.first();

		if ((int) serverstate.get("initVersion") > ParfaitAuth.UPDATED_DATABASE)
			return ParfaitAuth.CAUTION_PLUGIN_IS_OUTDATE;

		if (serverstate == null || serverstate.get("initVersion") == null
				|| (int) serverstate.get("initVersion") < ParfaitAuth.UPDATED_DATABASE) {
			Document document = new Document();
			document.put("_id", "serverstate");
			document.put("initVersion", ParfaitAuth.DATABASE_VERSION);

			Long timestamp = Calendar.getInstance().getTime().getTime();
			document.put("initTime", (new Timestamp(timestamp)).toString());
			document.put("initTimestamp", String.valueOf(timestamp));

			// TODO 만약 나중에 DB에 내장해야할 자료가 생기면 여기에 추가하고 DB버전업
			collection.insertOne(document);
		}

		if ((int) serverstate.get("initVersion") < ParfaitAuth.UPDATED_DATABASE)
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

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		FindIterable<Document> iterable = db.getCollection(ParfaitAuth.parfaitAuthCollectionName)
				.find(new Document("_id", "serverstate"));

		// _id는 겹칠 수 없기에 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document serverstate = iterable.first();

		if (serverstate == null)
			return ParfaitAuth.SERVERSTATE_IS_NULL;

		int serverTimestamp = (int) serverstate.get(uuid.toString());
		int currentTimestamp = (int) Calendar.getInstance().getTime().getTime();
		int diff = serverTimestamp - currentTimestamp;

		// System.out.println("[DEBUG] show diff:" + (diff % 60) + "\r\n");
		// 10초마다 서버핑을 보내게 처리하며
		// 20초이상 서버핑 업데이트가 이뤄지지 않았으면 사망처리합니다.
		// ( 즉 서버가 갑자기 크래시되도 10초안에 다른서버 접속이 허용됩니다. )
		return ((diff % 60) >= 20) ? ParfaitAuth.SERVERSTATE_IS_GREEN : ParfaitAuth.SERVERSTATE_IS_RED;
	}

	public static int updateServerStatus(UUID uuid) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		// _id는 겹칠 수 없기에 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		FindIterable<Document> iterable = collection.find(new Document("_id", "serverstate"));
		Document serverstate = iterable.first();

		if (serverstate == null)
			return ParfaitAuth.SERVERSTATE_IS_NULL;

		int currentTimestamp = (int) Calendar.getInstance().getTime().getTime();
		serverstate.replace(uuid.toString(), currentTimestamp);

		UpdateResult result = collection.updateOne(new Document("_id", "serverstate"), serverstate);
		return (result.getMatchedCount() == 1) ? ParfaitAuth.SUCCESS : ParfaitAuth.SERVERSTATE_IS_NULL;
	}

	/**
	 * 클라이언트가 온라인 상태인지 확인합니다.
	 * 
	 * @return Boolean
	 */
	public static boolean checkClientOnline() {
		return MongoDBLib.getClient() != null ? true : false;
	}
}