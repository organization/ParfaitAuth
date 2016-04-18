package hmhmmhm.ParfaitAuth.Tasks;

import java.util.ArrayList;

import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.Task;
import hmhmmhm.ParfaitAuth.ParfaitAuthPlugin;

public class SendUserIdentifierTask extends Task {
	private CommandSender sender;

	private ArrayList<String> list;
	private int index = 0;

	public SendUserIdentifierTask(CommandSender sender, ArrayList<String> list) {
		this.sender = sender;
		this.list = list;

		// 이미 작동중인 메시지 전달 테스크가 있을 경우
		if (SendMessageTask.userUUIDMap.get(sender.getName()) != null) {
			Task oldTask = SendMessageTask.userUUIDMap.get(sender.getName());

			// 그 테스크가 여전히 작동하고 있을 경우
			if (!oldTask.getHandler().isCancelled()) {
				oldTask.cancel();
				sender.sendMessage(ParfaitAuthPlugin.getPlugin().getMessage("status-new-message-task-now-working"));
			}
		}

		SendMessageTask.userUUIDMap.put(sender.getName(), this);
	}

	@Override
	public void onRun(int currentTick) {
		// * 포함된 유저 명단을 표시합니다.
		// * [1] user_1 [2] user_2
		// * [2] user_3 [3] user_4

		if (this.index == 0)
			sender.sendMessage(ParfaitAuthPlugin.getPlugin().getMessage("status-start-show-user-identifier-list"));

		String message = null;

		for (int i = 0; i <= 1; i++) {
			if (!this.list.contains(this.index)) {
				if (message != null)
					sender.sendMessage(message);
				SendMessageTask.userUUIDMap.put(this.sender.getName(), null);
				this.cancel();
				return;
			}

			message += this.list.get(this.index);
			this.index++;
		}

		sender.sendMessage(message);
	}
}
