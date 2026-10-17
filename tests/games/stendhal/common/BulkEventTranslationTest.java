package games.stendhal.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BulkEventTranslationTest {
 @Test public void tutorialAndSceneNotificationsReachTheEnglishPlayerInEnglish() {
  games.stendhal.server.maps.MockStendlRPWorld.get();
  games.stendhal.server.entity.player.Player player = utilities.PlayerTestHelper.createPlayer("LearningEnglish");
  player.setLanguage("en");
  player.setClientVersion("1.0");
  player.sendPrivateText(NotificationType.TUTORIAL, "Przewodnik: Naciśnij przycisk F1, aby przeczytać przewodnik ze zdjęciami.");
  player.sendPrivateText(NotificationType.SCENE_SETTING, "Zwiedzając dalej przechodzą Cię ciarki. Zdecydowanie znajduje się tutaj magia.");
  assertEquals("Guide: Press F1 to read the illustrated guide.", player.events().get(0).get("text"));
  assertEquals("A shiver runs through you as you explore farther. There is definitely magic here.", player.events().get(1).get("text"));
 }

 @Test public void humanConversationIsNotTranslatedByTheTutorialFix() {
  games.stendhal.server.maps.MockStendlRPWorld.get();
  games.stendhal.server.entity.player.Player player = utilities.PlayerTestHelper.createPlayer("LearningEnglish");
  player.setLanguage("en");
  String message = "Przewodnik: Naciśnij przycisk F1, aby przeczytać przewodnik ze zdjęciami.";
  player.sendPrivateText(NotificationType.NORMAL, message);
  assertEquals(message, player.events().get(0).get("text"));
 }
 @Test public void tutorialWrapperTranslatesTheAuthoredMessage() {
  check("Przewodnik: Możesz porozmawiać z Pietrkiem mówiąc \"cześć\".",
    "Guide: You can talk to Pietrek by saying \"hi\".");
  check("Przewodnik: Otrzymałeś prywatną wiadomość, aby odpisać użyj #/msg #imię #wiadomość.",
    "Guide: You have received a private message. To reply, use #/msg #name #message.");
  check("Przewodnik: Naciśnij przycisk F1, aby przeczytać przewodnik ze zdjęciami.",
    "Guide: Press F1 to read the illustrated guide.");
 }

 @Test public void zoneAndRaidAnnouncementsTranslate() {
  check("Zwiedzając dalej przechodzą Cię ciarki. Zdecydowanie znajduje się tutaj magia.",
    "A shiver runs through you as you explore farther. There is definitely magic here.");
  check("Marcus krzyczy: Potrzebujemy posiłków w ciągu 10 minut.",
    "Marcus shouts: We need reinforcements within 10 minutes.");
  check("Katinka krzyczy: Argh! Jedzą nasze dziki. Pomóżcie nam!",
    "Katinka shouts: Argh! They are eating our boars. Help us!");
 }

 @Test public void scriptedBetsPreserveNamesAndTargets() {
  check("Przepraszam miecz, ale akceptuje zakłady tylko na jabłko",
    "Sorry miecz, but I only accept bets on jabłko");
  check("miecz ona lub on mógł wygrać, ale odszedł.", "miecz could have won, but left.");
  check("Zwycięzcą został ... jabłko.", "The winner is... jabłko.");
 }

 @Test public void luaQuestDialogueAndHistoryTranslate() {
  check("Straciłem mój pierścionek zaręczynowy i wstydzę się powiedzieć Emmie. Czy pomógłbyś mi?",
    "I lost my engagement ring, and I am ashamed to tell Emma. Would you help me?");
  check("Znalazłem pierścionek Ariego, ale chyba go zgubiłem. Powinienem zgłosić Ariemu, że <em>zgubiłem</em> go.",
    "I found Ari's ring, but I seem to have lost it. I should tell Ari that I <em>lost</em> it.");
  check("Nie masz nawet wykrywacza metalu.", "You do not even have a metal detector.");
  check("Mogę #wzmocnić użyteczny #pierścień jeśli przyniesiesz parę materiałów.",
    "I can #forge a useful #ring if you bring me some materials.");
  check("jabłko, proszę zostaw miecz w spokoju.", "jabłko, please leave miecz alone.");
 }

 @Test public void multilineLuaInstructionsTranslateWithoutLosingBulletMarkers() {
  check("Uwagi dotyczące korzystania z wykrywacza metali:\n"
    + "- Naciśnij przycisk \"Skanuj\", aby go włączyć.\n"
    + "- Urządzenie wyda sygnał dźwiękowy, jeśli znajdziesz się w pobliżu.\n"
    + "- Im bliżej jesteś, tym szybciej emituje sygnał dźwiękowy.",
    "Notes on using the metal detector:\n"
    + "- Press the \"Scan\" button to turn it on.\n"
    + "- The device will beep when you are nearby.\n"
    + "- The closer you are, the faster it beeps.");
 }

 private static void check(String source, String expected) {
  assertEquals(source, expected, Translation.getText("en", source));
  assertEquals(source, source, Translation.getText("pl", source));
 }
}
