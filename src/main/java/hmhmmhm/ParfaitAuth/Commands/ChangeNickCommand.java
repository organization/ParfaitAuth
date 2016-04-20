package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuth;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.ChangeNameTask;

public class ChangeNickCommand extends ParfaitAuthCommand {
	public ChangeNickCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("changenick", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			if (args[0] == null || args[1] != null) {
				// TODO 명령어설명
				return true;
			}

			if (!(sender instanceof Player)) {
				// TODO 인게임유저만 가능
				return true;
			}

			Player player = (Player) sender;

			if (ParfaitAuth.authorisedID.get(player.getUniqueId()) == null) {
				// TODO ID계정으로 인증된 유저만가능
				return true;
			}

			if (!ParfaitAuth.checkRightName(args[0])) {
				// TODO 잘못된 닉네임 입력됨
				return true;
			}

			this.getServer().getScheduler()
					.scheduleAsyncTask(new ChangeNameTask(sender.getName(), player.getUniqueId().toString(), args[0]));
			return true;
		}
		return false;
	}
}
