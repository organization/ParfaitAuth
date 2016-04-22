package hmhmmhm.ParfaitAuth.Commands;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Events.UnregisterEvent;

public class UnregisterCommand extends ParfaitAuthCommand {
	public static LinkedHashMap<UUID, Long> userUUIDMap = new LinkedHashMap<UUID, Long>();

	public UnregisterCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("unregister", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(this.getMessage("error-in-game-user-only"));
				return true;
			}

			if (ParfaitAuth.authorisedID.get((Player) sender) == null) {
				sender.sendMessage(this.getMessage("error-please-login-first"));
				return true;
			}

			// 처음입력하는 경우 주의문 띄우기
			if (userUUIDMap.get(((Player) sender).getUniqueId()) == null) {
				Long currentTimestamp = Calendar.getInstance().getTime().getTime();

				userUUIDMap.put(((Player) sender).getUniqueId(), currentTimestamp);
				sender.sendMessage(plugin.getMessage("caution-are-you-really-unregister"));
				sender.sendMessage(plugin.getMessage("caution-if-you-want-unregister-hit-again"));
				return true;
			}

			// 두번째로 입력하는 경우
			if (userUUIDMap.get(((Player) sender).getUniqueId()) != null) {
				Long pastTimestamp = userUUIDMap.get(((Player) sender).getUniqueId());
				Long currentTimestamp = Calendar.getInstance().getTime().getTime();
				Long diff = TimeUnit.MILLISECONDS.toSeconds(currentTimestamp - pastTimestamp);

				// 이전보다 30초를 넘어서 입력되었다면 다시 주의문 띄우기
				if (diff >= 30) {
					userUUIDMap.put(((Player) sender).getUniqueId(), currentTimestamp);
					sender.sendMessage(plugin.getMessage("caution-are-you-really-unregister"));
					sender.sendMessage(plugin.getMessage("caution-if-you-want-unregister-hit-again"));
					return true;
				}
			}

			UnregisterEvent unregisterEvent = new UnregisterEvent((Player) sender);
			this.getServer().getPluginManager().callEvent(unregisterEvent);

			if (unregisterEvent.isCancelled()) {
				if (unregisterEvent.reason != null) {
					sender.sendMessage(unregisterEvent.reason);
				} else {
					sender.sendMessage(plugin.getMessage("error-unregister-is-cancelled-by-other-plugin"));
				}
				return true;
			}

			ParfaitAuth.deleteAccountAsync(((Player) sender).getUniqueId());
			((Player) sender).kick(ParfaitAuthPlugin.getPlugin().getMessage("kick-successfully-unregistered"));
			return true;
		}
		return false;
	}
}
