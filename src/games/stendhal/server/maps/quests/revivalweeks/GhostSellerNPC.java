package games.stendhal.server.maps.quests.revivalweeks;

import java.awt.Point;
import java.util.Arrays;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.seasonal.SeasonalEventService;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Duszek;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/** The seller is seasonal; companions already purchased are permanent. */
public class GhostSellerNPC implements LoadableContent {

	public static final String NPC_NAME = "Jaromira";
	private static final String ZONE = "0_zakopane_s";
	private static final int PRICE = 5000;
	private SpeakerNPC npc;

	@Override
	public void addToWorld() {
		if (npc != null || !SeasonalEventService.get().isMineTownEnabled()) {
			return;
		}
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(ZONE);
		if (zone == null || SingletonRepository.getNPCList().get(NPC_NAME) != null) {
			throw new IllegalStateException("Nie można ustawić sprzedawczyni duszków w " + ZONE);
		}
		final SpeakerNPC seller = new SpeakerNPC(NPC_NAME) {
			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj. Nie lękaj się tych małych zjaw. Podczas Halloween szukam dla nich dobrych opiekunów. Zapytaj o #duszka albo moją #ofertę.");
				addJob("Opiekuję się duszkami, które zagubiły drogę w jesienną noc. Niektóre wolą zostać przy ludziach i towarzyszyć im w wędrówkach.");
				addHelp("Jeśli chcesz przygarnąć duszka, powiedz #'kupię duszka'. Kosztuje 5000 monet. Możesz mieć jednego towarzysza naraz, tak samo jak z owczarkiem.");
				addOffer("Za 5000 monet pomogę ci znaleźć własnego duszka. Powiedz #'kupię duszka', jeśli chcesz go przygarnąć.");
				addReply(Arrays.asList("duszek", "duszka", "duszki", "opieka", "care", "wzrost"),
						"Duszek nie potrzebuje jedzenia i nie walczy. Wyrośnie po dziesięciu godzinach spędzonych z tobą w grze. Wylogowanie nie odbierze mu postępów, a po Halloween pozostanie twoim towarzyszem.");
				add(ConversationStates.ATTENDING, ConversationPhrases.PURCHASE_MESSAGES, null,
						ConversationStates.BUY_PRICE_OFFERED,
						"Przygarniesz jednego duszka za 5000 monet?", null);
				add(ConversationStates.BUY_PRICE_OFFERED, ConversationPhrases.YES_MESSAGES, null,
						ConversationStates.ATTENDING, null,
						(player, sentence, raiser) -> buyGhost(player, raiser));
				add(ConversationStates.BUY_PRICE_OFFERED, ConversationPhrases.NO_MESSAGES, null,
						ConversationStates.ATTENDING, "Dobrze. Wróć, gdy zechcesz znaleźć towarzysza.", null);
				addGoodbye("Niech dobre duchy czuwają nad twoją drogą.");
			}
		};
		seller.setEntityClass("youngwitchnpc");
		seller.setDescription("Oto Jaromira. Podczas Halloween szuka opiekunów dla małych duszków.");
		seller.setGender("F");
		seller.setDirection(Direction.DOWN);
		seller.initHP(100);
		final Point position = freePosition(zone, seller, 30, 20);
		if (position == null) {
			throw new IllegalStateException("Brakuje miejsca dla " + NPC_NAME);
		}
		seller.setPosition(position.x, position.y);
		zone.add(seller);
		npc = seller;
	}

	private void buyGhost(final Player player, final EventRaiser seller) {
		if (npc == null || !SeasonalEventService.get().isMineTownEnabled()) {
			seller.say("Przyjdź do mnie podczas Halloween. Wtedy duszki szukają opiekunów.");
			return;
		}
		if (player.hasPet()) {
			seller.say("Masz już towarzysza. Najpierw zaopiekuj się tym, który już ci zaufał.");
			return;
		}
		if (MoneyUtils.getTotalMoneyInCopper(player) < PRICE) {
			seller.say("Nie masz 5000 monet. Duszek może jeszcze na ciebie poczekać.");
			return;
		}
		final StendhalRPZone zone = player.getZone();
		if (zone == null || zone != npc.getZone()) {
			return;
		}
		final Duszek ghost = new Duszek();
		final Point position = freePosition(zone, ghost, player.getX(), player.getY() + 1);
		if (position == null) {
			seller.say("Zrób trochę miejsca obok siebie, żeby duszek mógł do ciebie dołączyć.");
			return;
		}
		ghost.setPosition(position.x, position.y);
		zone.add(ghost);
		if (!MoneyUtils.removeMoney(player, PRICE)) {
			zone.remove(ghost);
			seller.say("Nie masz 5000 monet.");
			return;
		}
		player.setPet(ghost);
		player.incBoughtForItem("duszek", 1);
		player.incCommerceTransaction(NPC_NAME, PRICE, false);
		player.notifyWorldAboutChanges();
		seller.say("Spójrz, już cię polubił! Dbaj o swojego duszka. Wspólne wędrówki pomogą mu wyrosnąć.");
	}

	private static Point freePosition(final StendhalRPZone zone,
			final Entity entity, final int x, final int y) {
		for (int radius = 0; radius <= 4; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dy = -radius; dy <= radius; dy++) {
					if (!zone.collides(entity, x + dx, y + dy)
							&& zone.getEntitiesAt(x + dx, y + dy).isEmpty()
							&& zone.getPortal(x + dx, y + dy) == null) {
						return new Point(x + dx, y + dy);
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean removeFromWorld() {
		if (npc != null) {
			if (npc.getZone() != null) {
				npc.getZone().remove(npc);
			}
			npc = null;
		}
		return true;
	}
}
