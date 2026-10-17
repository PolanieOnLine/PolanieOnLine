/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.imageviewer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.swing.JTable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;

public class ItemListImageViewerEventTest {
	private Field preferences;
	private Object originalPreferences;

	@Before public void isolateLanguage() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		originalPreferences = preferences.get(WtWindowManager.getInstance());
		preferences.set(WtWindowManager.getInstance(), new Properties());
	}

	@After public void restoreLanguage() throws Exception {
		preferences.set(WtWindowManager.getInstance(), originalPreferences);
	}

	@Test public void englishCatalogueTranslatesHeaders() throws Exception {
		WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY, "en");
		if (!marauroa.common.game.RPClass.hasRPClass("show_item_list")) {
			games.stendhal.server.events.ShowItemListEvent.generateRPClass();
		}
		RPEvent event = new RPEvent("show_item_list");
		event.setOwner(new RPObject());
		event.addSlot("content");
		ItemListImageViewerEvent view = new ItemListImageViewerEvent(event);
		Method createTable = ItemListImageViewerEvent.class.getDeclaredMethod("createTable", int.class);
		createTable.setAccessible(true);
		JTable table = (JTable) createTable.invoke(view, 600);
		assertEquals("Item", table.getColumnName(0));
		assertEquals("Price", table.getColumnName(1));
		assertEquals("Description", table.getColumnName(2));
	}

	@Test public void englishDescriptionTranslatesStatsAndUnderlinesActualName() throws Exception {
		WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY, "en");
		String html = description("sok z chmielu", "Oto sok z chmielu. Najlepszy na upalne dni. Parametry (PZ: 6 CYKL: 5s REGEN: 1).");
		assertTrue(html, html.contains("<u>beer</u>"));
		assertTrue(html, html.contains("Stats (HP: 6 INTERVAL: 5s REGEN: 1)."));
		assertFalse(html, html.contains("Parametry"));
	}

	@Test public void polishDescriptionUnderlinesNameAndEscapesHtml() throws Exception {
		String html = description("piwo & <lager>", "Oto piwo & <lager>. Parametry (PZ: 6).");
		assertTrue(html, html.contains("<u>piwo &amp; &lt;lager&gt;</u>"));
		assertTrue(html, html.contains("Parametry (PZ: 6)."));
		assertFalse(html, html.contains("<lager>"));
	}

	@Test public void existingMarkerDoesNotDuplicateUnderline() throws Exception {
		String html = description("piwo", "Oto §'piwo'. Parametry (PZ: 6).");
		assertTrue(html, html.contains("Oto <u>piwo</u>."));
		assertFalse(html, html.contains("<u><u>"));
	}

	@Test public void englishDescriptionTranslatesAnAlreadyMarkedCanonicalName() throws Exception {
		WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY, "en");
		String html = description("sok z chmielu", "Oto §'sok z chmielu'. Najlepszy na upalne dni. Parametry (PZ: 6 CYKL: 5s REGEN: 1).");
		assertTrue(html, html.contains("You see <u>beer</u>. Perfect for hot days."));
	}

	@Test public void englishCatalogueTranslatesEquipmentStatisticsWithoutChangingValues() throws Exception {
		WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY, "en");
		String html = description("sok z chmielu", "Oto sok z chmielu. Najlepszy na upalne dni. Parametry (ATK: 12 OBR: 34 STR: 5 ZASIĘG: 6).");
		assertTrue(html, html.contains("Stats (ATK: 12 DEF: 34 RATK: 5 RANGE: 6)."));
	}

	@Test public void englishCatalogueTranslatesSingleAndDoubleConsumableStatistics() throws Exception {
		WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY, "en");
		assertTrue(description("piwo", "Oto piwo. Parametry (PZ: 6).").contains("Stats (HP: 6)."));
		assertTrue(description("piwo", "Oto piwo. Parametry (PZ: 6 CYKL: 5s).").contains("Stats (HP: 6 INTERVAL: 5s)."));
	}

	@Test public void statisticsParserLeavesUnknownLabelsAndAuthoredTextAlone() throws Exception {
		WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY, "en");
		String html = description("item", "CUSTOM-OBR: 90. Parametry (OBR: 34 CUSTOM-OBR: 12 CUSTOM_OBR: 9 opis: 7).");
		assertTrue(html, html.contains("CUSTOM-OBR: 90. Stats (DEF: 34 CUSTOM-OBR: 12 CUSTOM_OBR: 9 opis: 7)."));
	}

	private String description(String name, String text) throws Exception {
		RPObject item = new RPObject();
		item.put("name", name);
		item.put("class", "drink");
		item.put("subclass", "beer");
		item.put("description_info", text);
		ItemListImageViewerEvent view = new ItemListImageViewerEvent(new RPEvent("show_item_list"));
		Method row = ItemListImageViewerEvent.class.getDeclaredMethod("createDataRow", RPObject.class, int.class);
		row.setAccessible(true);
		return (String) ((Object[]) row.invoke(view, item, 600))[2];
	}

	@Test
	public void testRarityColorsHighlightedItemName() {
		assertEquals("before <u><font color=\"#ff8c00\">sword</font></u> after",
				ItemListImageViewerEvent.colorHighlightedItemName(
						"before <u>sword</u> after", ItemRarity.LEGENDARY));
	}

	@Test
	public void testMissingRarityLeavesDescriptionUnchanged() {
		assertEquals("before <u>sword</u> after",
				ItemListImageViewerEvent.colorHighlightedItemName(
						"before <u>sword</u> after", null));
	}

	@Test
	public void testDescriptionWithoutHighlightedNameIsSafe() {
		assertEquals("plain description",
				ItemListImageViewerEvent.colorHighlightedItemName(
						"plain description", ItemRarity.RARE));
	}
}
