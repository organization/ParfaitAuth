package hmhmmhm.ParfaitAuth.Tasks;

import java.io.File;

import cn.nukkit.scheduler.AsyncTask;

public class FileDeleteTask extends AsyncTask {
	private String fileLocate;

	public FileDeleteTask(String fileLocate) {
		this.fileLocate = fileLocate;
	}

	@Override
	public void onRun() {
		File file = new File(this.fileLocate);
		file.delete();
	}

}
