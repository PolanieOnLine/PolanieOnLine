package games.stendhal.client.gui.stats;

/** UI event fired when mastery level increases. */
public final class MasteryLevelUpEvent {
	private final int newLevel;

	public MasteryLevelUpEvent(int newLevel) {
		this.newLevel = newLevel;
	}

	public int getNewLevel() {
		return newLevel;
	}
}
