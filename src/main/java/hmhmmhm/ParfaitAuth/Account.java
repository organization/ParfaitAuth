package hmhmmhm.ParfaitAuth;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import cn.nukkit.Player;
import cn.nukkit.Server;

public class Account {
	/* 계정 필수 정보들을 별도로 변수로 보관합니다. */

	public UUID uuid = null;
	public Object _id = null;
	public String id = null;
	public String password = null;
	public String lastIp = null;
	public String lastDate = null;
	public String nickname = null;
	public int accountType = 0;

	/* 계정 유형 목록 */
	public final static int TYPE_DEFAULT = 0; // 서버기본모드만 가능
	public final static int TYPE_GUEST = 1; // 어드벤쳐모드 가능
	public final static int TYPE_NORMAL = 2; // 생존모드 가능
	public final static int TYPE_BUILDER = 3; // 창조모드 가능
	public final static int TYPE_OVER_POWER = 4; // 밴처리가능
	public final static int TYPE_ADMIN = 5; // 모든권한가능

	/**
	 * banned is have the when he released.<br>
	 * banned 는 언제 차단을 풀지에 대한 정보를 갖습니다.
	 */
	public String banned = null;
	public String banCause = null;
	public String lastBanReleaseCause = null;

	/**
	 * 오진아웃제에 쓰이는 변수입니다.<br>
	 * 1씩 올리다가 5에 도달하면 밴처리합니다.
	 */
	public double fiveStrikes = (double) 0.0;

	/**
	 * logined is have the last logined server uuid.<br>
	 * logined 는 최근에 접속한 서버의 uuid를 갖습니다.
	 */
	public String logined = null;

	/**
	 * DB에 언제 유저정보를 보냈는지를 타임스탬프로 기록합니다.
	 */
	public long lastUploaded = 0;

	/**
	 * 전에 DB에 정보를 전송한 이후로 계정정보가 변경되었는지 확인합니다.
	 */
	public boolean isModified = false;

	/* 계정 Document 에 남은 추가적인 정보들을 보관합니다. */
	private LinkedHashMap<String, Object> additionalData = new LinkedHashMap<String, Object>();

	public Account() {
	}

	public Account(Document document) {
		String uuidString = (String) document.get("uuid");
		if (uuidString != null) {
			this.uuid = UUID.fromString(uuidString);
		} else {
			this.uuid = null;
		}

		this._id = document.get("_id");
		this.id = (String) document.get("id");
		this.password = (String) document.get("password");
		this.lastIp = (String) document.get("lastIp");
		this.lastDate = (String) document.get("id");
		this.nickname = (String) document.get("nickname");
		this.logined = (String) document.get("logined");
		this.banned = (String) document.get("banned");
		this.banCause = (String) document.get("banCause");
		this.accountType = (int) document.get("accountType");
		this.lastBanReleaseCause = (String) document.get("lastBanReleaseCause");

		for (Entry<String, Object> entry : document.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			switch (key) {
			case "_id":
			case "uuid":
			case "id":
			case "password":
			case "lastIp":
			case "lastDate":
			case "nickname":
			case "logined":
			case "banned":
			case "banCause":
			case "accountType":
			case "lastBanReleaseCause":
				continue;
			}
			additionalData.put(key, value);
		}
	}

	/**
	 * 계정정보를 BSON 문서로 반환합니다.
	 * 
	 * @return Document
	 */
	public Document convertToDocument() {
		Document document = new Document();

		if (this._id != null)
			document.put("_id", this._id);

		if (this.uuid != null) {
			document.put("uuid", this.uuid.toString());
		} else {
			document.put("uuid", null);
		}

		document.put("nickname", this.nickname);
		document.put("id", this.id);
		document.put("password", this.password);
		document.put("lastIp", this.lastIp);
		document.put("lastDate", this.lastDate);
		document.put("logined", this.logined);
		document.put("banned", this.banned);
		document.put("banCause", this.banCause);
		document.put("accountType", this.accountType);
		document.put("lastBanReleaseCause", this.lastBanReleaseCause);

		for (Entry<String, Object> entry : this.additionalData.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			document.put(key, value);
		}

		return document;
	}

	/**
	 * 로그인 했을때 갱신되어야할 정보들을<br>
	 * 함수호출시 자동으로 갱신처리 해줍니다.
	 * 
	 * @param player
	 */
	public void login(Player player) {
		this.setModified();
		this.uuid = player.getUniqueId();
		this.lastIp = player.getAddress();
		this.lastDate = String.valueOf(Calendar.getInstance().getTime().getTime());
		this.logined = ParfaitAuth.getParfaitAuthUUID().toString();
		this.upload();
	}

	/**
	 * 로그아웃 했을때 갱신되어야할 정보들을<br>
	 * 함수호출시 자동으로 갱신처리 해줍니다.
	 * 
	 * @param removeIpData
	 */
	public void logout(boolean removeIpData) {
		this.setModified();
		if (removeIpData)
			this.lastIp = null;
		this.logined = null;
	}

	/**
	 * 차단당한 상태인지 확인합니다.
	 * 
	 * @return
	 */
	public boolean isBanned() {
		if (this.banned == null || this.banned == "null")
			return false;

		Long nowTimestamp = Calendar.getInstance().getTime().getTime();
		Long releaseTimestamp = Long.getLong(this.banned);

		if (releaseTimestamp == null)
			return false;

		// 타임스탬프는 1970년 이후부터의 밀리초, 고로 큰게 더 미래입니다.
		if (releaseTimestamp > nowTimestamp)
			return false;

		return true;
	}

	/**
	 * DB에 유저자료를 업로드해야하는 시간이 되었는지 확인합니다.
	 * 
	 * @return
	 */
	public boolean isNeedUpload() {
		if (this.lastUploaded == 0)
			return true;

		Long nowTimestamp = Calendar.getInstance().getTime().getTime();
		Long diff = TimeUnit.MILLISECONDS.toMinutes(nowTimestamp - this.lastUploaded);

		if (diff < 3) // 3분이 지나지 않았다면 업로드 필요없음
			return false;

		return true;
	}

	/**
	 * 오진아웃제 경고에 사용되는 함수입니다.<br>
	 * double 형으로 1.2 같이 경고를 줄 수도 있으며,<br>
	 * 더한 값이 5.0이 넘어가면 true를 반환하므로,<br>
	 * 반환값이 true이면 밴처리 해야합니다.
	 * 
	 * @param count
	 * @return boolean
	 */
	public boolean warn(double count) {
		this.setModified();

		this.fiveStrikes += count;
		return (this.fiveStrikes >= (double) 5.0) ? true : false;
	}

	/**
	 * 몇초 동안 유저를 차단할지를 입력받아서<br>
	 * 계정정보에 입력해줍니다. (*DB에 실제반영은 수동!)
	 * 
	 * @param second
	 */
	public void ban(int second) {
		this.banCalendar(Calendar.SECOND, second);
	}

	/**
	 * 몇분 동안 유저를 차단할지를 입력받아서<br>
	 * 계정정보에 입력해줍니다. (*DB에 실제반영은 수동!)
	 * 
	 * @param second
	 */
	public void banMinute(int minute) {
		this.banCalendar(Calendar.MINUTE, minute);
	}

	/**
	 * 몇일 동안 유저를 차단할지를 입력받아서<br>
	 * 계정정보에 입력해줍니다. (*DB에 실제반영은 수동!)
	 * 
	 * @param second
	 */
	public void banDay(int day) {
		this.banCalendar(Calendar.DATE, day);
	}

	public void banCalendar(int field, int amount) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(field, amount);
		this.banTimestamp(calendar.getTime().getTime());
	}

	public void banTimestamp(long releaseTimestamp) {
		this.setModified();
		this.banned = String.valueOf(releaseTimestamp);
	}

	public void setBanCause(String banCause) {
		this.setModified();
		this.banCause = banCause;
	}

	public void setLastBanReleaseCause(String lastBanReleaseCause) {
		this.setModified();
		this.lastBanReleaseCause = lastBanReleaseCause;
	}

	/**
	 * DB에 비동기로 이 계정정보를 전송합니다.<br>
	 * 만일의 사태를 방지하기 위해서(서버크래시)<br>
	 * 서버는 주기적으로 유저의 정보를 비동기전송해야합니다.
	 */
	public void upload() {
		if (this.uuid == null || this.isModified == false)
			return;

		this.lastUploaded = Calendar.getInstance().getTime().getTime();
		this.setModified(false);
		
		// ID 정보가 존재하면 ID로 하고, 없을때만 UUID로
		if(this._id == null){
			ParfaitAuth.updateAccountAsync(this.uuid, this.convertToDocument());
		}else{
			ParfaitAuth.updateAccount_IdAsync(this._id, this.convertToDocument());
		}
	}

	/**
	 * 언제 차단이 풀리는지 일자를 문자열로 반환합니다.<br>
	 * ex: 2016-04-01 20:43:47.822
	 * 
	 * @return String
	 */
	public String getUnblockPeriod() {
		if (this.banned == null || this.banned == "null")
			return null;
		return (new Timestamp(Long.valueOf(this.banned))).toString();
	}

	/**
	 * 언제 마지막으로 접속했는지 일자를 문자열로 반환합니다.<br>
	 * ex: 2016-04-01 20:43:47.822
	 * 
	 * @return String
	 */
	public String getLastDate() {
		if (this.banned == null || this.banned == "null")
			return null;
		return (new Timestamp(Long.valueOf(this.lastDate))).toString();
	}

	/**
	 * 해당 계정이 어떤 유형인지를 문자열로 반환합니다.
	 * 
	 * @return String
	 */
	public String getAccountType() {
		String type = "DEFAULT";
		switch (this.accountType) {
		case Account.TYPE_ADMIN:
			type = "ADMIN";
			break;
		case Account.TYPE_BUILDER:
			type = "BUILDER";
			break;
		case Account.TYPE_DEFAULT:
			type = "DEFAULT";
			break;
		case Account.TYPE_GUEST:
			type = "GUEST";
			break;
		case Account.TYPE_NORMAL:
			type = "NORMAL";
			break;
		case Account.TYPE_OVER_POWER:
			type = "OVERPOWER";
			break;
		}
		return type;
	}

	/**
	 * 해당 플레이어에게 계정유형에 맞는 권한을 적용합니다.
	 * 
	 * @param player
	 */
	public void applyAccountType(Player player) {
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		switch (this.accountType) {
		case Account.TYPE_ADMIN:
			player.setOp(true);
			player.setGamemode(Player.CREATIVE);
			player.setAllowFlight(true);
			player.sendMessage(plugin.getMessage("info-admin-account-permission-granted"));
			break;
		case Account.TYPE_BUILDER:
			player.setOp(false);
			player.setGamemode(Player.CREATIVE);
			player.setAllowFlight(true);
			player.addAttachment(plugin, "nukkit.command.gamemode", true);
			player.sendMessage(plugin.getMessage("info-builder-account-permission-granted"));
			break;
		case Account.TYPE_DEFAULT:
			player.setOp(false);
			player.setGamemode(Server.getInstance().getGamemode());
			break;
		case Account.TYPE_GUEST:
			player.setOp(false);
			player.setGamemode(Player.VIEW);
			player.sendMessage(plugin.getMessage("info-guest-account-permission-granted"));
			break;
		case Account.TYPE_NORMAL:
			player.setOp(false);
			player.setGamemode(Player.SURVIVAL);
			break;
		case Account.TYPE_OVER_POWER:
			player.setOp(false);
			player.setAllowFlight(true);
			player.addAttachment(plugin, "nukkit.command.gamemode", true);
			player.addAttachment(plugin, "nukkit.command.say", true);
			player.addAttachment(plugin, "nukkit.command.time.add;" + "nukkit.command.time.set;"
					+ "nukkit.command.time.start;" + "nukkit.command.time.stop", true);
			player.addAttachment(plugin, "nukkit.command.weather", true);
			player.addAttachment(plugin, "nukkit.command.ban.player", true);
			player.addAttachment(plugin, "nukkit.command.ban.ip", true);
			player.addAttachment(plugin, "nukkit.command.ban.list", true);
			player.addAttachment(plugin, "nukkit.command.unban.player", true);
			player.addAttachment(plugin, "nukkit.command.unban.ip", true);
			player.addAttachment(plugin, plugin.getMessage("commands-howtoban-permission"), true);
			player.addAttachment(plugin, plugin.getMessage("commands-account-info-permission"), true);
			player.addAttachment(plugin, plugin.getMessage("commands-account-find-permission"), true);
			player.addAttachment(plugin, plugin.getMessage("commands-ban-account-permission"), true);
			player.addAttachment(plugin, plugin.getMessage("commands-ban-ipaddress-permission"), true);
			player.addAttachment(plugin, plugin.getMessage("commands-ban-subnet-permission"), true);
			player.addAttachment(plugin, plugin.getMessage("commands-ban-release-permission"), true);
			player.sendMessage(plugin.getMessage("info-op-account-permission-granted"));
			break;
		}
	}

	public LinkedHashMap<String, Object> getAdditionalData() {
		this.setModified();
		return this.additionalData;
	}

	/**
	 * 계정의 정보를 변경했을 경우 이 함수를 호출해야합니다.<br>
	 * 이 함수가 불려져야만 계정정보가 변경된 것으로 간주하고<br>
	 * 다음번 계정 업로드때 해당 자료가 업로드 될 것입니다.
	 */
	public void setModified() {
		this.isModified = true;
	}

	/**
	 * 계정의 정보를 변경했을 경우 이 함수를 호출해야합니다.<br>
	 * 이 함수가 불려져야만 계정정보가 변경된 것으로 간주하고<br>
	 * 다음번 계정 업로드때 해당 자료가 업로드 될 것입니다.
	 */
	public void setModified(boolean bool) {
		this.isModified = bool;
	}
}