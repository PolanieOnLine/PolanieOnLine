package games.stendhal.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import games.stendhal.common.grammar.Grammar;

public class BulkQuestTranslationTest {
 @Test public void questTitlesAndMapCaptionsAreEnglish() {
  check("Zbierz Informacje o Wrogu", "Collect Enemy Information");
  check("Prezent Wielkanocny", "Easter Gift");
  check("Przygotowania do Mine Town", "Preparations for Mine Town");
  check("Zagubione latarenki", "Lost Lanterns");
  check("Mapa miasta Zakopane", "Map of Zakopane");
  check("Rysunek Carmen", "Carmen's Drawing");
  check("Rysunek Ilisy", "Ilisa's Drawing");
 }

 @Test public void medicineConstantsAndAsciiRepliesTranslate() {
  check("Masz przy sobie te #zioła do #lekarstwa?", "Do you have the #herbs for the #medicine with you?");
  check("Mam butelkę i wkrótce zaniosę ją Tadowi.", "I have a flask and will take it to Tad soon.");
  check("Tad podziękował mi.", "Tad thanked me.");
  check("Pomożesz mi?", "Will you help me?");
  check("Fleur pracuje na targu w Kirdneh.", "Fleur works at the market in Kirdneh.");
  check("Zapytaj barmanki w Semos.", "Ask the barmaid in Semos.");
  check("Grrr. Pizza zimna. Ruszasz sie jak owca.", "Grrr. The pizza is cold. You move like a sheep.");
  check("Rabujcie co cennego!", "Loot anything valuable!");
 }

 @Test public void runtimeJournalGenderFragmentsProduceEnglishForBothGenders() {
  for (String gender : new String[] {"M", "F"}) {
   check(Grammar.genderVerb(gender, "Dostarczyłem")
    + " ostatni przedmiot do burmistrza i odebrałem moją nagrodę w ciągu 24 godzin.",
    "I delivered the last item to the mayor and received my reward within the last 24 hours.");
   check("Smoki " + Grammar.genderVerb(gender, "znalazłem") + " i "
    + Grammar.genderVerb(gender, "dostałem") + " nagrodę.",
    "I found the dragons and received a reward.");
   check(Grammar.genderVerb(gender, "Poszedłem") + " do banku w Nalwor i "
    + Grammar.genderVerb(gender, "spotkałem") + " Grafindle.",
    "I went to the bank in Nalwor and met Grafindle.");
   // This source intentionally contains a fixed first verb and a gendered second verb.
   check("Pozbyłem się kilku zbójników napadających Zakopane i "
    + Grammar.genderVerb(gender, "otrzymałem") + " nagrodę od Gazdy Wojtka!",
    "I got rid of some of the bandits raiding Zakopane and received a reward from Gazda Wojtek!");
  }
 }

 @Test public void rewardHooksAndNpcNamesRemainReadable() {
  check("Oddam ci za nie srebrną skrzynię. Niech i tobie coś dobrego przypadnie na święto.",
   "I will give you a silver chest for them. May the festival bring you something good too.");
  check("Masz 3 kanapki, 3 butelki soku z chmielu i 3 szklanki napoju z winogron?",
   "Do you have 3 sandwiches, 3 bottles of beer, and 3 glasses of grape drink?");
  check("Mam kilka zabawek dla Anna, Jens i George do zabawy.",
   "I have some toys for Anna, Jens, and George to play with.");
  check("Przykro mi, ale nie jestem ekspertem w potrawach i napojach. Zapytaj #Stefana.",
   "I am sorry, but I am no expert on food and drink. Ask #Stefan.");
  check("[name], Udało ci się mnie wyleczyć!", "[name], you managed to cure me!");
  check("Zamknij {0}, Udało ci się mnie wyleczyć!", "Zamknij {0}, you managed to cure me!");
  check("Gaździna Jadźka", "Gaździna Jadźka");
  check("Gazda Wojtek", "Gazda Wojtek");
 }

 private static void check(String source, String english) {
  assertEquals(source, english, Translation.getText("en", source));
  assertEquals(source, Translation.getText("pl", source));
 }
}
