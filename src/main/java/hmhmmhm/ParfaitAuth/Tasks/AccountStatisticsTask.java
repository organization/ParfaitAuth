package hmhmmhm.ParfaitAuth.Tasks;

import java.util.List;

import org.bson.Document;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class AccountStatisticsTask extends AsyncTask {
	private String sender;
	private List<Document> documents;

	public AccountStatisticsTask(String sender) {
		this.sender = sender;
	}

	@Override
	public void onRun() {
		this.documents = ParfaitAuth.getAccountStatistics();
	}

	public void onCompletion(Server server) {
		Player player = server.getPlayer(sender);
		ParfaitAuthPlugin plugin = ParfaitAuthPlugin.getPlugin();

		if (player == null)
			return;

		player.sendMessage(plugin.getMessage("status-show-account-statistics"));

		// TODO 다듬어야함
		for (Document document : this.documents)
			player.sendMessage(document.toJson());
	}
}