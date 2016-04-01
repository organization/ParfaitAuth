package hmhmmhm.ParfaitAuth;

import java.sql.Date;
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

	public static int updateAccount(UUID uuid, Document document) {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.accountCollectionName);

		UpdateResult result = collection.updateOne(new Document("_id", uuid.toString()), document);
		return (result.getMatchedCount() == 1) ? ParfaitAuth.SUCCESS : ParfaitAuth.USER_DATA_NOT_EXIST;
	}

	public static int initialDatabase() {
		// 클라이언트가 오프라인상태일때 작업하지 않고 반환
		if (!ParfaitAuth.checkClientOnline())
			return ParfaitAuth.CLIENT_IS_DEAD;

		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		MongoCollection<Document> collection = db.getCollection(ParfaitAuth.parfaitAuthCollectionName);

		FindIterable<Document> iterable = collection.find(new Document("_id", "updateinfo"));
		Document updateinfo = iterable.first();

		Calendar calendar = Calendar.getInstance();
		Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());

		if (updateinfo == null) {
			// install database
		} else {
			updateinfo.get("");
		}
		return ParfaitAuth.SUCCESS;
	}

	public static String getServerStatus(UUID uuid) {
		String success = "";
		// TODO

		return success;
	}

	public static String updateServerStatus(UUID uuid) {
		String success = "";
		// TODO

		return success;
	}

	/**
	 * 클라이언트가 온라인 상태인지 확인
	 * 
	 * @return Boolean
	 */
	public static boolean checkClientOnline() {
		return MongoDBLib.getClient() != null ? true : false;
	}
}