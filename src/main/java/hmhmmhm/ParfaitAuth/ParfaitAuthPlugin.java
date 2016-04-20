package hmhmmhm.ParfaitAuth;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Event;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import hmhmmhm.ParfaitAuth.EventHandler;
import hmhmmhm.ParfaitAuth.Commands.AuthCommand;
import hmhmmhm.ParfaitAuth.Commands.ChangeNickCommand;
import hmhmmhm.ParfaitAuth.Commands.ChangePasswordCommand;
import hmhmmhm.ParfaitAuth.Commands.FindAccountCommand;
import hmhmmhm.ParfaitAuth.Commands.LoginCommand;
import hmhmmhm.ParfaitAuth.Commands.ParfaitAuthCommand;
import hmhmmhm.ParfaitAuth.Commands.RegisterCommand;
import hmhmmhm.ParfaitAuth.Commands.UnregisterCommand;
import hmhmmhm.ParfaitAuth.Events.NewBannedAddressEvent;

public class ParfaitAuthPlugin extends PluginBase {
	private Config settings;
	private Config language;
	private Config randomName;
	private LinkedHashMap<String, Object> commandMap = new LinkedHashMap<String, Object>();
	private static ParfaitAuthPlugin plugin;

	/**
	 * 플러그인 언어 메시지 파일의 버전을 나타냅니다. 개발자는 향후 메시지 내용이 변경되면 이 숫자를 올려줘야합니다!
	 */
	final int languageFileVersion = 1;

	@Override
	public void onEnable() {
		ParfaitAuthPlugin.plugin = this;

		this.checkCompatibility(); // 호환성체크
		this.loadResources(); // 기본파일 불러오기
		this.loadCommands(); // 명령어 불러오기
		this.initialDatabase(); // 데이터베이스 체크

		// 이벤트 핸들러 등록
		this.getServer().getPluginManager().registerEvents(new EventHandler(this), this);

		// DB에 서버 상태갱신
		this.serverStatusUpdater();

		// DB에서 푸시 이벤트 돌리기
		this.notificationCollector();

		// 차단해야하는 IP명부 가져오기 및 확인
		this.getBannedAddress();

		// TODO 외부 아이피 확인 및 서버상태 문서에 업로드하기
	}

	/**
	 * 새로 차단된 네트워크주소가 있음을 이서버에 통보합니다.
	 */
	public void addedBannedAddress(String address, Long period) {
		Event event = new NewBannedAddressEvent(address, period);
		this.getServer().getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		for (Entry<String, Player> entry : this.getServer().getOnlinePlayers().entrySet()) {
			if (entry.getValue().getAddress() == address)
				entry.getValue().kick(this.getMessage("kick-that-address-is-banned"));
		}

		ParfaitAuth.bannedAddress.put(address, period);
	}

	private void getBannedAddress() {
		this.getServer().getScheduler().scheduleAsyncTask(new AsyncTask() {
			private LinkedHashMap<String, Long> list;

			@Override
			public void onRun() {
				this.list = ParfaitAuth.getBannedAddress();
			}

			@Override
			public void onCompletion(Server server) {
				for (Entry<String, Long> entry : this.list.entrySet())
					ParfaitAuthPlugin.getPlugin().addedBannedAddress(entry.getKey(), entry.getValue());
			}
		});
	}

	private void serverStatusUpdater() {
		this.getServer().getScheduler().scheduleRepeatingTask((new Task() {
			@Override
			public void onRun(int currentTick) {
				ParfaitAuth.updateServerStatus(ParfaitAuth.getParfaitAuthUUID());
			}
		}), 200);
	}

	private void notificationCollector() {
		this.getServer().getScheduler().scheduleRepeatingTask((new Task() {
			@Override
			public void onRun(int currentTick) {
				Server.getInstance().getScheduler().scheduleAsyncTask(new AsyncTask() {
					private ArrayList<Event> events;

					@Override
					public void onRun() {
						this.events = ParfaitAuth.pullNotification();
					}

					@Override
					public void onCompletion(Server server) {
						if (this.events != null)
							for (Event event : this.events)
								if (event != null)
									server.getPluginManager().callEvent(event);
					}
				});
			}
		}), 5);
	}

	private void initialDatabase() {
		switch (ParfaitAuth.initialDatabase()) {
		case ParfaitAuth.CLIENT_IS_DEAD:
			this.getLogger().emergency(this.getMessage("caution-client-is-dead"));
			break;
		case ParfaitAuth.CAUTION_PLUGIN_IS_OUTDATE:
			this.getLogger().info(this.getMessage("caution-plugin-is-outdate"));
			break;
		case ParfaitAuth.UPDATED_DATABASE:
			this.getLogger().info(this.getMessage("status-database-was-updated"));
			break;
		case ParfaitAuth.SUCCESS:
			this.getLogger().info(this.getMessage("status-database-check-all-green"));
			break;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		for (Entry<String, Object> entry : commandMap.entrySet()) {
			Object pluginCommand = entry.getValue();

			if (pluginCommand instanceof hmhmmhm.ParfaitAuth.Commands.Command)
				if (((hmhmmhm.ParfaitAuth.Commands.Command) pluginCommand).onCommand(sender, command, label, args))
					return true;
		}
		return false;
	}

	/**
	 * 플러그인이 서버와 호환되는지 확인합니다.
	 */
	private void checkCompatibility() {
		// MongoDBLib이 없는 경우
		if (this.getServer().getPluginManager().getPlugin("MongoDBLib") == null) {
			this.getLogger().critical(this.getMessage("error-no-exist-mongodb"));
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	/**
	 * 플러그인의 설정과 언어자료를 불러옵니다.
	 */
	private void loadResources() {
		this.getDataFolder().mkdirs();

		// 기본설정 JSON을 서버에 저장합니다.
		LinkedHashMap<String, Object> defaultMap = new LinkedHashMap<String, Object>();
		defaultMap.put("server-uuid", this.getServer().getServerUniqueId().toString());
		this.settings = new Config(new File(this.getDataFolder(), "settings.json"), Config.JSON, defaultMap);

		// 랜덤 닉네임 목록을 불러옵니다.
		this.loadRandomName();

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

	private void loadRandomName() {
		this.saveResource("randomname.json");
		this.randomName = new Config(new File(this.getDataFolder().getAbsolutePath() + "/randomname.json"),
				Config.JSON);
	}

	/**
	 * 명령어들을 서버에 등록합니다.
	 */
	private void loadCommands() {
		this.commandMap.put("AuthCommand", new AuthCommand(this));
		this.commandMap.put("ChangeNickCommand", new ChangeNickCommand(this));
		this.commandMap.put("ChangePasswordCommand", new ChangePasswordCommand(this));
		this.commandMap.put("FindAccountCommand", new FindAccountCommand(this));
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

	/**
	 * 랜덤닉네임 목록을 가져옵니다.
	 * 
	 * @return Config
	 */
	public Config getRandomName() {
		return this.randomName;
	}

	public String getMessage(String key) {
		return (String) this.language.get(key);
	}

	public ParfaitAuthCommand getCommandClass(String key) {
		return (ParfaitAuthCommand) this.commandMap.get(key);
	}

	public static ParfaitAuthPlugin getPlugin() {
		return plugin;
	}
}