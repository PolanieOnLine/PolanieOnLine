package games.stendhal.common;

import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.util.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Offer;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.semos.tavern.market.ShowOffersChatAction;
import games.stendhal.server.maps.semos.tavern.market.ShowOfferItemsChatAction;
import utilities.PlayerTestHelper;

public class MarketDisplayTranslationTest {
 @BeforeClass public static void setup(){MockStendlRPWorld.get();}
 @Test public void actualOfferRowsTranslateWhilePriceSellerAndCanonicalItemStayUnchanged() throws Exception {
  Player seller=PlayerTestHelper.createPlayer("Zamknij");
  StackableItem wood=(StackableItem)SingletonRepository.getEntityManager().getItem("polano");wood.setQuantity(7);
  Offer offer=new Offer(wood,987654321,seller);
  StringBuilder text=new StringBuilder();Map<String,Offer> rows=new HashMap<String,Offer>();
  Method build=ShowOffersChatAction.class.getDeclaredMethod("buildMessage",StringBuilder.class,List.class,Map.class,boolean.class);build.setAccessible(true);
  assertEquals(1,build.invoke(new ShowOffersChatAction(),text,Collections.singletonList(offer),rows,false));
  assertEquals("1: 7 pieces of wood for 987654321 coins\n",Translation.getText("en",text.toString()));
  assertEquals(text.toString(),Translation.getText("pl",text.toString()));
  assertSame(offer,rows.get("1"));assertEquals(987654321,offer.getPrice().intValue());
  assertEquals("Zamknij",offer.getOfferer());assertEquals("polano",wood.getName());assertEquals(7,wood.getQuantity());
 }
 @Test public void itemListTranslatesNamesWithoutChangingItsCanonicalSet() throws Exception {
  Set<String> names=new TreeSet<String>(Arrays.asList("polano","skórzany kirys"));
  Method build=ShowOfferItemsChatAction.class.getDeclaredMethod("buildItemListText",Set.class);build.setAccessible(true);
  String text=(String)build.invoke(new ShowOfferItemsChatAction(),names);
  assertEquals("I offer the following items: #wood, #'leather cuirass'. Say #'show <item name>' to see the price and quantity.",Translation.getText("en",text));
  assertTrue(names.contains("skórzany kirys"));assertFalse(names.contains("leather cuirass"));
 }
 @Test public void confirmationPreservesFeeAndBothGenderedSoldNotificationsTranslate() {
  assertEquals("Would you like to sell 7 pieces of wood for a total of 12345 coins? The commission is 123 coins.",
   Translation.getText("en","Czy chcesz sprzedać "+Grammar.quantityplnoun(7,"polano")+" za łącznie 12345 money? Będzie cię to kosztowało 123 money prowizji."));
  for(String name:new String[]{"skórzany kirys","tarcza drewniana"}) {
   String source=Grammar.genderNouns(name,"Twój")+" "+name+" "+Grammar.genderNouns(name,"został")+" "+Grammar.genderNouns(name,"sprzedany")+". Możesz przyjść do mnie po należne ci pieniądze.";
   assertTrue(source,Translation.getText("en",source).endsWith("has been sold. You can come and collect your money."));
  }
 }
 @Test public void expirationAndProlongationTranslateDurationsAndKeepLargePrices() {
  assertEquals("Your offer for leather cuirass has expired. You have 2 days to collect the item or extend the offer.",
   Translation.getText("en","Twoja oferta skórzany kirys wygasła. Masz 2 dni czasu aby odebrać przedmiot lub przedłużyć ofertę."));
  assertEquals("Would you like to extend your offer for leather cuirass at a price of 987654321 coins and a fee of 12345 coins?",
   Translation.getText("en","Czy chcesz przedłużyć swoją ofertę na skórzany kirys w cenie 987654321 i podatek 12345 money?"));
 }
 @Test public void boundItemNamesTranslateButTheirOwnersDoNot() {
  assertEquals("This leather cuirass can be used only by Zamknij. You cannot sell it.",
   Translation.getText("en","Ten skórzany kirys może być używany tylko przez Zamknij. Nie możesz go sprzedać."));
  assertEquals("This leather cuirass can be used only by you. You cannot sell it.",
   Translation.getText("en","Ten skórzany kirys może być używany tylko przez Ciebie. Nie możesz go sprzedać."));
 }
 @Test public void existingOfferTemplatesTranslateTheirInsertedItemDescriptions() {
  assertEquals("Would you like to remove your listing for 7 pieces of wood?",Translation.getText("en","Czy chcesz usunąć swoją ofertę na "+Grammar.quantityplnoun(7,"polano")+"?"));
  assertEquals("I prolonged your offer leather cuirass at a price of 123 coins and charged a fee of 7.",
   Translation.getText("en","Przedłużyłam twoją ofertę skórzany kirys w cenie 123 i pobrałam opłatę 7."));
 }
}
