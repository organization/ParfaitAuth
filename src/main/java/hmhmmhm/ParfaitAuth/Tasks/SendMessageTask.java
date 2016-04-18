package hmhmmhm.ParfaitAuth.Tasks;

import java.util.LinkedHashMap;

import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.Task;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class SendMessageTask extends Task {
	private CommandSender sender;
	private String stringKey;
	private int index = 1;

	// 메시지 테스크 간에 중복으로 작동하지 않도록 방지
	public static LinkedHashMap<String, Task> userUUIDMap = new LinkedHashMap<String, Task>();

	public SendMessageTask(CommandSender sender, String stringKey) {
		this.sender = sender;
		this.stringKey = stringKey;

		// 이미 작동중인 메시지 전달 테스크가 있을 경우
		if (userUUIDMap.get(sender.getName()) != null) {
			Task oldTask = userUUIDMap.get(sender.getName());

			// 그 테스크가 여전히 작동하고 있을 경우
			if (!oldTask.getHandler().isCancelled()) {
				oldTask.cancel();
				sender.sendMessage(ParfaitAuthPlugin.getPlugin().getMessage("status-new-message-task-now-working"));
			}
		}

		userUUIDMap.put(sender.getName(), this);
	}

	@Override
	public void onRun(int currentTick) {
		String message = ParfaitAuthPlugin.getPlugin().getMessage(this.stringKey + index);

		if (message == null) {
			userUUIDMap.put(this.sender.getName(), null);
			this.cancel();
			return;
		}

		sender.sendMessage(message);
		this.index++;
	}
}
