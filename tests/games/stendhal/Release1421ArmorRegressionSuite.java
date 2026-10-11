/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import games.stendhal.client.gui.GroundItemTooltipTest;
import games.stendhal.client.gui.j2d.entity.CreatureArmorRendererTest;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionServiceTest;
import games.stendhal.server.entity.creature.CreatureArmorTest;

/** Shared UI, public-data and unchanged-combat checks for patch 1.42.1 and master. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	CreatureArmorTest.class,
	CreatureArmorRendererTest.class,
	GroundItemTooltipTest.class,
	WeaponArmorInteractionServiceTest.class
})
public class Release1421ArmorRegressionSuite {
	// JUnit suite marker.
}
