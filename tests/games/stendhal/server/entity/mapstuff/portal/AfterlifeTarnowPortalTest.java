package games.stendhal.server.entity.mapstuff.portal;

import static org.junit.Assert.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.*;
import org.w3c.dom.*;
import games.stendhal.server.core.config.factory.ConfigurableFactory;
import games.stendhal.server.core.config.factory.ConfigurableFactoryContext;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

/** Loads the real configured portal, not a duplicate of its access condition. */
public class AfterlifeTarnowPortalTest {
 private AccessCheckingPortal portal;
 private StendhalRPZone tarnow,afterlife;
 private Player player;
 @Before public void loadConfiguredPortal() throws Exception {
  StendhalRPWorld world=MockStendlRPWorld.get();
  tarnow=new StendhalRPZone("0_tarnow_city",128,128);world.addRPZone(tarnow);
  afterlife=new StendhalRPZone("int_afterlife",64,32);world.addRPZone(afterlife);
  Element configuration=findPortal("data/conf/zones/misc.xml","afterlife_tarnow_exit");
  Element implementation=(Element)configuration.getElementsByTagNameNS("*","implementation").item(0);
  Map<String,String> parameters=new HashMap<String,String>();
  NodeList entries=implementation.getElementsByTagNameNS("*","parameter");
  for(int i=0;i<entries.getLength();i++) {Element entry=(Element)entries.item(i);parameters.put(entry.getAttribute("name"),entry.getTextContent());}
  ConfigurableFactory factory=(ConfigurableFactory)Class.forName(implementation.getAttribute("class-name")+"Factory").newInstance();
  portal=(AccessCheckingPortal)factory.create(new ConfigurableFactoryContext(parameters));
  portal.setIdentifier("afterlife_tarnow_exit");
  portal.setPosition(Integer.parseInt(configuration.getAttribute("x")),Integer.parseInt(configuration.getAttribute("y")));
  Element destination=(Element)configuration.getElementsByTagNameNS("*","destination").item(0);
  portal.setDestination(destination.getAttribute("zone"),destination.getAttribute("ref"));afterlife.add(portal);
  Element target=findPortal("data/conf/zones/pol/tarnow.xml","tarnow_restart_life");
  Portal arrival=new OneWayPortalDestination();arrival.setIdentifier("tarnow_restart_life");
  arrival.setPosition(Integer.parseInt(target.getAttribute("x")),Integer.parseInt(target.getAttribute("y")));tarnow.add(arrival);
  player=PlayerTestHelper.createPlayer("TarnowTraveller");player.put("text","");
 }
 @Test public void savedVisitUnlocksThePortalWithoutLegacyQuestOrRevisiting() {
  player.setKeyedSlot("!visited","0_tarnow_city","1");
  assertFalse(player.hasQuest("0_tarnow_city"));assertTrue(portal.isAllowed(player));
 }
 @Test public void actualVisitUnlocksThePortal() {
  player.onAdded(tarnow);assertTrue(player.hasVisitedZone("0_tarnow_city"));
  assertTrue(portal.isAllowed(player));
 }
 @Test public void visitingADifferentTownDoesNotUnlockTarnow() {
  player.setKeyedSlot("!visited","0_semos_city","1");assertFalse(portal.isAllowed(player));
 }
 @Test public void legacyUnlockRemainsValid() {
  player.setQuest("0_tarnow_city","done");assertTrue(portal.isAllowed(player));
 }
 @Test public void returningVisitorCanActuallyTravelFromAfterlifeToTarnow() {
  player.setKeyedSlot("!visited","0_tarnow_city","1");afterlife.add(player);player.setPosition(20,7);
  assertTrue(portal.onUsed(player));assertSame(tarnow,player.getZone());
  assertEquals(50,player.getX());assertEquals(72,player.getY());
 }
 private Element findPortal(String file,String reference) throws Exception {
  DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();factory.setNamespaceAware(true);
  NodeList portals=factory.newDocumentBuilder().parse(new File(file)).getElementsByTagNameNS("*","portal");
  for(int i=0;i<portals.getLength();i++) {Element candidate=(Element)portals.item(i);if(reference.equals(candidate.getAttribute("ref")))return candidate;}
  throw new AssertionError("Missing configured portal "+reference);
 }
}
