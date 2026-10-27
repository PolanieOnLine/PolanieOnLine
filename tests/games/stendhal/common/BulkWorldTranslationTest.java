package games.stendhal.common;

import static org.junit.Assert.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.*;

/** Reads actual zone attributes, including XML-normalized multiline signs. */
public class BulkWorldTranslationTest {
 private String find(String file, String source) throws Exception {
  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
  factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
  factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
  Document doc = factory.newDocumentBuilder().parse(new File("data/conf/zones/" + file));
  NodeList nodes = doc.getElementsByTagName("attribute");
  for (int i = 0; i < nodes.getLength(); i++) {
   Element node = (Element) nodes.item(i);
   if (source.equals(node.getTextContent().trim())) return node.getTextContent().trim();
  }
  fail("Missing zone fixture: " + source);
  return null;
 }

 @Test public void actualMultilineSemosSignHasEnglishDirections() throws Exception {
  String source = find("stendhal/semos.xml", "Północ: Stara kopalnia\nWschód: Młyn\nPołudnie: Miasto Semos");
  assertEquals("North: Old mine\nEast: Mill\nSouth: Semos City", Translation.getText("en", source));
  assertEquals(source, Translation.getText("pl", source));
 }

 @Test public void publicChestAndSceneryDescriptionsTranslate() throws Exception {
  assertEquals("Semos Public Chest", Translation.getText("en", find("stendhal/semos.xml", "Ogólnodostępna Skrzynia Semos")));
  assertEquals("The blacksmith heats iron in this forge until it glows.", Translation.getText("en",
   find("stendhal/semos.xml", "W tym palenisku kowal podgrzewa żelazo dopóki nie zacznie żarzyć.")));
 }

 @Test public void zakopaneWelcomeSignKeepsDirectionsAndLandmarkNames() throws Exception {
  String source = find("pol/zakopane.xml", "#'WITOJCIE W ZAKOPANEM!'\nNa północ ↑ gród Kraka\nNa wschód → dzielnica willowa\nNa zachód ← polany kościeliskie\nNa południe ↓ Tatry");
  assertEquals("#'WELCOME TO ZAKOPANE!'\nNorth ↑ Kraków\nEast → villa district\nWest ← Kościelisko glades\nSouth ↓ Tatry", Translation.getText("en", source));
  assertEquals(source, Translation.getText("pl", source));
 }

 @Test public void marketNoticesAndBookTextKeepAuthorAndNumbers() {
  assertEquals("Hungry? I can cook you a delicious fish soup. Visit me on the east side of the market!\n- Florence Boullabaisse -",
   Translation.getText("en", "Jesteś głodny? Mogę ugotować dla Ciebie pyszną zupę rybną. Odwiedź mnie we wschodniej części targowiska!\n- Florence Boullabaisse -"));
  assertEquals("NISHIYA'S SHEEP FARM\n\nBuy a sheep from Nishiya for the best price!",
   Translation.getText("en", "OWCZA FARMA NISHIYA\n\nKup owcę od Nishiya, aby dostać najlepszą cenę!"));
 }
}
