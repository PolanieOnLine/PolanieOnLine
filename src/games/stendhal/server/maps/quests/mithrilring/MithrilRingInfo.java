package games.stendhal.server.maps.quests.mithrilring;

public class MithrilRingInfo {
	private static final String QUEST_SLOT = "mithril_ring";

	private static final String GOLD_RING_SLOT = "zloty_pierscien";

	private static final String RING = "złoty pierścień";

	public static final String NPC = "Amileusz";
	public static final String FIRST_SCHEME_NPC = "Mieczysław";

	public String getQuestSlot() {
		return QUEST_SLOT;
	}

	public String getShieldQuestSlot() {
		return GOLD_RING_SLOT;
	}

	public String getRingName() {
		return RING;
	}
}
