package games.stendhal.server.maps.quests.mithrilring;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.NPCList;

class SearchingSchemes {
	private MithrilRingInfo mithrilring;

	public SearchingSchemes(final MithrilRingInfo mithrilring) {
		this.mithrilring = mithrilring;
	}

	private final NPCList npcs = SingletonRepository.getNPCList();

	public void addToWorld() {
		return;
	}
}
