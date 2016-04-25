package hmhmmhm.ParfaitAuth.Tasks;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class AccountStatisticsTask extends AsyncTask {
	private String sender;
	private ArrayList<Integer> list;
	private boolean isFailed = false;

	public AccountStatisticsTask(String sender) {
		this.sender = sender;
	}

	@Override
	public void onRun() {
		try {
			this.list = ParfaitAuth.getAccountStatistics();
		} catch (Exception e) {
			this.isFailed = true;
		}
	}

	public void onCompletion(Server server) {
		Player player = server.getPlayer(sender);
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		if (player == null)
			return;

		player.sendMessage(plugin.getMessage("status-show-account-statistics"));

		if (isFailed) {
			player.sendMessage(plugin.getMessage("error-account-statistics-task-failed"));
			return;
		}

		player.sendMessage(plugin.getMessage("info-server-full-account-count").replace("%fullcount",
				String.valueOf(this.list.get(0))));
		player.sendMessage(plugin.getMessage("info-server-type-account-count")
				.replace("%uuidcount", String.valueOf(this.list.get(1)))
				.replace("%idcount", String.valueOf(this.list.get(2))));
	}
}