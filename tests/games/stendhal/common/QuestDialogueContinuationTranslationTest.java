package games.stendhal.common;

import static org.junit.Assert.*;
import org.junit.Test;

public class QuestDialogueContinuationTranslationTest {
 @Test public void fishingLicenseAndRebelGuardsDialogueIsEnglish() {
  String[][] dialogue = {
   {"Wykonałeś dobrą robotę! Teraz jesteś prawdziwym rybakiem i będziesz lepszym, gdy złowisz rybę!", "You did a good job! Now you are a real fisherman, and you will get better as you catch fish!"},
   {"Masz już kartę wędkarską i nie mam dla ciebie zadania.", "You already have a fishing license, and I have no task for you."},
   {"Straż wraz co niektórymi rycerzami się zbuntowali i przejeli zamek. Przydałaby się nam każda pomoc, dasz radę pozbyć się zbuntowanych rycerzy?", "The guards and some of the knights have rebelled and seized the castle. We could use all the help we can get. Can you get rid of the rebel knights?"},
   {"Już się Ciebie pytałem o pozbyciu się zbuntowanej straży i rycerzy!", "I have already asked you to get rid of the rebel guards and knights!"},
   {"Jesteś godzien czci rycerskiej! Pamiętaj, że dalsza droga nie będzie łatwa, na każdym kroku musisz udowadniać swe męstwo i odwagę! Przyjmij nieco złotych dukatów na pokrycie napraw swojego wyposażenia.", "You are worthy of a knight's honor! Remember, the road ahead will not be easy; you must prove your valor and courage at every step! Accept some gold ducats to cover repairs to your equipment."}
  };
  checkDialogue(dialogue);
 }

 @Test public void marriageAndHoneymoonRequirementsAreEnglish() {
  checkDialogue(new String[][] {
   {"Mój słuch nie jest zbyt dobry. Proszę podejdźcie oboje bliżej i powiedzcie mi z kim chcecie się zaręczyć.", "My hearing is not very good. Please both come closer and tell me whom you wish to get engaged to."},
   {"Jak słodko! Proszę zobaczcie nasz katalog i powiedzcie mi numer pokoju, który chcecie.", "How sweet! Please look at our catalogue and tell me the number of the room you would like."},
   {"Każdy z was potrzebuje jednego miejsca w plecaku, aby wziąć zwój. Proszę zróbcie miejsce i zapytajcie mnie ponownie. Dziękuję.", "Each of you needs one free space in your backpack for a scroll. Please make room and ask me again. Thank you."}
  });
 }

 @Test public void questImageTitlesAndChallengeNameMarkerRemainUsable() {
  checkDialogue(new String[][] {
   {"Rysunek Jadźki", "Jadźka's Drawing"},
   {"Jadźka rysuje", "Jadźka Draws"},
   {"Gaździna Jadźka", "Gaździna Jadźka"},
   {"Nie możesz oznaczyć dopóki nie masz łuku i specjalnej amunicji", "You cannot tag anyone until you have a bow and special ammunition"},
   {"Przepraszam cię [name], ale moja intuicja co do skarbów jest niezawodna i podpowiada mi iż nie udało ci się jeszcze dokończyć wyzwania...", "I am sorry, [name], but my instinct for treasure is infallible, and it tells me that you have not completed the challenge yet..."}
  });
 }

 @Test public void challengePreservesPlayerNamesAndHouseBrochureKeepsItsExactUrl() {
  assertEquals("I am sorry, Zamknij {0}, but my instinct for treasure is infallible, and it tells me that you have not completed the challenge yet...",
   Translation.getText("en", "Przepraszam cię Zamknij {0}, ale moja intuicja co do skarbów jest niezawodna i podpowiada mi iż nie udało ci się jeszcze dokończyć wyzwania..."));
  assertEquals("You should ask about the #price before buying and check our brochure at #'https://polanieonline.eu/forum/wiedza?type=guide&q=domek'.",
   Translation.getText("en", "Powinieneś się dowiedzieć o #cenę przed kupnem i sprawdzić naszą broszurę #'https://polanieonline.eu/forum/wiedza?type=guide&q=domek'."));
 }

 private void checkDialogue(String[][] dialogue) {
  for (String[] row : dialogue) {
   assertEquals(row[0], row[1], Translation.getText("en", row[0]));
   assertEquals(row[0], Translation.getText("pl", row[0]));
  }
 }

 @Test public void orbInstructionsAndNewJournalVariantsTranslate() {
  checkDialogue(new String[][] {
   {"Naciśnij prawy przycisk i wybierz Użyj. Dostałeś jakąś wiadomość?", "Right-click and select Use. Did you receive a message?"},
   {"Naciśnij prawy przycisk na kuli i wybierz Użyj.", "Right-click the orb and select Use."},
   {"Znam wszystkie nazwy ryb i jestem teraz lepszym wędkarzem!", "I know all the fish names and am a better fisherman now!"},
   {"Tomi poprosił mnie o \"miecz lodowy\" i wziął ten, który mu przyniosłem!", "Tomi asked me for an \"ice sword\" and took the one I brought him!"},
   {"Pozbyłem się kilku zbójników napadających Zakopane i postaram się pozbyć ich więcej.", "I got rid of some of the bandits raiding Zakopane and will try to get rid of more."},
   {"Rycerz z pod zamku potrzebuje jeszcze raz pomocy i nagrodzi mnie, czy mam mu pomóc?", "The knight outside the castle needs help again and will reward me. Should I help him?"}
  });
 }

 @Test public void remainingStaticQuestResponsesTranslateWithoutChangingLinks() {
  checkDialogue(new String[][] {
   {"Sprawdziłem moje zapiski i na razie nie jesteś nic winny. Właściciele domów są informowani przez postman, gdy przyjdzie czas zapłaty.", "I checked my records, and you owe nothing for now. Homeowners are notified by the postman when payment is due."},
   {"Pomocy! Duergars najechały na więzienie i zamknęły mnie tutaj! Powinienem być Strażnikiem! To chaos.", "Help! Duergars invaded the prison and locked me in here! I am supposed to be a guard! This is chaos."},
   {"Wspaniale! Odkąd zajęli zamek nie mamy dostępu do odpowiedniego wyposażenia i arsenału. Kręcą się za murami tego zamku buntownicy...", "Wonderful! Since they seized the castle, we have had no access to proper equipment or the arsenal. Rebels are lurking behind those castle walls..."},
   {"Ślub możesz wziąć tutaj w tym kościele. Jeżeli chcesz się #zaręczyć to przyprowadź tutaj swojego partnera i powiedz mi #zaręcz #imię.", "You can get married here in this church. If you want to get #engaged, bring your partner here and tell me #engage #name."}
  });
 }

 @Test public void honeymoonPreservesPartnerNameAndDivorceTranslatesNestedFees() {
  assertEquals("Could you ask Zamknij {0} to come to reception and look through our catalogue? Then tell me which room you have chosen.",
   Translation.getText("en", "Czy mógłbyś poprosić Zamknij {0}, aby podeszła do recepcji i zapoznała się z naszym katalogiem. Później powiedz, który pokój wybraliście."));
  String prefix = "Istnieje możliwość zapłacenia za rozwód zamiast utraty doświadczenia. Będzie cię to kosztować 6200 money. ";
  String suffix = " Czy chcesz wziąć rozwód i zapłacić dodatkową opłatę, która uratuje Ciebie od utraty doświadczenia?";
  String englishPrefix = "You can pay for a divorce instead of losing experience. It will cost you 6200 coins. ";
  String englishSuffix = " Would you like to get divorced and pay the extra fee to avoid losing experience?";
  assertEquals(englishPrefix + "Zamknij {0} has 8000 coins to pay the fee." + englishSuffix,
   Translation.getText("en", prefix + "Zamknij {0} posiada 8000 money na opłatę." + suffix));
  assertEquals(englishPrefix + "Zamknij {0} does not have 8000 coins to pay the fee and will lose 3% experience instead." + englishSuffix,
   Translation.getText("en", prefix + "Zamknij {0} nie posiada 8000 money na opłatę i straci zamiast tego 3% doświadczenia." + suffix));
  assertEquals(englishPrefix + englishSuffix, Translation.getText("en", prefix + suffix));
 }

 @Test public void guardCooldownTranslatesTheActualDurationAndMarriageLinksRemainCanonical() {
  assertEquals("On behalf of the king, thank you for your help. The rebel guards and knights may return. Come back in 2 hours.",
   Translation.getText("en", "W imieniu króla dziękuję za pomoc. Zbuntowana straż i rycerze mogą powrócić. Wróć za "
    + games.stendhal.server.util.TimeUtil.approxTimeUntil(7200) + "."));
  assertEquals("zaręcz", Translation.getOriginalKeyword("en", "engage"));
  assertEquals("imie", Translation.getOriginalKeyword("en", "name"));
  assertEquals("razem", Translation.getOriginalKeyword("en", "together"));
 }
}
