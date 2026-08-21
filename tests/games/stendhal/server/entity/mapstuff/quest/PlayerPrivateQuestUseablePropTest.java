/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.quest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.BlockTestHelper;

public class PlayerPrivateQuestUseablePropTest {

	@BeforeClass
	public static void beforeClass() {
		BlockTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void onlyOwnerCanUsePropFromNearby() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		owner.setPosition(10, 10);
		stranger.setPosition(10, 10);

		final TestProp prop = new TestProp(owner);
		prop.setPosition(11, 10);

		assertFalse(prop.onUsed(stranger));
		assertFalse(prop.used);
		assertTrue(prop.onUsed(owner));
		assertTrue(prop.used);
	}

	@Test
	public void ownerCannotUsePropFromFarAway() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		owner.setPosition(1, 1);

		final TestProp prop = new TestProp(owner);
		prop.setPosition(20, 20);

		assertFalse(prop.onUsed(owner));
		assertFalse(prop.used);
	}

	@Test
	public void useablePropKeepsBlockRpClassAndPublishesRecognizedUseAction() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final TestProp prop = new TestProp(owner);

		assertEquals("block", prop.getRPClass().getName());
		assertEquals("block", prop.get("type"));
		assertEquals(PlayerPrivateQuestUseableProp.USEABLE_ENTITY_CLASS, prop.get("class"));
		assertEquals("Użyj", prop.get("menu"));
	}

	private static final class TestProp extends PlayerPrivateQuestUseableProp {
		private boolean used;

		TestProp(final Player owner) {
			super(owner, "item/pot/crate_small", 0, 1, true);
		}

		@Override
		protected boolean onUsedByOwner(final Player player) {
			used = true;
			return true;
		}
	}
}
