package hmhmmhm.ParfaitAuth;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.bson.Document;

public class Account {
	/* 계정 필수 정보들을 별도로 변수로 보관합니다. */
	public UUID uuid;
	public String id;
	public String password;
	public String lastIp;
	public String lastDate;
	public String nickname;
	public String logined;

	/* 계정 Document 에 남은 추가적인 정보들을 보관합니다. */
	public LinkedHashMap<String, Object> additionalData = new LinkedHashMap<String, Object>();

	public Account() {
	}

	public Account(Document document) {
		String uuidString = (String) document.get("uuid");

		this.uuid = UUID.fromString(uuidString);
		this.id = (String) document.get("id");
		this.password = (String) document.get("password");
		this.lastIp = (String) document.get("lastIp");
		this.lastDate = (String) document.get("id");
		this.nickname = (String) document.get("nickname");
		this.logined = (String) document.get("logined");
	}

	/**
	 * 계정정보를 BSON 문서로 반환합니다.
	 * @return Document
	 */
	public Document convertToDocument() {
		Document document = new Document();

		document.put("uuid", this.uuid);
		document.put("id", this.id);
		document.put("password", this.password);
		document.put("lastIp", this.lastIp);
		document.put("lastDate", this.lastDate);
		document.put("nickname", this.nickname);
		document.put("logined", this.logined);
		document.put("additionalData", this.additionalData);

		return document;
	}
}