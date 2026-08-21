/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.dragon_knights;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.quests.socialstatusrings.MieszczaninRepairMovementSync;
import games.stendhal.server.maps.quests.socialstatusrings.MieszczaninRepairStage;

/** Adds the inhabitants of the small settlement east of Dragon Knights. */
public class SettlementNPCs implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC dobrawa = buildDobrawa(zone);
		final SpeakerNPC stach = buildStach(zone);
		final SpeakerNPC zywia = buildZywia(zone);
		final SpeakerNPC milost = buildMilost(zone);
		MieszczaninRepairStage.attach(zone, dobrawa, stach, zywia, milost);
		MieszczaninRepairMovementSync.attach(zone);
	}

	private SpeakerNPC buildDobrawa(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Dobrawa") {
			@Override
			protected void createPath() {
				final List<Node> nodes = Arrays.asList(
						new Node(86, 72),
						new Node(89, 72),
						new Node(89, 75),
						new Node(86, 75));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj. Jeśli przybywasz traktem, rozgość się chwilę przy studni.");
				addJob("Doglądam gospodarstwa i pilnuję, żeby nikomu tutaj nie zabrakło tego, co najpotrzebniejsze.");
				addHelp("Jeśli czegoś szukasz, zapytaj mieszkańców. Każdy z nas zna ten trakt trochę inaczej.");
				addGoodbye("Niech droga będzie dla ciebie spokojna.");
			}
		};

		npc.setDescription("Oto Dobrawa. Sprawia wrażenie osoby, która zna wszystkie sprawy przysiółka.");
		npc.setEntityClass("woman_006_npc");
		npc.setGender("F");
		npc.setPosition(86, 72);
		zone.add(npc);
		return npc;
	}

	private SpeakerNPC buildStach(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Stach") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj. Tylko uważaj, żebym przez rozmowę nie chybił siekierą.");
				addJob("Naprawiam wozy, płoty i wszystko, co z drewna potrafi się rozpaść w najmniej odpowiedniej chwili.");
				addHelp("Jeśli coś na trakcie wymaga naprawy, zwykle prędzej czy później trafia właśnie do mnie.");
				addGoodbye("Wracam do roboty.");
			}
		};

		npc.setDescription("Oto Stach. Pracuje przy pniu z siekierą w dłoniach.");
		npc.setEntityClass("woodcutternpc");
		npc.setGender("M");
		npc.setPosition(69, 67);
		npc.setIdleDirection(Direction.LEFT);
		zone.add(npc);
		return npc;
	}

	private SpeakerNPC buildZywia(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Żywia") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj. Nie nadepnij na zioła, dopiero co je rozłożyłam.");
				addJob("Znam tutejsze zioła i opatruję tych, którym przydarzy się choroba albo nieszczęście.");
				addHelp("Najwięcej daje mi las, ale nie wszystko da się zastąpić ziołami. Czasem czekamy na dostawy z daleka.");
				addGoodbye("Zdrowia ci życzę.");
			}
		};

		npc.setDescription("Oto Żywia. Stoi między kamieniami i niewielką chatą.");
		npc.setEntityClass("woman_005_npc");
		npc.setGender("F");
		npc.setPosition(84, 60);
		npc.setIdleDirection(Direction.DOWN);
		zone.add(npc);
		return npc;
	}

	private SpeakerNPC buildMilost(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Miłost") {
			@Override
			protected void createDialog() {
				addGreeting("Siadaj przy ogniu, jeśli masz chwilę. Na stojąco droga bardziej ciąży w nogach.");
				addJob("Pomagam tam, gdzie akurat potrzeba rąk. Przy polu, przy sadzie, czasem przy wozach.");
				addHelp("Przy ognisku usłyszysz więcej o tym, co dzieje się na trakcie, niż przy niejednym stole w gospodzie.");
				addGoodbye("Do zobaczenia przy ogniu.");
			}
		};

		npc.setDescription("Oto Miłost. Spogląda w stronę wspólnego ogniska.");
		npc.setEntityClass("man_001_npc");
		npc.setGender("M");
		npc.setPosition(76, 70);
		npc.setIdleDirection(Direction.RIGHT);
		zone.add(npc);
		return npc;
	}
}
