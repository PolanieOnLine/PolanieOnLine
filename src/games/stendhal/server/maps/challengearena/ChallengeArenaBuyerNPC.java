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
				addGreeting("Witaj! Masz coś po walce? Chętnie odkupię twoje zdobycze.");
				addJob("Skupuję łupy od wojowników. Oni wracają na arenę, a ja szukam kupców na ich zdobycze.");
				addHelp("Chcesz coś sprzedać? Powiedz #sprzedaj i nazwę przedmiotu, na przykład #'sprzedaj polano'. Jeśli sprzedajesz kilka sztuk, podaj też ich liczbę. Najpierw zaproponuję cenę, a ty zdecydujesz.");
				addOffer("Chętnie odkupię broń, zbroje i inne łupy. Za rzadsze okazy zapłacę więcej. Monet, kluczy i rzeczy przypisanych do postaci nie biorę.");
				addGoodbye("Powodzenia na arenie! Wróć, jeśli znów nazbierasz łupów.");
				new BuyerAdder().addBuyer(this, new ValueBuyerBehaviour(
						SingletonRepository.getEntityManager().getDefaultItems()), false);
			}
		};
		npc.setEntityClass("buyer2npc");
		npc.setGender("M");
		npc.setPosition(x, y);
		npc.setDirection(Direction.LEFT);
		npc.setIdleDirection(Direction.LEFT);
		npc.setDescription("Oto Kupiec Areny. Czeka na wojowników wracających z łupami.");
		npc.initHP(100);
		zone.add(npc);
	}
}
