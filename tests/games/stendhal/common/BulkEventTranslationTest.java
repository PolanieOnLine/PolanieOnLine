package games.stendhal.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.StunnedStatus;
import games.stendhal.server.entity.status.StunnedStatusHandler;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class BulkEventTranslationTest {
	@Test public void statusTemplatesTranslateBothPlayerGendersWithoutChangingNames() {
		check("Zostałeś zraniony przez Zamknij.", "You have been wounded by Zamknij.");
		check("Zostałaś zraniona przez Zamknij.", "You have been wounded by Zamknij.");
		check("Zostałeś otruty przez Jożin z bażin.", "You have been poisoned by Jożin z bażin.");
		check("Zostałaś otruta przez Jożin z bażin.", "You have been poisoned by Jożin z bażin.");
		check("Zostałeś ogłuszony przez Zamknij.", "You were stunned by Zamknij.");
	}

	@Test public void statusHandlersLocalizeCreaturesButPreservePlayerNames() {
		MockStendlRPWorld.get();
		final Creature giant = SingletonRepository.getEntityManager().getCreature("lodowy starszy olbrzym");
		final Entity[] sources = {giant, PlayerTestHelper.createPlayer("Zamknij")};
		final String[] names = {"elder ice giant", "Zamknij"};
		for (int i = 0; i < sources.length; i++) {
			final Player target = PlayerTestHelper.createPlayer("Reader" + i);
			target.setLanguage("en");
			final StunnedStatusHandler handler = new StunnedStatusHandler();
			final StunnedStatus status = new StunnedStatus();
			handler.inflict(status, target.getStatusList(), sources[i]);
			assertEquals("You were stunned by " + names[i] + ".", target.events().get(0).get("text"));
			handler.remove(status, target.getStatusList());
		}
	}

	@Test public void chestAndTeddyDescriptionsAndGeneratedShopOffersTranslate() {
		check("Przytul", "Hug");
		check("!me przytulił pluszowego misia.", "!me hugged their teddy bear.");
		check("Pluszowy miś jest miękki i kojący.", "The teddy bear is soft and comforting.");
		check("chest jest zbyt daleko.", "The chest is too far away.");
		check("corpse jest zbyt daleko.", "The corpse is too far away.");
		check("Oto skrzynia. Jest otwarta. Możesz nacisnąć prawy przycisk myszy, aby zobaczyć zawartość.",
			"You see a chest. It is open. Right-click to view its contents.");
		check("Oto skrzynia. Jest zamknięta.", "You see a chest. It is closed.");
		check("Sprzedaję bilet na mecz oraz piłka.", "I sell match tickets and balls.");
		check("Sprzedaję miecz oraz topór.", "For sale: sword and axe.");
	}
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
