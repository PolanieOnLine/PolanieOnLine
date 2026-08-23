/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.Arrays;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.util.Area;

/** Creates the in-world master used to start paid Challenge Arena runs. */
public final class ChallengeArenaNPC {
	private ChallengeArenaNPC() {
	}

	public static void create(final StendhalRPZone zone, final Area arena,
			final Spot entrance, final int x, final int y) {
		final ChallengeArenaInfo arenaInfo = new ChallengeArenaInfo(arena, zone,
				entrance);

		final SpeakerNPC npc = new SpeakerNPC("Mistrz Wyzwań") {
			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wojowniku. Jeśli zwykły Deathmatch to za mało zapytaj mnie o #arenę.");
				addJob("Prowadzę Arenę Wyzwań dla wojowników którzy chcą postawić własne pieniądze na trudniejszą walkę.");
				addHelp("Powiedz #arena. Wybierzesz stawkę a ona określi liczbę i siłę przeciwników.");
				addGoodbye("Wróć gdy będziesz gotowy na prawdziwe wyzwanie.");

				add(ConversationStates.ATTENDING,
						Arrays.asList("arena", "arenę", "wyzwania"),
						null, ConversationStates.QUESTION_1,
						"Możesz postawić 100000, 250000, 500000, 1000000, 2500000 albo 5000000 money. Większa stawka oznacza więcej silniejszych przeciwników oraz trudniejsze fale. Wpisowe przepada po rozpoczęciu walki. Wybierz kwotę.",
						null);

				addTier("100000", "100k", ChallengeArenaTier.TRIAL);
				addTier("250000", "250k", ChallengeArenaTier.SKIRMISH);
				addTier("500000", "500k", ChallengeArenaTier.HUNTER);
				addTier("1000000", "1m", ChallengeArenaTier.VETERAN);
				addTier("2500000", "2m5", ChallengeArenaTier.CHAMPION);
				addTier("5000000", "5m", ChallengeArenaTier.LEGEND);
			}

			private void addTier(final String amount, final String shortName,
					final ChallengeArenaTier tier) {
				add(ConversationStates.QUESTION_1,
						Arrays.asList(amount, shortName), null,
						ConversationStates.ATTENDING, null,
						new StartChallengeArenaAction(arenaInfo, tier));
			}
		};

		npc.setEntityClass("darkwizardnpc");
		npc.setPosition(x, y);
		npc.setDirection(Direction.DOWN);
		npc.setDescription("Oto Mistrz Wyzwań. Przyjmuje wysokie stawki za walki na Arenie Wyzwań.");
		npc.initHP(100);
		npc.setPerceptionRange(7);
		zone.add(npc);
	}
}
