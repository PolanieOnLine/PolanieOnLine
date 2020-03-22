package games.stendhal.server.maps.quests;

import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.mithrilring.MithrilRingQuest;

public class MithrilRing extends AbstractQuest {
	private static final String QUEST_SLOT = "mithril_ring";
	private static Logger logger = Logger.getLogger(MithrilRing.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pierścień z mithrilu",
				"Zbieranie schematów na pierścień? Brzmi ciekawie! Kto by nie chciał mieć pierścienia z mithrilu...",
				false);

		MithrilRingQuest mithrilring = new MithrilRingQuest();
		mithrilring.addToWorld();
	}

	@Override
	public List<String> getHistory(Player player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMinLevel() {
		return 150;
	}

	@Override
	public String getNPCName() {
		return "Amileusz";
	}

	@Override
	public String getName() {
		return "MithrilRing";
	}

	@Override
	public String getRegion() {
		return Region.KRAKOW_CITY;
	}
}
