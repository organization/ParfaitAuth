package hmhmmhm.ParfaitAuth.Tasks;

import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.ParfaitAuth;

public class DeleteBannedAddressTask extends AsyncTask {
	private String address;

	public DeleteBannedAddressTask(String address) {
		this.address = address;
	}

	@Override
	public void onRun() {
		if (this.address != null)
			ParfaitAuth.deleteBannedAddress(this.address);
	}
}