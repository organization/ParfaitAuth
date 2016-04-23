package hmhmmhm.ParfaitAuth;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;
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
import cn.nukkit.utils.TextFormat;
import hmhmmhm.ParfaitAuth.EventHandler;
import hmhmmhm.ParfaitAuth.Commands.AccountCommand;
import hmhmmhm.ParfaitAuth.Commands.AccountFindCommand;
import hmhmmhm.ParfaitAuth.Commands.AccountInfoCommand;
import hmhmmhm.ParfaitAuth.Commands.AuthCommand;
import hmhmmhm.ParfaitAuth.Commands.BanAccountCommand;
import hmhmmhm.ParfaitAuth.Commands.BanIpAddressCommand;
import hmhmmhm.ParfaitAuth.Commands.BanReleaseCommand;
import hmhmmhm.ParfaitAuth.Commands.BanSubnetAddressCommand;
import hmhmmhm.ParfaitAuth.Commands.ChangeNickCommand;
import hmhmmhm.ParfaitAuth.Commands.ChangePasswordCommand;
import hmhmmhm.ParfaitAuth.Commands.FindAccountCommand;
import hmhmmhm.ParfaitAuth.Commands.HowToBanCommand;
import hmhmmhm.ParfaitAuth.Commands.LoginCommand;
import hmhmmhm.ParfaitAuth.Commands.LogoutCommand;
import hmhmmhm.ParfaitAuth.Commands.ParfaitAuthCommand;
import hmhmmhm.ParfaitAuth.Commands.RegisterCommand;
import hmhmmhm.ParfaitAuth.Commands.UnregisterCommand;
import hmhmmhm.ParfaitAuth.Events.NewBannedAddressEvent;

public class ParfaitAuthPlugin extends PluginBase {
	/* 플러그인 로컬 세팅이 여기 저장됩니다. */
	private Config settings;

	/* 로딩된 언어파일이 여기 저장됩니다. */
	private Config language;

	/* 로딩된 랜덤닉네임 명단이 여기 저장됩니다. */
	private Config randomName;

	/* 로딩된 플러그인 명령어클래스 인스턴스들이 여기 저장됩니다. */
	private LinkedHashMap<String, Object> commandMap = new LinkedHashMap<String, Object>();

	/* 플러그인의 싱글톤 인스턴스가 여기 저장됩니다. */
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

		// 서버상태 문서에 적힐 외부 아이피를 확인
		this.getExternalAddress();

		// 계정정보를 주기적으로 DB에 업로드 합니다.
		this.accountDataUploader();
	}

	private void accountDataUploader() {
		this.getServer().getScheduler().scheduleDelayedRepeatingTask(new Task() {
			@Override
			public void onRun(int currentTick) {
				for (Player player : Server.getInstance().getOnlinePlayers().values()) {
					if (ParfaitAuth.authorisedID.get(player.getUniqueId()) != null) {
						Account account = ParfaitAuth.authorisedID.get(player.getUniqueId());
						if (account.isNeedUpload())
							account.upload();
					}
					if (ParfaitAuth.authorisedUUID.get(player.getUniqueId()) != null) {
						Account account = ParfaitAuth.authorisedUUID.get(player.getUniqueId());
						if (account.isNeedUpload())
							account.upload();
					}
				}
			}
		}, 1200, 1200);
	}

	/**
	 * 서버상태 문서에 적힐 외부 아이피를 비동기로 AWS에서 가져옵니다.
	 */
	private void getExternalAddress() {
		this.getServer().getScheduler().scheduleAsyncTask(new AsyncTask() {
			String address = null;

			@Override
			public void onRun() {
				try {
					URL whatismyip = new URL("http://checkip.amazonaws.com");
					BufferedReader in = null;
					try {
						in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
						this.address = in.readLine();
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
							}
						}
					}
				} catch (Exception e) {
				}
			}

			@Override
			public void onCompletion(Server server) {
				if (this.address != null)
					ParfaitAuth.externalAddress = address + ":" + server.getPort();
			}
		});
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
			if (entry.getValue().getAddress() == address) {
				String releasePeriod = (new Timestamp(period)).toString();
				entry.getValue().kick(this.getMessage("kick-address-is-banned").replace("%period", releasePeriod),
						false);
			}
		}

		ParfaitAuth.bannedAddress.put(address, period);
	}

	/**
	 * DB에서 이전에 차단된 네트워크 주소 명단을 받아 램으로 불러옵니다.
	 */
	private void getBannedAddress() {
		this.getServer().getScheduler().scheduleAsyncTask(new AsyncTask() {
			private LinkedHashMap<String, Long> list = null;

			@Override
			public void onRun() {
				this.list = ParfaitAuth.getBannedAddress();
			}

			@Override
			public void onCompletion(Server server) {
				if (this.list != null)
					for (Entry<String, Long> entry : this.list.entrySet())
						ParfaitAuthPlugin.getPlugin().addedBannedAddress(entry.getKey(), entry.getValue());
			}
		});
	}

	/**
	 * 10초마다 DB에 현재 시간을 타임스탬프로 저장해 올려서<br>
	 * 다른서버들이 이서버가 온라인상태임을 알 수 있게끔 지속적으로 갱신합니다..
	 */
	private void serverStatusUpdater() {
		this.getServer().getScheduler().scheduleRepeatingTask((new Task() {
			@Override
			public void onRun(int currentTick) {
				ParfaitAuth.updateServerStatus(ParfaitAuth.getParfaitAuthUUID());
			}
		}), 200);
	}

	/**
	 * 이 서버에 요청들어온 알림들을 DB에서 수집해옵니다.
	 */
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

	/**
	 * DB에 필수컬렉션과 필수문서가 만들어져있는지 확인후 없으면 생성합니다.<br>
	 * 만약 DB 버전이 플러그인에 내장된 DB버전보다 높거나 낮으면 그사항을 알립니다.
	 */
	private void initialDatabase() {
		ParfaitAuth.parfaitAuthUUID = UUID.fromString((String) this.getSettings().get("server-uuid"));
		ParfaitAuth.randomName = this.getRandomName();

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
		if (this.language.get("languageFileVersion", "1") != String.valueOf(this.languageFileVersion))
			this.loadLanguage(true);
	}

	/**
	 * 플러그인에서 사용하는 언어파일을 램으로 불러옵니다.<br>
	 * replace가 true면 서버의 언어파일을 내장파일로 교체(갱신)합니다.
	 * 
	 * @param replace
	 */
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

	/*
	 * 서버에서 사용할 랜덤닉네임명단을 램으로 불러옵니다.
	 */
	private void loadRandomName() {
		this.saveResource("randomname.json");
		this.randomName = new Config(new File(this.getDataFolder().getAbsolutePath() + "/randomname.json"),
				Config.JSON);
	}

	/**
	 * 명령어들을 서버에 등록합니다.
	 */
	private void loadCommands() {
		this.commandMap.put("AccountCommand", new AccountCommand(this));
		this.commandMap.put("AccountFindCommand", new AccountFindCommand(this));
		this.commandMap.put("AccountInfoCommand", new AccountInfoCommand(this));
		this.commandMap.put("AuthCommand", new AuthCommand(this));
		this.commandMap.put("BanAccountCommand", new BanAccountCommand(this));
		this.commandMap.put("BanIpAddressCommand", new BanIpAddressCommand(this));
		this.commandMap.put("BanReleaseCommand", new BanReleaseCommand(this));
		this.commandMap.put("BanSubnetAddressCommand", new BanSubnetAddressCommand(this));
		this.commandMap.put("ChangeNickCommand", new ChangeNickCommand(this));
		this.commandMap.put("ChangePasswordCommand", new ChangePasswordCommand(this));
		this.commandMap.put("FindAccountCommand", new FindAccountCommand(this));
		this.commandMap.put("HowToBanCommand", new HowToBanCommand(this));
		this.commandMap.put("LoginCommand", new LoginCommand(this));
		this.commandMap.put("LogoutCommand", new LogoutCommand(this));
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

	/**
	 * 플러그인에서 쓰는 메시지의 번역본을 가져옵니다.
	 * 
	 * @param key
	 * @return String
	 */
	public String getMessage(String key) {
		String message = (String) this.language.get(key);

		if (message == null || message == "null")
			return null;

		// Coloring
		if (key.split("error-").length == 2)
			message = TextFormat.RED + message;
		if (key.split("caution-").length == 2)
			message = TextFormat.YELLOW + message;
		if (key.split("status-").length == 2)
			message = TextFormat.DARK_AQUA + message;
		if (key.split("success-").length == 2)
			message = TextFormat.DARK_AQUA + message;
		if (key.split("info-").length == 2)
			message = TextFormat.DARK_AQUA + message;
		if (key.split("-help-").length == 2)
			message = TextFormat.DARK_AQUA + message;

		return message;
	}

	/**
	 * 파르페오스의 명령어 클래스의 인스턴스를 얻어옵니다.<br>
	 * 해당 인스턴스를 통해 파르페오스의 명령어를 실행시킬 수 있습니다.
	 * 
	 * @param key
	 * @return ParfaitAuthCommand
	 */
	public ParfaitAuthCommand getCommandClass(String key) {
		return (ParfaitAuthCommand) this.commandMap.get(key);
	}

	/**
	 * 파르페오스 플러그인의 인스턴스를 가져옵니다.
	 * 
	 * @return
	 */
	public static ParfaitAuthPlugin getPlugin() {
		return plugin;
	}
}