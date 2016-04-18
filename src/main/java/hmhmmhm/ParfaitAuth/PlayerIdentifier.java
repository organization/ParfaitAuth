package hmhmmhm.ParfaitAuth;

import java.util.ArrayList;

import cn.nukkit.Player;

public class PlayerIdentifier {
	public static ArrayList<Player> identifiers = new ArrayList<Player>();

	public static int put(Player player) {
		if (PlayerIdentifier.identifiers.size() >= 9999)
			PlayerIdentifier.identifiers = new ArrayList<Player>();

		PlayerIdentifier.identifiers.add(player);
		return PlayerIdentifier.identifiers.size() - 1;
	}

	public static Player get(int index) {
		if (PlayerIdentifier.identifiers.get(index) == null)
			return null;

		return PlayerIdentifier.identifiers.get(index);
	}
}