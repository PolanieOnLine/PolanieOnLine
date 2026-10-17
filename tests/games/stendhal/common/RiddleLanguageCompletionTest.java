package games.stendhal.common;

import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.io.File;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.*;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Sentence;

public class RiddleLanguageCompletionTest {
 private Object riddles() throws Exception {
  Class<?> type = Class.forName("games.stendhal.server.maps.quests.SolveRiddles$Riddles");
  Constructor<?> constructor = type.getDeclaredConstructor();
  constructor.setAccessible(true);
  return constructor.newInstance();
 }
 private boolean matches(Object riddles, String question, String answer) throws Exception {
  Method method = riddles.getClass().getDeclaredMethod("matches", String.class, Sentence.class);
  method.setAccessible(true);
  return (Boolean) method.invoke(riddles, question, ConversationParser.parse(answer));
 }

 @Test public void reaperAcceptsEnglishAnswersWithoutRemovingPolishAnswers() throws Exception {
  Object riddles = riddles();
  String[][] cases = {
   {"Jaki rodzaj zwierząt jest różowy i wyrabia się z niego świeże kotlety i boczek?", "świnia", "pig"},
   {"Od razu rozpozna, czy śmierdzi, czy pachnie.", "nos", "nose"},
   {"O szyby dzwoni, gdy chmura łzy roni.", "deszcz", "rain"},
   {"Aby odpowiedzieć na tą zagadkę powtórz: trzy ślepe myszki, trzy ślepe myszki", "trzy ślepe myszki, trzy ślepe myszki", "three blind mice, three blind mice"}
  };
  for (String[] row : cases) {
   assertTrue("Polish: " + row[1], matches(riddles, row[0], row[1]));
   assertTrue("English: " + row[2], matches(riddles, row[0], row[2]));
   assertFalse(matches(riddles, row[0], "completely wrong answer"));
  }
 }

 @Test public void numericAnswersAndUnknownLegacyRiddlesKeepExistingRules() throws Exception {
  Object riddles = riddles();
  assertTrue(matches(riddles, "Ile to jest 2+2*2?", "6"));
  assertFalse(matches(riddles, "Ile to jest 2+2*2?", "8"));
  assertTrue(matches(riddles, "removed legacy question", "anything"));
 }

 @Test public void actualXmlQuestionsTranslateWhileAnswerLettersStayCanonical() throws Exception {
  Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("data/conf/woodcutter_test.xml"));
  NodeList entries = doc.getElementsByTagName("entry");
  assertEquals(10, entries.getLength());
  for (int i = 0; i < entries.getLength(); i++) {
   Element entry = (Element) entries.item(i);
   String question = entry.getAttribute("key");
   String english = Translation.getText("en", question);
   assertFalse("Missing question: " + question, question.equals(english));
   assertTrue(english, english.contains("a)") && english.contains("b)") && english.contains("c)"));
   assertTrue(english, english.endsWith("Choose the correct answer"));
   assertEquals(question, Translation.getText("pl", question));
   assertTrue(Arrays.asList("a", "b", "c").contains(entry.getTextContent()));
  }
 }

 @Test public void canonicalRiddleKeysAndAnswersAreNotReplacedByEnglish() throws Exception {
  Object riddles = riddles();
  Field mapField = riddles.getClass().getDeclaredField("riddleMap");
  mapField.setAccessible(true);
  Map<?, ?> map = (Map<?, ?>) mapField.get(riddles);
  String key = "O szyby dzwoni, gdy chmura łzy roni.";
  assertEquals(Collections.singletonList("deszcz"), map.get(key));
  assertEquals("It taps on the window when clouds shed tears. What is it?", Translation.getText("en", key));
 }

 @Test public void everyXmlRiddleHasAnEnglishQuestionAndAcceptsItsDisplayedAnswer() throws Exception {
  Object riddles = riddles();
  Field mapField = riddles.getClass().getDeclaredField("riddleMap");
  mapField.setAccessible(true);
  @SuppressWarnings("unchecked")
  Map<String, Collection<String>> map = (Map<String, Collection<String>>) mapField.get(riddles);
  for (Map.Entry<String, Collection<String>> entry : map.entrySet()) {
   String question = entry.getKey();
   assertFalse("Missing riddle: " + question, question.equals(Translation.getText("en", question)));
   for (String answer : entry.getValue()) {
    assertTrue(answer, matches(riddles, question, answer));
    assertTrue(answer, matches(riddles, question, Translation.getText("en", answer)));
   }
  }
 }
}
