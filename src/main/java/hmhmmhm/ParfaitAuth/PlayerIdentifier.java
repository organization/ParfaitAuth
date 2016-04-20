package hmhmmhm.ParfaitAuth;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerIdentifier {
	public static ArrayList<UUID> identifiers = new ArrayList<UUID>();

	public static int put(UUID uuid) {
		if (PlayerIdentifier.identifiers.size() >= 9999)
			PlayerIdentifier.identifiers = new ArrayList<UUID>();

		PlayerIdentifier.identifiers.add(uuid);
		return PlayerIdentifier.identifiers.size() - 1;
	}

	public static UUID get(int index) {
		if (PlayerIdentifier.identifiers.get(index) == null)
			return null;

		return PlayerIdentifier.identifiers.get(index);
	}
}