package games.stendhal.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BulkServiceTranslationTest {
 @Test public void equipmentAndInteractionErrorsTranslate() {
  check("Nie posiadasz zbyt wiele zwoi do zapisania.", "You do not have enough scrolls to mark.");
  check("Przedmiot jest zbyt daleko.", "The item is too far away.");
  check("Jedzenie jest zbyt daleko.", "The food is too far away.");
  check("Eliksir jest zbyt daleko.", "The potion is too far away.");
  check("Nie możesz zabrać wyposażenia z cudzej skrzyni zadania.",
    "You cannot take equipment from another player's quest chest.");
  check("Twoja oferta została zablokowana. Możesz anulować wymianę jeżeli chcesz coś zmienić.",
    "Your offer is locked. You can cancel the trade if you want to change something.");
  check("Twoja grupa jest w komplecie.", "Your group is full.");
  check("Nie ma tam miejsca.", "There is no room there.");
 }

 @Test public void capturedPlayerAndPetNamesRemainLiteral() {
  check("Ten przedmiot jest chroniony przez miecz", "This item is protected by miecz");
  check("Nie znaleziono jabłko.", "jabłko was not found.");
  check("Nie masz miecz", "You do not have miecz");
  check("jabłko nie jest ignorowany przez Ciebie.", "You are not ignoring jabłko.");
  check("Nie możesz wziąć przedmiotów ze skrzyni w banku miecz.",
    "You cannot take items from miecz's bank chest.");
 }

 @Test public void questRequirementsAndServicesTranslate() {
  check("Ukończ zadanie: ZZQuest (tak)", "Complete the quest: ZZQuest (yes)");
  check("Ukończ zadanie: ZZQuest (nie)", "Complete the quest: ZZQuest (no)");
  check("Pokonaj samodzielnie: ZZCreature (0/1)", "Defeat alone: ZZCreature (0/1)");
  check("Wybierz konkretny przedmiot w otwartym oknie ulepszania.",
    "Choose a specific item in the upgrade window.");
  check("Wróć z przedmiotem potrzebnym do ukończenia zlecenia.",
    "Come back with the item needed to complete the order.");
  check("Najpierw musisz ukończyć wcześniejsze zadania.",
    "You must complete the earlier quests first.");
 }

 @Test public void itemConditionIsTranslatedInsideTheFullDescription() {
  check("ZZDescription Jest nieużywany.", "ZZDescription It is unused.");
  check("ZZDescription Jest bardzo zużyty.", "ZZDescription It is very worn.");
  check("Zapisane miejsce w pierścieniu jest uszkodzone. Kamień stracił ten zapis i trzeba wskazać mu nowe miejsce.",
    "The location saved in the ring is corrupted. The stone has lost this record and needs a new location.");
  check("Portal utworzony przez kamień słabnie, odczuwasz wrażenie potrzeby powrotu do swojego świata!",
    "The portal created by the stone is weakening. You feel the need to return to your own world!");
 }

 private static void check(String source, String expected) {
  assertEquals(source, expected, Translation.getText("en", source));
  assertEquals(source, source, Translation.getText("pl", source));
 }
}
