/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.entity.IEntity;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;

public class SlotWindowCharacterSessionTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
	}

	@Test
	public void repaintWhileCharacterIsClearedKeepsVisibility() {
		final IEntity oldOwner = createMock(IEntity.class);
		replay(oldOwner);
		assertTrue(SlotWindow.isCloseEnough(oldOwner, null, 100, 100));
		assertTrue(SlotWindow.isCloseEnough(null, "new_character", 100, 100));
		verify(oldOwner);
	}

	@Test
	public void ownInventoryRemainsOpenEvenWhenOldEntityIsFarAway() {
		final RPObject root = new RPObject();
		root.setRPClass("player");
		root.put("name", "Character");
		final IEntity owner = createMock(IEntity.class);
		expect(owner.getRPObject()).andReturn(root);
		replay(owner);
		assertTrue(SlotWindow.isCloseEnough(owner, "character", 100, 100));
		verify(owner);
	}

	@Test
	public void distanceChecksDoNotExpandCachedEntityBounds() {
		final Rectangle2D bounds = new Rectangle2D.Double(10, 10, 1, 1);
		final IEntity owner = createMock(IEntity.class);
		expect(owner.getRPObject()).andReturn(null).anyTimes();
		expect(owner.getArea()).andReturn(bounds).anyTimes();
		replay(owner);
		assertTrue(SlotWindow.isCloseEnough(owner, "character", 6, 6));
		for (int i = 0; i < 10; i++) {
			assertFalse(SlotWindow.isCloseEnough(owner, "character", 5, 5));
		}
		assertEquals(new Rectangle2D.Double(10, 10, 1, 1), bounds);
		verify(owner);
	}
}
