package games.stendhal.common;

import static org.junit.Assert.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;

/** Clickable English topic groups must retain the canonical NPC command order. */
public class BulkNpcTopicMarkupTest {
 private static final String[][] CASES = {
  {"en-quests.txt", "Wyjdziesz z tego budynku", "(right on the map)|(right on the map)|BANK", "(prawo na mapie)|(prawo na mapie)|BANK"},
  {"en-quests-dialogue-4.txt", "Mama powiedziała, że", "playthings|toys", "zabawki|toys"},
  {"en-quests-rescue.txt", "Poczekaj, hm...", "hostess|Maryśki", "gaździny|Maryśki"},
  {"en-quests-soldiers.txt", "Mapa #skarbów", "treasure|kingdom of the dwarves", "skarbów|królestwa krasnali"},
  {"en-quests-crystals.txt", "Dziękuję. Chciałbym zdobyć pięć", "emotion crystals", "kryształów emocji"},
  {"en-quests-antivenom.txt", "To nie pytanie typu", "yes|no", "tak|nie"},
  {"en-quests-mz.txt", "Dzięki, potrzebuję, abyś przyniósł mi 20", "traps|rodents", "pułapek|gryzonie"},
  {"en-quests-mz.txt", "Nie ukończyłeś testu praktycznego.", "start|wizards", "rozpocząć|czarodziejach"},
  {"en-npcs-ados.txt", "Możesz wejść na #prom", "ferry|status|board", "prom|status|wejdź"},
  {"en-npcs-ados.txt", "Mam nadzieje, że spodoba", "Deathmatch", "Deathmatch"},
  {"en-npcs-ados.txt", "Mój ojciec był bardzo", "human|Nalwor|mother", "człowiekiem|Nalwor|matkę"},
  {"en-npcs-ados.txt", "Moja matka jest #elfem", "elf|Nalwor|father|years", "elfem|Nalwor|ojcu|lat"},
  {"en-npcs-semos.txt", "Witaj. Jestem tutejszym kowalem.", "quest|golden highland axe", "zadanie|złotą ciupagę"},
  {"en-npcs-semos.txt", "Odlewam żelazo oraz", "golden highland axe", "złotą ciupagę"},
  {"en-npcs-semos.txt", "Podejmę się #zadania wykonania", "quest|highland axe|logs|gold bars|coins", "zadania|ciupagę|polana|sztabki złota|money"},
  {"en-npcs-regions.txt", "Na razie nic nie potrzebuję, ale okazjonalnie", "play|games", "grać|gry"},
  {"en-npcs-regions.txt", "Nie wiem jak tobie pomóc. Od paru dni", "play|games", "grać|gry"},
  {"en-npcs-regions.txt", "Mogę odlać żelazo, jeżeli", "iron ore|log|cast|quest|golden highland axe|highland axe|gold bars|logs|money|quest for true knight|golden highland axe", "rudę żelaza|polano|odlej|zadania|złotą ciupagę|ciupagę|sztabki złota|polana|money|misyjkę dla prawdziwego rycerza|złotą ciupagę"}
 };
 @Test public void markedTopicsKeepTheirCountOrderAndMeaning() throws Exception {
  for (String[] fixture : CASES) {
   String source = null;
   for (String line : Files.readAllLines(Paths.get("data/languages", fixture[0]), StandardCharsets.UTF_8)) {
    if (line.startsWith(fixture[1])) { source = line.substring(0, line.indexOf('=')); break; }
   }
   assertNotNull(fixture[1], source);
   String rendered = Translation.getText("en", source);
   List<String> captions = links(rendered);
   assertEquals(fixture[1], Arrays.asList(fixture[2].split("\\|")), captions);
   String[] commands = fixture[3].split("\\|");
   for (int i = 0; i < captions.size(); i++) {
    assertEquals(fixture[1] + ": " + captions.get(i), commands[i],
      Translation.getOriginalKeyword("en", captions.get(i), rendered));
   }
   assertEquals(source, Translation.getText("pl", source));
  }
 }
 private static List<String> links(String text) {
  List<String> result = new ArrayList<>();
  Matcher matcher = Pattern.compile("(?<!#)#(?!#)(?:'([^']+)'|([^\\s,.;:!?]+))").matcher(text);
  int end = -1; boolean quoted = true;
  while (matcher.find()) {
   String label = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
   if (!quoted && matcher.group(1) == null && text.substring(end, matcher.start()).equals(" "))
    result.set(result.size() - 1, result.get(result.size() - 1) + " " + label);
   else result.add(label);
   quoted = matcher.group(1) != null; end = matcher.end();
  }
  return result;
 }
}
