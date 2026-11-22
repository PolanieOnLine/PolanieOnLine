/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.entity.mapstuff.useable;

import static org.junit.Assert.*;

import java.util.Collections;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import games.stendhal.common.constants.Events;
import games.stendhal.server.actions.UseAction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.tarnow.enchanter.EnchanterNPC;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** Exercises the actual configured table through the shared client use route. */
public class AffixWorkbenchTest {
	private StendhalRPZone zone;
	private Entity table;
	private Player player;
	private SpeakerNPC npc;

	@BeforeClass public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
		if (!RPClass.hasRPClass(Events.AFFIX_REROLL)) games.stendhal.server.events.AffixRerollEvent.generateRPClass();
	}

	@Before public void setup() throws Exception {
		SingletonRepository.getNPCList().remove(EnchanterNPC.NAME);
		zone = new StendhalRPZone(EnchanterNPC.ZONE, 16, 14);
		MockStendlRPWorld.get().addRPZone(zone);
		new EnchanterNPC().configureZone(zone, Collections.emptyMap());
		npc = SingletonRepository.getNPCList().get(EnchanterNPC.NAME);
		final NodeList zones = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				getClass().getClassLoader().getResourceAsStream("data/conf/zones/pol/tarnow.xml")).getElementsByTagName("zone");
		for (int i = 0; i < zones.getLength(); i++) {
			final Element configuredZone = (Element) zones.item(i);
			if (!EnchanterNPC.ZONE.equals(configuredZone.getAttribute("name"))) continue;
			final NodeList entities = configuredZone.getElementsByTagName("entity");
			for (int j = 0; j < entities.getLength(); j++) {
				final Element configuredEntity = (Element) entities.item(j);
				if (!"7".equals(configuredEntity.getAttribute("x")) || !"6".equals(configuredEntity.getAttribute("y"))) continue;
				final Element implementation = (Element) configuredEntity.getElementsByTagName("implementation").item(0);
				table = (Entity) Class.forName(implementation.getAttribute("class-name")).getDeclaredConstructor().newInstance();
			}
		}
		assertNotNull("The painted workbench needs its interaction entity", table);
		table.setPosition(7, 6);
		zone.add(table);
		player = PlayerTestHelper.createPlayer("affix_table_customer");
		player.setPosition(8, 7);
		zone.add(player);
	}

	@After public void cleanup() {
		SingletonRepository.getNPCList().remove(EnchanterNPC.NAME);
	}

	private int windows() {
		int count = 0;
		for (final RPEvent event : player.events()) if (Events.AFFIX_REROLL.equals(event.getName())) count++;
		return count;
	}

	private void click() {
		final RPAction action = new RPAction();
		action.put("zone", player.getZone().getName());
		action.put("target", "#" + table.getID().getObjectID());
		new UseAction().onAction(player, action);
	}

	@Test public void clickingEitherHalfOpensPrzemirsServiceWithoutConversation() {
		for (final int x : new int[] {7, 8}) {
			player.setPosition(x, 7);
			final int before = windows();
			click();
			assertEquals("Clicking the table must open a service window", before + 1, windows());
			RPEvent opened = null;
			for (final RPEvent event : player.events()) if (Events.AFFIX_REROLL.equals(event.getName())) opened = event;
			assertEquals("open", opened.get("phase"));
			assertEquals(npc.getID().getObjectID(), opened.getInt("npc_id"));
		}
	}

	@Test public void remoteUseCannotOpenAWindow() {
		player.setPosition(8, 10);
		click();
		assertEquals(0, windows());
	}

	@Test public void foreignZoneCannotUseTheTableEvenAtTheSameCoordinates() {
		zone.remove(player);
		final StendhalRPZone other = new StendhalRPZone("affix_table_other", 16, 14);
		MockStendlRPWorld.get().addRPZone(other);
		other.add(player);
		assertTrue(table instanceof UseListener);
		assertFalse(((UseListener) table).onUsed(player));
		assertEquals(0, windows());
	}

	@Test public void tableCannotBorrowPrzemirFromAnotherZone() {
		zone.remove(npc);
		final StendhalRPZone other = new StendhalRPZone("affix_table_foreign_npc", 16, 14);
		MockStendlRPWorld.get().addRPZone(other);
		other.add(npc);
		click();
		assertEquals(0, windows());
	}

	@Test public void tableCannotOpenWhenPrzemirIsMissingOrTooFarAway() {
		npc.setPosition(1, 1);
		click();
		assertEquals(0, windows());
		npc.setPosition(8, 5);
		SingletonRepository.getNPCList().remove(EnchanterNPC.NAME);
		click();
		assertEquals(0, windows());
	}

	@Test public void tableDoesNotAddCollisionOrPaintOverTheExistingMapArtwork() throws Exception {
		assertEquals(0, table.getResistance());
		assertEquals(2, table.getWidth(), 0);
		assertEquals(1, table.getHeight(), 0);
		final java.awt.image.BufferedImage sprite = javax.imageio.ImageIO.read(getClass().getClassLoader().getResource(
				"data/sprites/" + table.get("class") + "/" + table.get("name") + ".png"));
		assertEquals(64, sprite.getWidth());
		assertEquals(32, sprite.getHeight());
		for (int y = 0; y < 32; y++) for (int x = 0; x < 64; x++) assertEquals(0, sprite.getRGB(x, y) >>> 24);
	}
}
