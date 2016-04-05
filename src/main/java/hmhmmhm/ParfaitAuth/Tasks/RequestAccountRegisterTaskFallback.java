package hmhmmhm.ParfaitAuth.Tasks;

import java.util.UUID;

import cn.nukkit.scheduler.Task;

public class RequestAccountRegisterTaskFallback extends Task {
	String username;

	public RequestAccountRegisterTaskFallback(String username, UUID taskUUID) {
		this.username = username;
	}

	@Override
	public void onRun(int currentTick) {
		
	}

}
