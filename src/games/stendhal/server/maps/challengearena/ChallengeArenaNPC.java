/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.Arrays;
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

/** Creates the master of the dedicated Challenge Arena near Krakow. */
public final class ChallengeArenaNPC {
	private ChallengeArenaNPC() {
	}

	public static void create(final StendhalRPZone zone, final int x, final int y) {
		final SpeakerNPC npc = new SpeakerNPC("Mistrz Wyzwań") {
			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wojowniku. Na tej arenie sam wybierasz stawkę i ryzyko. Zapytaj mnie o #arenę.");
				addJob("Prowadzę Arenę Wyzwań na ziemiach Kraka.");
				addHelp("Powiedz #arena aby wybrać stawkę. Podczas walki możesz powiedzieć #rezygnuję. Wpisowe wtedy przepada. Gdy chcesz wrócić do Krakowa powiedz #wyjdź.");
				addGoodbye("Wróć gdy będziesz gotowy na kolejną walkę.");

				add(ConversationStates.ATTENDING,
						Arrays.asList("arena", "arenę", "wyzwania"),
						null, ConversationStates.QUESTION_1,
						"Mam sześć prób. #próba kosztuje 100000 money. #potyczka kosztuje 250000 money. #łowca kosztuje 500000 money. #weteran kosztuje 1000000 money. #czempion kosztuje 2500000 money. #legenda kosztuje 5000000 money. Większa stawka oznacza więcej silniejszych przeciwników oraz trudniejsze fale. Wpisowe przepada po rozpoczęciu walki.",
						null);

				addTier(Arrays.asList("próba", "proba", "100000", "100k"),
						ChallengeArenaTier.TRIAL);
				addTier(Arrays.asList("potyczka", "250000", "250k"),
						ChallengeArenaTier.SKIRMISH);
				addTier(Arrays.asList("łowca", "lowca", "500000", "500k"),
						ChallengeArenaTier.HUNTER);
				addTier(Arrays.asList("weteran", "1000000", "1m"),
						ChallengeArenaTier.VETERAN);
				addTier(Arrays.asList("czempion", "2500000", "2m5"),
						ChallengeArenaTier.CHAMPION);
				addTier(Arrays.asList("legenda", "5000000", "5m"),
						ChallengeArenaTier.LEGEND);

				add(ConversationStates.ANY,
						Arrays.asList("rezygnuję", "rezygnuje", "poddaję", "poddaje"),
						null, ConversationStates.ATTENDING, null,
						new ForfeitChallengeArenaAction());
				add(ConversationStates.ANY,
						Arrays.asList("wyjdź", "wyjdz", "wychodzę", "wychodze"),
						null, ConversationStates.IDLE, null,
						new LeaveChallengeArenaAction());
				addKnownChatOptions("arena", "rezygnuję", "wyjdź");
			}

			private void addTier(final List<String> triggers,
					final ChallengeArenaTier tier) {
				add(ConversationStates.QUESTION_1, triggers, null,
						ConversationStates.ATTENDING, null,
						new StartChallengeArenaAction(tier));
			}
		};

		npc.setEntityClass("barracksbuyernpc");
		npc.setGender("M");
		npc.setPosition(x, y);
		npc.setDirection(Direction.DOWN);
		npc.setDescription("Oto Mistrz Wyzwań prowadzący arenę dla najodważniejszych wojowników ziem Kraka.");
		npc.initHP(100);
		npc.setPerceptionRange(100);
		zone.add(npc);
	}
}
