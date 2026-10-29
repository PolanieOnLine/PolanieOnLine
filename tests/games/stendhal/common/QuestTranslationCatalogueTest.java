package games.stendhal.common;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import games.stendhal.common.grammar.Grammar;

/** Exercises current Polish quest text, not obsolete upstream quest states. */
public class QuestTranslationCatalogueTest {

	@Test public void translatesCurrentCustomQuestTitlesAndDescriptions() {
		assertEquals("Highlander Collector III", Translation.getText("en", "Góralski Kolekcjoner III"));
		assertEquals("Social Rank: Magnate", Translation.getText("en", "Status Społeczny: Magnat"));
		assertEquals("Janisław offers young warriors rewards for hunting creatures.",
				Translation.getText("en", "Janisław proponuje wszystkim młodym wojom upolowanie stworzeń w zamian za nagrodę."));
	}

	@Test public void bothPlayerGendersReceiveTheSameEnglishJournalMeaning() {
		String suffix = " z Monogenes i on zaproponował mi mapę. Zawsze mogę spytać się jego o mapę i ją dostanę.";
		String expected = "I spoke to Monogenes and he offered me a map. I can always ask him for one.";
		assertEquals(expected, Translation.getText("en", "Rozmawiałem" + suffix));
		assertEquals(expected, Translation.getText("en", "Rozmawiałam" + suffix));
		assertEquals("I met eDragon, the old guardian of the barons' customs.",
				Translation.getText("en", "Spotkałam eDragona, starego strażnika zwyczaju baronów."));
	}

	@Test public void currentRankTrialsPreserveMaterialAmountsAndWaitDurations() {
		assertEquals("The fund must be established in full. It requires 150000 coins together. I will not take part of the sum. Return when you can truly set that amount aside from your own wealth.",
				Translation.getText("en", "Fundusz musi powstać w całości. Potrzeba 150000 monet razem. Nie zabieram części. Wróć, gdy naprawdę możesz odłożyć tę sumę poza własny majątek."));
		assertEquals("It is still cooling. Return in about 12 minutes. Patience is part of authority too.",
				Translation.getText("en", "Jeszcze stygnie. Wróć za około 12 minut. Cierpliwość też jest częścią władzy."));
	}

	@Test public void fixedKeywordMarkupIsRetained() {
		assertEquals("I once had a nice #'leather cuirass', but it was destroyed during the last robbery. If you find a new one, I will reward you.",
				Translation.getText("en", "Kiedyś miałem fajny #'skórzany kirys', ale uległ zniszczeniu podczas ostatniego napadu. Jeśli znajdziesz nowy, dam ci nagrodę."));
		assertEquals("Ah, I see you have an §'olive drink'. Is it for me?",
				Translation.getText("en", "Ach, rozumiem, masz §'napój z oliwką'. Czy to dla mnie?"));
	}

	@Test public void huntingAntiRerollRulesAreTranslated() {
		String source = "Propozycja dla każdej trudności jest zapamiętana na 7 dni. Ukończenie polowania pozwala od razu wylosować nowe. Zabójstwa na arenach nie zaliczają polowania.";
		assertEquals("The offer for each difficulty is saved for 7 days. Completing a hunt lets you draw new offers immediately. Arena kills do not count towards the hunt.", Translation.getText("en", source));
		assertEquals("You have declined the quest. Declining does not draw a new offer. You can choose another difficulty.", Translation.getText("en", "Nie przyjmujesz zadania. Odrzucenie nie losuje nowej propozycji. Możesz wybrać inną trudność."));
		assertEquals("The hunt was abandoned without a reward. Abandoning does not draw a new offer. Choose #easy, #medium, or #hard.", Translation.getText("en", "Polowanie przerwane bez nagrody. Rezygnacja nie losuje nowej propozycji. Wybierz #łatwe, #średnie lub #trudne."));
		assertEquals("Hunting reward rules have changed. Review the new offer and confirm it again.", Translation.getText("en", "Zmieniono zasady nagród za polowania. Sprawdź nową ofertę i potwierdź ją ponownie."));
	}
	@Test public void polishLanguageKeepsQuestTextAndStatesUntouched() {
		assertEquals("Status Społeczny: Magnat", Translation.getText("pl", "Status Społeczny: Magnat"));
		assertEquals("hunting_offer", Translation.getText("en", "hunting_offer"));
		assertEquals("start;12;done", Translation.getText("en", "start;12;done"));
	}

	@Test public void miningUsesCurrentBlackIronRequirementsAndBothGenders() {
		assertEquals("I must mine 12 pieces of black iron ore.",
				Translation.getText("en", "Muszę wydobyć 12 sztuk rudy czarnego żelaza."));
		String source = "To naprawdę czarne żelazo! Dziękuję. Teraz potrzebuję 10 sztuk rudy srebra, 7 polan i kilofa stalowego. Przygotuję z nich mocniejszy kilof, którym będziesz szybciej ";
		String expected = "That really is black iron! Thank you. Now I need 10 pieces of silver ore, 7 logs, and a steel pickaxe. I will use them to make a stronger pickaxe that lets you mine faster.";
		assertEquals(expected, Translation.getText("en", source + "kopał."));
		assertEquals(expected, Translation.getText("en", source + "kopała."));
	}

	@Test public void witchJournalPreservesAmountsAndCompletesBothGenderVariants() {
		assertEquals("I should return in 60 minutes. Keyword: ingredients",
				Translation.getText("en", "Mam wrócić za 60 minut. Hasło: składniki"));
		String expected = "I killed the blue dragon and brought its hide to Benigna as proof that I defeated it alone!";
		assertEquals(expected, Translation.getText("en", "Zabiłem błękitnego smoka i zaniosłem Benignie jego skórę w dowód, że pokonałem go samodzielnie!"));
		assertEquals(expected, Translation.getText("en", "Zabiłam błękitnego smoka i zaniosłam Benignie jego skórę w dowód, że pokonałem go samodzielnie!"));
	}

	@Test public void veterinarianTranslatesRandomDiseaseWithoutChangingTheKeywordValue() {
		assertEquals("The horses have equine influenza. You will also need medicine. Return to Dr. Wojciech and tell him #'equine influenza'.",
				Translation.getText("en", "Konie chorują na grypa końska. Będziesz jeszcze potrzebował lekarstwa. Wróć do Dr. Wojciecha i powiedz mu #'grypa końska'."));
		assertEquals("grypa końska", Translation.getOriginalKeyword("en", "equine influenza"));
		assertEquals("I managed to deliver 12 meals to Groongo.",
				Translation.getText("en", "Udało mi się zanieść 12 posiłków dla Groongo."));
	}

	@Test public void actionAppendedWaitingTimesTranslateAsCompleteSentences() {
		assertEquals("The hammers still beat upon your highland axe. Return in about 2 and a half hours.",
				Translation.getText("en", "Młoty nadal biją nad twoją ciupagą. Wróć za około 2 i pół godziny."));
		assertEquals("I have not finished the knife yet. Please return in less than 2 minutes.",
				Translation.getText("en", "Jeszcze nie skończyłem noża. Proszę wróć za mniej niż 2 minuty."));
		assertEquals("The holy water will be ready in 4 minutes.",
				Translation.getText("en", "Woda święcona będzie gotowa 4 minuty."));
	}

	@Test public void currentGrammarGeneratedSingleAndRepeatedFlowerCountsAreTranslated() {
		for (int amount : new int[] {1, 2, 5, 21}) {
			String expected = "I have brought Princess Tywysoga " + amount + " precious " + (amount == 1 ? "flower." : "flowers.");
			for (String gender : new String[] {"M", "F"}) {
				String source = Grammar.genderVerb(gender, "Zaniosłem") + " już Księżniczce Tywysodze " + Grammar.quantityplnoun(amount, "cenny kwiat") + ".";
				assertEquals(expected, Translation.getText("en", source));
			}
		}
	}

	@Test public void completedMiningAssessmentKeepsTheNumericSkillValue() {
		assertEquals("Your mining experience is fairly good. You have some experience and a basic knowledge of minerals. I would put your mining level at #57!",
				Translation.getText("en", "Twoje doświadczenie w górnictwie jest w miarę dobre, posiadasz trochę już doświadczenia oraz szczątkową wiedzę na temat minerałów kopalnianych. Na moje oko to twój poziom górnictwa wynosi #57!"));
		assertEquals("I am still hard at work. Come back in about 12 minutes.",
				Translation.getText("en", "Wciąż ciężko pracuję. Wróć za około 12 minut."));
	}

	@Test public void barioAndOrcShamanJournalUseActualGenderVerbResults() {
		for (String gender : new String[] {"M", "F"}) {
			String source = Grammar.genderVerb(gender, "Spotkałem") + " zmarźniętego krasnala, który ukrywa się pod ziemią w okolicy Ados. Poprosił mnie, żebym " + Grammar.genderVerb(gender, "przyniósł") + "  mu 10 płaszczy elfickich lazurowych.";
			assertEquals("I met a freezing dwarf hiding underground near Ados. He asked me to bring 10 azure elf cloaks.", Translation.getText("en", source));
			source = Grammar.genderVerb(gender, "Powiedziałem") + " Szamanowi o wykonaniu zadania, a w zamian " + Grammar.genderVerb(gender, "otrzymałem") + " potężną maczugę cierniową.";
			assertEquals("I told the Shaman that the task was complete and received a powerful club of thorns.", Translation.getText("en", source));
		}
	}

	@Test public void fishingListsUseTheExactQuantityAndVerbHelpers() {
		for (int amount : new int[] {1, 2, 5}) {
			String source = "Brakuje wciąż " + Grammar.quantityplnoun(amount, "ryba") + Grammar.isare(amount) + " to: #pstrąg. Czy masz przy sobie którąś z tych ryb?";
			String expected = amount == 1 ? "One fish is still missing: #trout. Do you have it with you?" : amount + " fish are still missing: #trout. Do you have any of them with you?";
			assertEquals(expected, Translation.getText("en", source));
		}
		assertEquals("Let me know when you find it. Goodbye.", Translation.getText("en", "Daj mi znać, gdy znajdziesz " + Grammar.itthem(1) + ". Do widzenia."));
		assertEquals("Let me know when you find them. Goodbye.", Translation.getText("en", "Daj mi znać, gdy znajdziesz " + Grammar.itthem(2) + ". Do widzenia."));
	}

	@Test public void requiredItemAndJoinedRepliesTranslateActualDisplayValues() {
		String item = Grammar.quantityplnoun(2, "jabłko");
		assertEquals("You already have a task to bring 2 apples. Say #done when you have it!",
				Translation.getText("en", "Już dostałeś zadanie by przynieść " + item + ". Powiedz #załatwione jeżeli przyniesiesz!"));
		String list = Grammar.enumerateCollection(java.util.Arrays.asList(item, Grammar.quantityplnoun(3, "polano")));
		assertEquals("I need 2 apples and 3 pieces of wood. Do you have any with you?",
				Translation.getText("en", "Potrzebuję " + list + ". Masz coś ze sobą?"));
		assertEquals("Thank you. If you meet other ghosts, please tell me their names.",
				Translation.getText("en", "Dziękuje. Jeżeli spotkasz inne duchy to proszę powiedz mi ich imiona."));
		assertEquals("You already told me that child is all right. Phew...Now I can rest knowing my children are all right.",
				Translation.getText("en", "Już mi mówiłeś, że z tym dzieckiem jest wszystko dobrze. Uff... teraz mogę odsapnąć wiedząc, że z dziećmi jest wszystko w porządku."));
	}

	@Test public void dragonJournalAndSingleGuessUseActualGrammarOutput() {
		for (String gender : new String[] {"M", "F"}) {
			assertEquals("I undertook to find all the dragons.", Translation.getText("en", Grammar.genderVerb(gender, "Podjąłem") + " się znalezienia wszystkich smoków."));
		}
		assertEquals("Let us see... You have 1 guess left... And if I remember correctly, I asked... How many rats do you think you killed?",
				Translation.getText("en", "Zobaczmy... zostało Tobie " + Grammar.quantityplnounCreature(1, "prób") + "... i jeśli dobrze pamiętam to zapytałam się ciebie... ile " + Grammar.pluralCreature("szczur") + " myślisz, że zabiłeś?"));
	}

	@Test public void builtQuestAndGlyphCompletionCountersKeepTheirActualAmounts() {
		for (int amount : new int[] {1, 2, 5}) {
			String built = "Wnętrze góry zostało wyczyszczone ze zbójników [count] [raz].".replace("[count]", String.valueOf(amount)).replace("[raz]", Grammar.plnoun(amount, "raz"));
			String expected = amount == 1 ? "The mountain interior has been cleared of bandits once." : "The mountain interior has been cleared of bandits " + amount + " times.";
			assertEquals(expected, Translation.getText("en", built));
			String glyph = "Omar zrekonstruował dla mnie " + Grammar.quantityplnoun(amount, "raz") + " fragment dawnej matrycy glifu.";
			expected = amount == 1 ? "Omar reconstructed an ancient glyph matrix fragment for me once." : "Omar reconstructed an ancient glyph matrix fragment for me " + amount + " times.";
			assertEquals(expected, Translation.getText("en", glyph));
		}
	}

	@Test public void separatelyStoredHintsAndRiddlesHaveFullEnglishText() {
		assertEquals("Artificial intelligence is no match for natural stupidity.", Translation.getText("en", "Sztuczna inteligencja nie pasuje do naturalnej głupoty."));
		assertEquals("I am sure Giles in Kirdneh can braid a rope for you.", Translation.getText("en", "Jestem pewien, że Giles w Kirdneh może splatać dla ciebie linę."));
		assertEquals("Antithei was in Kościelisko.", Translation.getText("en", "Antithei przebywał na Kościelisku."));
		assertEquals("I burn like fire. My presence brings little joy, and those who provoke me will feel my wrath. What am I?", Translation.getText("en", "Płonę jak ogień. Moja obecność za bardzo nie cieszy, a ci którzy spróbują to poczują mój gniew. Czym jestem?"));
	}
}
