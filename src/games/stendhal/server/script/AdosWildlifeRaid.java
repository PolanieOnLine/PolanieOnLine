package games.stendhal.server.script;


import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.PlayerList;
import games.stendhal.server.core.engine.StendhalRPRuleProcessor;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.scripting.ScriptingNPC;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.mapstuff.portal.OneWayPortalDestination;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;

public class AdosWildlifeRaid implements TurnListener {

	private int turnCounter = 0;
	private Portal portal;
	private OneWayPortalDestination portalDestination;


	/**
	 * Creates a soldier
	 *
	 * @param zone zone
	 * @param name Name of the NPC
	 * @param x x-postion
	 * @param y y-postion
	 */
	public void createSoldier(StendhalRPZone zone, String name, int x, int y) {
		ScriptingNPC npc = new ScriptingNPC(name);
		npc.setEntityClass("youngsoldiernpc");
		npc.setHP((int) (Math.random() * 80) + 10);
		npc.setPosition(x, y);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

	/**
	 * Creates three soldiers to block the entrance
	 *
	 * @param zone zone
	 */
	public void createSoldiers(StendhalRPZone zone) {

		// main entrance
		createSoldier(zone, "Żołnierz", 55, 47);
		createSoldier(zone, "Żołnierz", 56, 47);
		createSoldier(zone, "Żołnierz", 57, 47);

		// backdoor
		createSoldier(zone, "Żołnierz", 43, 23);
	}

	/**
	 * Creates a sheep for the Orcs to target
	 *
	 * @param zone zone
	 */
	public void createSheep(StendhalRPZone zone) {
		Creature creature = new Sheep();
		creature.setPosition(56, 46);
		zone.add(creature);
	}

	@Override
	public void onTurnReached(int currentTurn) {
		int wait = 6;
		switch (turnCounter) {

			case 0:
				shout("Katinka krzyczy: Pomocy. Zbliżają się dwa Orki do naszego Schroniska dla zwierząt");
				wait = 5 * 3;
				break;

			case 1:
				shout("Dowódca Żołnierzy krzyczy: Katinka uspokój się.");
				break;

			case 2:
				shout("Dowódca Żołnierzy krzyczy: Wyślę tobie jednego z moich żołnierzy zwanego Marcus, aby ci pomógł.");
				wait = 60 * 3;
				break;

			case 3:
				shout("Marcus krzyczy: Zabiłem te dwa Orki, ale dalsze dochodzenie wykazało:");
				break;

			case 4:
				shout("Marcus krzyczy: Byli tylko zwiadem dużej zgrai wojowniczych Orków.");
				break;

			case 5:
				shout("Marcus krzyczy: Potrzebujemy posiłków w ciągu 10 minut.");
				wait = 10 * 3;
				break;

			case 6:
				shout("Io Flotto krzyczy: Utworzyłam portal obok Carmen w południowo-zachodniej części Semos.");
				break;

			case 7:
				shout("Io Flotto krzyczy: Możesz z niego skorzystać, aby na czas dostać się do Schroniska dla zwierząt w Ados.");
				wait = 120 * 3;
				break;

			case 8:
				shout("Katinka krzyczy: Argh! Jedzą nasze dziki. Pomóżcie nam!");
				// shout("Dr. Feelgood krzyczy: Pomocy! Pomóżcie nam! Schronisko dla zwierząt w Ados jest pod silnym atakiem zgrai głodnych wojowniczych Orków.");
				wait = 600 * 3;
				break;

			case 9:
				// remove the portal
				StendhalRPWorld.get().remove(portal.getID());
				StendhalRPWorld.get().remove(portalDestination.getID());
				return;

		}
		TurnNotifier.get().notifyInTurns(wait, this);
		turnCounter++;
	}

	public void createPortal() {
		StendhalRPZone zone1 = (StendhalRPZone) StendhalRPWorld.get().getRPZone(new IRPZone.ID("0_semos_city"));
		StendhalRPZone zone2 = (StendhalRPZone) StendhalRPWorld.get().getRPZone(new IRPZone.ID("0_ados_outside_nw"));

		portal = new Portal();
		zone1.assignRPObjectID(portal);
		portal.setPosition(9, 41);
		portal.setDestination("0_ados_outside_nw", "wildlife");
		zone1.add(portal);

		portalDestination = new OneWayPortalDestination();
		zone2.assignRPObjectID(portalDestination);
		portalDestination.setPosition(53, 108);
		portalDestination.setIdentifier("wildlife");
		zone2.add(portalDestination);
	}

	public void shout(String text) {
		PlayerList players = StendhalRPRuleProcessor.get().getOnlinePlayers();
		for (Player player : players.getAllPlayers()) {
			player.sendPrivateText(text);
		}
	}


	public void addToWorld() {
		StendhalRPZone zone = StendhalRPWorld.get().getZone("0_ados_outside_nw");
		createSoldiers(zone);
		createSheep(zone);
		createPortal();
		TurnNotifier.get().notifyInTurns(0, this);
	}


}
