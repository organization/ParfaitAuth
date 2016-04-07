package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class ChangeNickCommand extends ParfaitAuthCommand {
	public ChangeNickCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("changenick", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			// TODO
			// TODO 닉네임변경 명령어사용시 *가 앞에 붙는 닉네임으로 변경불가처리
			return true;
		}
		return false;
	}
}
