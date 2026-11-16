/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.tarnow.enchanter;

import static org.junit.Assert.*;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.*;
import org.w3c.dom.*;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.config.zone.PortalSetupXMLReader;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** Loads the real maps and portal descriptors through the game engine. */
public class EnchanterWorkshopTest {
	@BeforeClass public static void initialize() throws Exception { QuestHelper.setUpBeforeClass(); }

	@Test public void configuredDoorAllowsEnteringAndLeavingWithoutLandingOnAPortal() throws Exception {
		final StendhalRPZone city = load("0_tarnow_city", "data/maps/Level 0/tarnow/city.tmx");
		final StendhalRPZone room = load(EnchanterNPC.ZONE, "data/maps/interiors/tarnow/enchanter_workshop.tmx");
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); factory.setNamespaceAware(true);
		final NodeList portals = factory.newDocumentBuilder().parse(getClass().getResourceAsStream("/data/conf/zones/pol/tarnow.xml")).getElementsByTagNameNS("*", "portal");
		for (int i = 0; i < portals.getLength(); i++) {
			final Element element = (Element) portals.item(i);
			if ("enchanter_workshop_entrance".equals(element.getAttribute("ref"))) new PortalSetupXMLReader().read(element).setup(city);
			if ("enchanter_workshop_exit".equals(element.getAttribute("ref"))) new PortalSetupXMLReader().read(element).setup(room);
		}
		SingletonRepository.getNPCList().remove(EnchanterNPC.NAME);
		new EnchanterNPC().configureZone(room, Collections.emptyMap());
		final Player player = PlayerTestHelper.createPlayer("workshop_visitor"); city.add(player); player.setPosition(55, 87);
		final Portal entrance = city.getPortal("enchanter_workshop_entrance");
		final Portal exit = room.getPortal("enchanter_workshop_exit");
		assertNotNull(entrance); assertNotNull(exit);
		assertEquals(56, entrance.getX()); assertEquals(87, entrance.getY());
		assertEquals(3, entrance.getOffset());
		assertTrue(entrance.onUsed(player)); assertSame(room, player.getZone());
		assertEquals(8, player.getX()); assertEquals(11, player.getY());
		assertTrue(exit.onUsed(player)); assertSame(city, player.getZone());
		assertEquals(55, player.getX()); assertEquals(87, player.getY());
		assertEquals(games.stendhal.common.Direction.LEFT, player.getDirection());
		SingletonRepository.getNPCList().remove(EnchanterNPC.NAME);
	}

	@Test public void minimapUsesTheWorkshopName() {
		final StendhalRPZone zone = new StendhalRPZone(EnchanterNPC.ZONE);
		assertEquals("Pracownia Przemira w Tarnowie", zone.getAttributes().get("readable_name"));
	}

	private StendhalRPZone load(final String id, final String map) throws Exception {
		final StendhalRPZone zone = new StendhalRPZone(id);
		zone.addCollisionLayer(id + ".collision", TMXLoader.load(map).getLayer("collision"));
		MockStendlRPWorld.get().addRPZone(zone);
		return zone;
	}
}
