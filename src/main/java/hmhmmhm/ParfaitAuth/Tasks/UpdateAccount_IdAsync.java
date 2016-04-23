package hmhmmhm.ParfaitAuth.Tasks;

import org.bson.Document;
import cn.nukkit.scheduler.AsyncTask;
import hmhmmhm.ParfaitAuth.ParfaitAuth;

public class UpdateAccount_IdAsync extends AsyncTask {
	private Object _id;
	private Document document;

	public UpdateAccount_IdAsync(Object _id, Document document) {
		this._id = _id;
		this.document = document;
	}

	@Override
	public void onRun() {
		ParfaitAuth.updateAccount_id(this._id, this.document);
	}
}