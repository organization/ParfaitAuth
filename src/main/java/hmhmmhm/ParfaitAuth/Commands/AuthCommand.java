package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;
import hmhmmhm.ParfaitAuth.Tasks.SendMessageTask;

public class AuthCommand extends ParfaitAuthCommand {
	public AuthCommand(ParfaitAuthPlugin plugin) {
		super(plugin);
		this.load("auth", false);
	}

	@Override
	public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.commandName) {
			// /인증 안내 를 입력한 경우 인증체계 설명 전송
			if (args[0] != null && args.length > 0 && args[0] == this.getMessage("commands-auth-sub-info")) {
				this.getServer().getScheduler().scheduleRepeatingTask(new SendMessageTask(sender, "authcommand-info-"),
						40);
				return true;
			}

			// /인증 을 입력한 경우 인증용 명령어 전송
			this.getServer().getScheduler()
					.scheduleRepeatingTask(new SendMessageTask(sender, "authcommand-commandlist-"), 20);
			return true;
		}
		return false;
	}
}