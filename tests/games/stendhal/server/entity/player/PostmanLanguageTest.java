package games.stendhal.server.entity.player;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.ChatMessage;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class PostmanLanguageTest {
 @BeforeClass public static void setup(){MockStendlRPWorld.get();}
 @Test public void polishPlayerLetterKeepsTheExistingPresentationAndOriginalBody() {
  Player player=PlayerTestHelper.createPlayer("Recipient");player.setClientVersion("1.0");player.setLanguage("pl");
  String body="Witaj.\nZamknij {0} $FEE";
  new ReadPostmanMessages().deliverMessages(player,Collections.singletonList(new ChatMessage("Witaj.",body,"2026-10-16 12:34:56","P")));
  assertEquals("postman powiedział Tobie: 2026-10-16 12:34 Witaj. poprosił mnie o dostarczenie tej wiadomości dla Ciebie: \n"+body,player.events().get(0).get("text"));
  assertEquals("PRIVMSG",player.events().get(0).get("texttype"));
  assertEquals("postman",player.getLastPrivateChatter());
 }
 @Test public void playerTextIsNeverTranslatedEvenIfItMatchesAnNpcMessage() {
  String body="Twoja oferta skórzany kirys została usunięta z rynku.\nWitaj.";
  Player player=deliver("P","Witaj.{2} $FEE",body,"2026-10-16 12:34:56");
  assertEquals("postman tells you: 2026-10-16 12:34 Witaj.{2} $FEE asked me to deliver this message to you: \n"+body,player.events().get(0).get("text"));
  assertEquals("postman",player.getLastPrivateChatter());
 }
 @Test public void authoredNpcMailTranslatesAtDeliveryAndKeepsSavedMessageCanonical() {
  String body="Twoja oferta skórzany kirys została usunięta z rynku.";
  ChatMessage message=new ChatMessage("Harold",body,"2026-10-16 12:34:56","N");
  Player player=englishPlayer();new ReadPostmanMessages().deliverMessages(player,Collections.singletonList(message));
  assertEquals("postman tells you: 2026-10-16 12:34 Harold asked me to deliver this message to you: \nYour offer for leather cuirass has been removed from the market.",player.events().get(0).get("text"));
  assertEquals(body,message.getMessage());assertEquals("N",message.getMessagetype());
 }
 @Test public void supportWrapperTranslatesButTheReplyAndAdminNameStayVerbatim() {
  String reply="Witaj.\nZamknij {0} $FEE";
  Player player=deliver("S","Admin{2}","W odpowiedzi na twoje pytanie:\n"+reply+" \nJeżeli chcesz odpowiedzieć użyj /support.","2010-07-20 00:00:00");
  assertEquals("postman tells you: unknown date Admin{2} asked me to deliver this message to you: \nIn reply to your question:\n"+reply+" \nTo reply, use /support.",player.events().get(0).get("text"));
  assertEquals("SUPPORT",player.events().get(0).get("texttype"));
 }
 @Test public void unknownKindsKeepTheBodyUnchangedRatherThanAssumingItIsAuthored() {
  Player player=deliver("X","Sender","Witaj.","2026-10-16 12:34:56");
  assertTrue(player.events().get(0).get("text").endsWith("\nWitaj."));
 }
 private Player englishPlayer(){Player player=PlayerTestHelper.createPlayer("Recipient");player.setLanguage("en");player.setClientVersion("1.0");return player;}
 private Player deliver(String kind,String source,String body,String timestamp){
  Player player=englishPlayer();new ReadPostmanMessages().deliverMessages(player,Collections.singletonList(new ChatMessage(source,body,timestamp,kind)));return player;
 }
}
