package hmhmmhm.ParfaitAuth.Commands;

import java.util.ArrayList;
import java.util.Map.Entry;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.Account;
import hmhmmhm.ParfaitAuth.EventHandler;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.PlayerIdentifier;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;
import hmhmmhm.ParfaitAuth.Tasks.SendUserIdentifierTask;

public class AccountFindCommand extends ParfaitAuthCommand {

	public AccountFindCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("account-find", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// f <필터> <검색어>

		if (command.getName().toLowerCase() == this.commandName) {
			if (args[0] == null) {
				this.getServer().getScheduler()
						.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
				return true;
			}

			// /f 아이디 검색어
			if (args[0] == this.getMessage(this.commandName + "-sub-id")) {
				if (args[1] == null) {
					this.getServer().getScheduler()
							.scheduleRepeatingTask(new SendMessageTask(sender, this.commandName + "-help-"), 10);
					return true;
				}

				// 접속중인 유저중 해당 아이디가 있나 확인
				Account findOnlineAccount = ParfaitAuth.authorisedID.get(args[1]);
				if (findOnlineAccount != null) {
					Player target = this.getServer().getPlayer(findOnlineAccount.nickname);

					// 찾았으면 바로 메시지 전송
					if (target != null) {
						int identifier = PlayerIdentifier.put(target);
						sender.sendMessage(
								plugin.getMessage("info-identifier-founded").replace("%1", String.valueOf(identifier)));
						return true;
					}
				}

				sender.sendMessage(plugin.getMessage("error-identifier-create-failed"));
				return true;
			}

			// /f 닉네임 검색어
			if (args[0] == this.getMessage(this.commandName + "-sub-nick")) {
				ArrayList<String> list = new ArrayList<String>();

				// 온라인 상태인 유저목록 순회
				for (Entry<String, Player> entry : this.getServer().getOnlinePlayers().entrySet()) {
					Player player = entry.getValue();

					// 검색어가 닉네임에 포함되는지 체크
					if (player.getName().split(args[1])[1] != null) {
						int identifier = PlayerIdentifier.put(player);
						list.add("[" + identifier + "] " + player.getName() + " ");
					}
				}

				// 식별번호 시간차를 갖고 나눠서 보여지게함
				if (list.size() != 0) {
					this.getServer().getScheduler().scheduleRepeatingTask(new SendUserIdentifierTask(sender, list), 10);
					return true;
				}

				sender.sendMessage(plugin.getMessage("error-identifier-create-failed"));
				return true;
			}

			// /f 채팅
			if (args[0] == this.getMessage(this.commandName + "-sub-chat")) {
				ArrayList<String> list = new ArrayList<String>();

				// 이벤트 헨들러에 저장된 채팅친 유저리스트 순회
				for (Player player : EventHandler.lastChatList) {
					int identifier = PlayerIdentifier.put(player);
					list.add("[" + identifier + "] " + player.getName() + " ");
				}

				// 식별번호 시간차를 갖고 나눠서 보여지게함
				if (list.size() != 0) {
					this.getServer().getScheduler().scheduleRepeatingTask(new SendUserIdentifierTask(sender, list), 10);
					return true;
				}

				sender.sendMessage(plugin.getMessage("error-identifier-create-failed"));
				return true;
			}

			// /f 접속
			if (args[0] == this.getMessage(this.commandName + "-sub-login")) {
				ArrayList<String> list = new ArrayList<String>();

				// 이벤트 헨들러에 저장된 최근접속한 유저리스트 순회
				for (Player player : EventHandler.lastLoginList) {
					int identifier = PlayerIdentifier.put(player);
					list.add("[" + identifier + "] " + player.getName() + " ");
				}

				// 식별번호 시간차를 갖고 나눠서 보여지게함
				if (list.size() != 0) {
					this.getServer().getScheduler().scheduleRepeatingTask(new SendUserIdentifierTask(sender, list), 10);
					return true;
				}

				sender.sendMessage(plugin.getMessage("error-identifier-create-failed"));
				return true;
			}

			// /f 나감
			if (args[0] == this.getMessage(this.commandName + "-sub-logout")) {
				ArrayList<String> list = new ArrayList<String>();

				// 이벤트 헨들러에 저장된 최근나간 유저리스트 순회
				for (Player player : EventHandler.lastLoginList) {
					int identifier = PlayerIdentifier.put(player);
					list.add("[" + identifier + "] " + player.getName() + " ");
				}

				// 식별번호 시간차를 갖고 나눠서 보여지게함
				if (list.size() != 0) {
					this.getServer().getScheduler().scheduleRepeatingTask(new SendUserIdentifierTask(sender, list), 10);
					return true;
				}

				sender.sendMessage(plugin.getMessage("error-identifier-create-failed"));
				return true;
			}

			// /f 복잡닉
			if (args[0] == this.getMessage(this.commandName + "-sub-diff")) {
				ArrayList<String> list = new ArrayList<String>();

				for (Entry<String, Player> entry : this.getServer().getOnlinePlayers().entrySet()) {
					Player player = entry.getValue();

					int diff = ParfaitAuth.getNickNameDifficult(player.getName());

					if (diff != 0) {
						int identifier = PlayerIdentifier.put(player);
						list.add("[" + identifier + "] " + player.getName() + " ");
					}
				}

				// 식별번호 시간차를 갖고 나눠서 보여지게함
				if (list.size() != 0) {
					this.getServer().getScheduler().scheduleRepeatingTask(new SendUserIdentifierTask(sender, list), 10);
					return true;
				}

				sender.sendMessage(plugin.getMessage("error-identifier-create-failed"));
				return true;
			}
		}
		return false;
	}
}