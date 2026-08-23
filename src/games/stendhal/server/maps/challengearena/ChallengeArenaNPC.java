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

/** Creates the master of the Challenge Arena in Tarnow. */
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
				addGreeting("Witaj wojowniku. W Tarnowie sam wybierasz stawkę i trudność walki. Zapytaj mnie o #arenę.");
				addJob("Prowadzę Arenę Wyzwań w Tarnowie.");
				addHelp("Powiedz #arena aby wybrać stawkę. Po wybraniu próby powiedz #wejście. O swoje dotychczasowe osiągnięcia zapytaj mówiąc #wyniki. Podczas walki możesz powiedzieć #rezygnuję. Wpisowe wtedy przepada.");
				addGoodbye("Wróć gdy będziesz gotowy na kolejną walkę.");

				add(ConversationStates.ATTENDING,
						Arrays.asList("arena", "arenę", "wyzwania"),
						null, ConversationStates.QUESTION_1,
						"Mam sześć prób. #próba kosztuje 100000 sztuk złota. #potyczka kosztuje 250000 sztuk złota. #łowca kosztuje 500000 sztuk złota. #weteran kosztuje 1000000 sztuk złota. #czempion kosztuje 2500000 sztuk złota. #legenda kosztuje 5000000 sztuk złota. Większa stawka oznacza więcej silniejszych przeciwników oraz trudniejsze fale.",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("wyniki", "wynik", "rekord"),
						null, ConversationStates.ATTENDING, null,
						new ShowChallengeArenaStatsAction());

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

				add(ConversationStates.QUESTION_2,
						Arrays.asList("wejście", "wejscie", "wchodzę", "wchodze",
								"wejście na arenę", "wejscie na arene"),
						null, ConversationStates.QUESTION_2, null,
						new StartSelectedChallengeArenaAction());

				add(ConversationStates.ANY,
						Arrays.asList("rezygnuję", "rezygnuje", "poddaję", "poddaje"),
						null, ConversationStates.ATTENDING, null,
						new ForfeitChallengeArenaAction());
				add(ConversationStates.ANY,
						Arrays.asList("wyjdź", "wyjdz", "wyjście", "wyjscie",
								"wychodzę", "wychodze", "opuścić", "opuscic"),
						null, ConversationStates.IDLE, null,
						new LeaveChallengeArenaAction());
				addKnownChatOptions("arena", "wyniki", "wejście", "rezygnuję", "wyjdź");
			}

			private void addTier(final List<String> triggers,
					final ChallengeArenaTier tier) {
				add(ConversationStates.QUESTION_1, triggers, null,
						ConversationStates.QUESTION_2, null,
						new SelectChallengeArenaTierAction(tier));
			}
		};

		npc.setEntityClass("barracksbuyernpc");
		npc.setGender("M");
		npc.setPosition(x, y);
		npc.setDirection(Direction.DOWN);
		npc.setDescription("Oto Mistrz Wyzwań prowadzący tarnowską arenę dla najodważniejszych wojowników.");
		npc.initHP(100);
		npc.setPerceptionRange(100);
		zone.add(npc);
	}
}
