package hmhmmhm.ParfaitAuth;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import hmhmmhm.ParfaitAuth.EventHandler;
import hmhmmhm.ParfaitAuth.Commands.AuthCommand;
import hmhmmhm.ParfaitAuth.Commands.ChangeNickCommand;
import hmhmmhm.ParfaitAuth.Commands.ChangePasswordCommand;
import hmhmmhm.ParfaitAuth.Commands.FindAccountCommand;
import hmhmmhm.ParfaitAuth.Commands.LanguageCommand;
import hmhmmhm.ParfaitAuth.Commands.LoginCommand;
import hmhmmhm.ParfaitAuth.Commands.RegisterCommand;
import hmhmmhm.ParfaitAuth.Commands.UnregisterCommand;

public class ParfaitAuthPlugin extends PluginBase {
	private Config settings;
	private Config language;
	private LinkedHashMap<String, Object> commandMap = new LinkedHashMap<String, Object>();

	/**
	 * 플러그인 언어 메시지 파일의 버전을 나타냅니다. 개발자는 향후 메시지 내용이 변경되면 이 숫자를 올려줘야합니다!
	 */
	final int languageFileVersion = 1;

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();
		this.loadResources();
		this.loadCommands();
		this.getServer().getPluginManager().registerEvents(new EventHandler(this), this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		for (Entry<String, Object> entry : commandMap.entrySet()) {
			String key = entry.getKey();
			Object pluginCommand = entry.getValue();

			if (pluginCommand instanceof hmhmmhm.ParfaitAuth.Commands.Command)
				if (((hmhmmhm.ParfaitAuth.Commands.Command) pluginCommand).onCommand(sender, command, label, args))
					return true;
		}
		return false;
	}

	/**
	 * 플러그인의 설정과 언어자료를 불러옵니다.
	 */
	private void loadResources() {
		// 기본설정 JSON을 서버에 복사합니다.
		this.saveResource("settings.json");
		this.settings = new Config(new File(this.getDataFolder(), "settings.json"), Config.JSON);

		// 언어자료를 불러옵니다.
		this.loadLanguage(false);

		// 서버에 있는 언어자료가 최신판이 아니면 업데이트합니다.
		if ((int) this.language.get("languageFileVersion", 1) != this.languageFileVersion)
			this.loadLanguage(true);
	}

	private void loadLanguage(boolean replace) {
		// 현재서버에서 사용 중인 언어자료가 존재할 경우
		if (this.getResource("languages/" + this.getServer().getLanguage().getLang() + ".json") != null) {
			this.saveResource("languages/" + this.getServer().getLanguage().getLang() + ".json", replace);
			this.language = new Config(new File(this.getDataFolder().getAbsolutePath() + "/languages/"
					+ this.getServer().getLanguage().getLang() + ".json"), Config.JSON);
		} else {
			// 존재하지 않으면 영어 언어자료를 불러옵니다.
			this.saveResource("languages/eng.json", replace);
			this.language = new Config(new File(this.getDataFolder().getAbsolutePath() + "/languages/eng.json"),
					Config.JSON);
		}
	}

	/**
	 * 명령어들을 서버에 등록합니다.
	 */
	private void loadCommands() {
		this.commandMap.put("AuthCommand", new AuthCommand(this));
		this.commandMap.put("ChangeNickCommand", new ChangeNickCommand(this));
		this.commandMap.put("ChangePasswordCommand", new ChangePasswordCommand(this));
		this.commandMap.put("FindAccountCommand", new FindAccountCommand(this));
		this.commandMap.put("LanguageCommand", new LanguageCommand(this));
		this.commandMap.put("LoginCommand", new LoginCommand(this));
		this.commandMap.put("RegisterCommand", new RegisterCommand(this));
		this.commandMap.put("UnregisterCommand", new UnregisterCommand(this));
	}

	/**
	 * 플러그인 설정을 가져옵니다.
	 * 
	 * @return Config
	 */
	public Config getSettings() {
		return this.settings;
	}

	/**
	 * 언어자료를 가져옵니다.
	 * 
	 * @return Config
	 */
	public Config getLanguage() {
		return this.language;
	}

	public String getMessage(String key) {
		return (String) this.language.get(key);
	}
}