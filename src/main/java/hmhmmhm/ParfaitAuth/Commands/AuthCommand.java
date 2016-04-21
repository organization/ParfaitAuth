package hmhmmhm.ParfaitAuth.Commands;

import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.TaskHandler;
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
			if (args.length > 0 && args[0].equals(this.getMessage(this.commandKey + "-sub-info"))) {
				SendMessageTask task = new SendMessageTask(sender, this.commandKey + "-help-");
				TaskHandler handler = this.getServer().getScheduler().scheduleRepeatingTask(task, 40);
				task.setHandler(handler);
				return true;
			}

			// /인증 을 입력한 경우 인증용 명령어 전송
			SendMessageTask task = new SendMessageTask(sender, this.commandKey + "-sub-info-help-");
			TaskHandler handler = this.getServer().getScheduler().scheduleRepeatingTask(task, 40);
			task.setHandler(handler);
			return true;
		}
		return false;
	}
}