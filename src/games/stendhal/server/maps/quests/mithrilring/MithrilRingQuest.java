package games.stendhal.server.maps.quests.mithrilring;

public class MithrilRingQuest {
	private static MithrilRingInfo mithrilring = new MithrilRingInfo();

	public void addToWorld() {
		new SearchingSchemes(mithrilring).addToWorld();
		new MakingScheme(mithrilring).addToWorld();
		new BringItems(mithrilring).addToWorld();
		new MakingRing(mithrilring).addToWorld();
	}
}
