package games.stendhal.server.entity.status;

public class BloodAttackerFactory {
	public static BloodAttacker get(final String profile) {
		if (profile != null) {
			final String[] statusparams = profile.split(";");
			return new BloodAttacker(Integer.parseInt(statusparams[0]));
		}
		return null;
	}
}
