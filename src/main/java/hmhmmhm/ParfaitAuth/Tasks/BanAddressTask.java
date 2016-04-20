package hmhmmhm.ParfaitAuth.Tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.Notification;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class BanAddressTask extends AsyncTask {
	public String uuid = null;
	public String address = null;
	public String cause = "";
	public String sender = null;

	public int periodInt = 0;
	public long timestamp;

	@Override
	public void onRun() {
		if (this.uuid != null) {
			Account account = ParfaitAuth.getAccount(UUID.fromString(this.uuid));
			this.address = account.lastIp;
		}
		if (this.address != null) {
			this.timestamp = Calendar.getInstance().getTime().getTime();
			this.timestamp += TimeUnit.MINUTES.toMillis(periodInt);
			ParfaitAuth.addBannedAddress(this.address, this.timestamp);
		}
	}

	@Override
	public void onCompletion(Server server) {
		Player player = server.getPlayer(this.sender);
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		if (player == null)
			return;

		// 모든서버에 해당 아이피 추가차단 요청
		if (this.address != null) {
			ArrayList<String> data = new ArrayList<String>();
			data.add(this.address);
			data.add(String.valueOf(this.timestamp));
			Notification.push("hmhmmhm.ParfaitAuth.Tasks.BanAddressTask", data);
		}

		player.sendMessage(plugin.getMessage("success-address-banned"));
	}
}
