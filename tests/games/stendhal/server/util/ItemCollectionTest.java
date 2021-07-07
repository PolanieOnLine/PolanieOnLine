/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import games.stendhal.server.entity.npc.ConversationPhrases;

/**
 * Tests for the area class.
 *
 * @author M. Fuchs
 */
public class ItemCollectionTest {

	@Test
	public void testCreateArea() {
	    final ItemCollection coll = new ItemCollection();
	    assertEquals("", coll.toStringForQuestState());
        assertEquals(ConversationPhrases.EMPTY, coll.toStringList());

	    coll.addItem("ser", 5);
	    assertEquals("ser=5", coll.toStringForQuestState());

	    coll.addFromQuestStateString("ser=2;szynka=3");
        assertEquals("ser=7;szynka=3", coll.toStringForQuestState());

        assertTrue(coll.removeItem("ser", 1));
        assertEquals("ser=6;szynka=3", coll.toStringForQuestState());
        assertEquals(Arrays.asList("6 pieces of cheese", "3 pieces of ham"), coll.toStringList());
        assertEquals(Arrays.asList("6 #'pieces of cheese'", "3 #'pieces of ham'"), coll.toStringListWithHash());

        assertFalse(coll.removeItem("szynka", 5));
        assertEquals("ser=6;szynka=3", coll.toStringForQuestState());

        assertTrue(coll.removeItem("ser", 6));
        assertEquals("szynka=3", coll.toStringForQuestState());

        assertTrue(coll.removeItem("szynka", 3));
        assertEquals("", coll.toStringForQuestState());

        coll.addItem("spodnie cieni", 1);
        assertEquals("spodnie cieni=1",coll.toStringForQuestState());
        assertTrue(coll.removeItem("spodnie cieni",1));
        assertEquals("", coll.toStringForQuestState());
	}

	@Test
	public void testAddFromQuestStateString() {
		final ItemCollection coll = new ItemCollection();
		assertEquals("", coll.toStringForQuestState());
		coll.addFromQuestStateString("ser=6;szynka=3;spodnie cieni=1");
		assertEquals("ser=6;szynka=3;spodnie cieni=1", coll.toStringForQuestState());
		assertTrue(coll.removeItem("spodnie cieni",1));
		assertEquals("ser=6;szynka=3", coll.toStringForQuestState());
	}

}
