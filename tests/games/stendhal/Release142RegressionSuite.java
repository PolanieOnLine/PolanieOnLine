/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import games.stendhal.client.gui.ItemRarityPresentationTest;
import games.stendhal.server.actions.admin.EliteSummonActionTest;
import games.stendhal.server.core.events.seasonal.EasterEventPlanTest;
import games.stendhal.server.core.events.seasonal.MineTownConstructionEventPlanTest;
import games.stendhal.server.core.events.seasonal.MineTownEventPlanTest;
import games.stendhal.server.core.events.seasonal.SeasonalZonePlanTest;
import games.stendhal.server.core.rp.group.GroupExperienceDistributorTest;
import games.stendhal.server.core.rule.damage.CriticalHitServiceTest;
import games.stendhal.server.core.rule.rarity.ItemAffixDropIntegrationTest;
import games.stendhal.server.core.rule.rarity.ItemAffixGeneratorTest;
import games.stendhal.server.core.rule.rarity.ItemAffixSeedTest;
import games.stendhal.server.core.rule.rarity.ItemAffixStateTest;
import games.stendhal.server.core.rule.rarity.ItemRarityServiceTest;
import games.stendhal.server.entity.RPEntityGroupExperienceTest;
import games.stendhal.server.entity.RPEntityWeaponDamageRollTest;
import games.stendhal.server.entity.item.ChallengeArenaRewardChestTest;
import games.stendhal.server.entity.status.BleedingAttackerFactoryTest;
import games.stendhal.server.entity.status.BleedingAttackerTest;
import games.stendhal.server.entity.status.BleedingStatusTest;
import games.stendhal.server.entity.status.StunnedStatusTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaManagerTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaModifierTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaStateTest;
import games.stendhal.server.maps.challengearena.ChallengeArenaTierTest;

/**
 * Fast, explicit regression suite for the highest-risk changes scheduled for
 * release 1.42. The full {@code ant test} suite remains the final CI gate.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ItemRarityPresentationTest.class,
	ItemRarityServiceTest.class,
	ItemAffixStateTest.class,
	ItemAffixSeedTest.class,
	ItemAffixGeneratorTest.class,
	ItemAffixDropIntegrationTest.class,
	EliteSummonActionTest.class,
	StunnedStatusTest.class,
	BleedingStatusTest.class,
	BleedingAttackerTest.class,
	BleedingAttackerFactoryTest.class,
	RPEntityWeaponDamageRollTest.class,
	CriticalHitServiceTest.class,
	GroupExperienceDistributorTest.class,
	RPEntityGroupExperienceTest.class,
	ChallengeArenaTierTest.class,
	ChallengeArenaStateTest.class,
	ChallengeArenaManagerTest.class,
	ChallengeArenaModifierTest.class,
	ChallengeArenaRewardChestTest.class,
	SeasonalZonePlanTest.class,
	EasterEventPlanTest.class,
	MineTownEventPlanTest.class,
	MineTownConstructionEventPlanTest.class
})
public class Release142RegressionSuite {
	// JUnit suite marker.
}
