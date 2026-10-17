package games.stendhal.server.maps.magic.bricabrac;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import games.stendhal.common.Translation;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.SpeakerNPCTestHelper;

public class BuyerNpcLanguageLinksTest {
 private SpeakerNPC npc;
 @Before public void createActualNpc() {
  MockStendlRPWorld.get();
  new BuyerNPC().configureZone(new StendhalRPZone("vonda_language_links",40,30),Collections.<String,String>emptyMap());
  npc=SingletonRepository.getNPCList().get("Vonda");
 }
 @After public void removeNpc() {SingletonRepository.getNPCList().remove("Vonda");}
 @Test public void englishDressLinkReachesTheDressReply() {
  npc.getEngine().setCurrentState(ConversationStates.ATTENDING);
  npc.getEngine().step(PlayerTestHelper.createPlayer("Witaj."),Translation.getOriginalKeyword("en","dress"));
  String reply=SpeakerNPCTestHelper.getReply(npc);
  assertNotNull("English dress link should produce a reply",reply);
  assertTrue(reply,reply.startsWith("Kocham tę piękną różową sukienkę."));
 }
 @Test public void englishSewingMachineLinkReachesTheMachineReply() {
  npc.getEngine().setCurrentState(ConversationStates.ATTENDING);
  npc.getEngine().step(PlayerTestHelper.createPlayer("Witaj."),Translation.getOriginalKeyword("en","sewing machine"));
  String reply=SpeakerNPCTestHelper.getReply(npc);
  assertNotNull("English sewing-machine link should produce a reply",reply);
  assertTrue(reply,reply.startsWith("To jest moja ulubiona rzecz."));
 }
}
