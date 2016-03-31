package hmhmmhm.ParfaitAuth;

import java.util.UUID;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import mongodblib.MongoDBLib;
import mongodblib.MongoDBLibPlugin;

public class ParfaitAuth {
	public static String parfaitAuthCollectionName = "hmhmmhm.ParfaitAuth";
	public static String accountCollectionName = "hmhmmhm.ParfaitAuth.Account";

	/**
	 * 유저의 UUID를 검색해서 유저 계정자료를 얻어옵니다.
	 * 
	 * @param uuid
	 * @return Account | null
	 */
	public static Account getAccount(UUID uuid) {
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
		MongoDatabase db = MongoDBLib.getClient().getDatabase(MongoDBLibPlugin.getPlugin().getDBLibConfig().dbName);
		FindIterable<Document> iterable = db.getCollection(ParfaitAuth.accountCollectionName)
				.find(new Document("nickname", nickname));

		// 닉네임은 겹치지 않게 반복문 돌지않고 즉시 첫째값만 꺼냅니다.
		Document accountDocument = iterable.first();

		if (accountDocument == null)
			return null;

		return new Account(accountDocument);
	}

	public static String addAccount(UUID uuid) {
		String success = "";
		// TODO

		return success;
	}

	public static String deleteAccount() {
		String success = "";
		// TODO

		return success;
	}

	public static String updateAccount(Document json) {
		String success = "";
		// TODO

		return success;
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