package games.stendhal.server.maps.challengearena;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ValueBuyerBehaviour;

/** Buys tradable loot in the arena lobby using the item's current value. */
public final class ChallengeArenaBuyerNPC {
	private ChallengeArenaBuyerNPC() { }

	public static void create(final StendhalRPZone zone, final int x, final int y) {
		final SpeakerNPC npc = new SpeakerNPC("Kupiec Areny") {
			@Override
			protected void createPath() { setPath(null); }

			@Override
			protected void createDialog() {
				addGreeting("Witaj! Skupuję łupy za ich wartość. Powiedz #sprzedaj i nazwę przedmiotu.");
				addJob("Odkupuję zdobycze wojowników Areny Wyzwań.");
				addHelp("Powiedz np. #sprzedaj miecz albo #sprzedaj 3 miecz. Wycenę podam przed zatwierdzeniem. Rzadkość zwiększa cenę, ale nie kupuję monet, kluczy i przedmiotów przypisanych do postaci.");
				addOffer("Skupuję przedmioty dopuszczone do handlu, mające dodatnią wartość. Nie musisz szukać ich na liście: powiedz #sprzedaj i nazwę zdobyczy.");
				addGoodbye("Niech kolejne walki przyniosą ci cenne łupy!");
				new BuyerAdder().addBuyer(this, new ValueBuyerBehaviour(
						SingletonRepository.getEntityManager().getDefaultItems()), false);
			}
		};
		npc.setEntityClass("barracksbuyernpc");
		npc.setGender("M");
		npc.setPosition(x, y);
		npc.setDirection(Direction.DOWN);
		npc.setDescription("Oto Kupiec Areny, odkupujący łupy z tarnowskiej Areny Wyzwań.");
		npc.initHP(100);
		zone.add(npc);
	}
}
