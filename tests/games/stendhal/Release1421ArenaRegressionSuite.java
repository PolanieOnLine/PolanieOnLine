/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import games.stendhal.server.core.rule.rarity.ItemRarityServiceTest;
import games.stendhal.server.entity.creature.EliteCreatureDropRarityTest;
import games.stendhal.server.entity.npc.behaviour.impl.ValueBuyerBehaviourTest;
import games.stendhal.server.entity.player.ReservePouchMigrationTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaCreatureSpawnerTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaEngineLootPeriodTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaLootServiceTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaNPCTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaInstanceTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaManagerTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaInfoTest;
import games.stendhal.client.gui.j2d.entity.Item2DViewGroundRarityTest;
import games.stendhal.server.script.SummonMistCreatureTest;

/** Focused regression suite shared by the 1.42.1 patch and master. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ItemRarityServiceTest.class,
	EliteCreatureDropRarityTest.class,
	ValueBuyerBehaviourTest.class,
	ReservePouchMigrationTest.class,
	ChallengeArenaCreatureSpawnerTest.class,
	ChallengeArenaEngineLootPeriodTest.class,
	ChallengeArenaLootServiceTest.class,
	ChallengeArenaNPCTest.class,
	ChallengeArenaInstanceTest.class,
	ChallengeArenaManagerTest.class,
	ChallengeArenaInfoTest.class,
	Item2DViewGroundRarityTest.class,
	SummonMistCreatureTest.class
})
public class Release1421ArenaRegressionSuite {
	// JUnit suite marker.
}
