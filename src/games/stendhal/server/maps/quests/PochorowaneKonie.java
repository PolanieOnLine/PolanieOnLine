package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class PochorowaneKonie extends AbstractQuest {
	private static final String QUEST_SLOT = "pochorowane_konie";

	// Ilosc potrzebnych lekow
	private static final int ILE_LEKOW = 2;
	// Ilosc potrzebnego jedzenia
	private static final int ILE_JEDZENIA = 5;

	// Losowa choroba
	private static final List<String> CHOROBA = Arrays.asList("grypa końska", "tężec", "zołzy",
			"łykawość", "lipcówka", "kulawizna", "ochwat", "opoje", "nosacizna", "gruda",
			"żabka", "surra", "szpat", "sarkoidoza", "afrykański pomór", "brodawczyca");
	// Losowe przedmioty potrzebne do wyleczenia
	private static final List<String> LEKI = Arrays.asList("mocne antidotum", "antidotum", "duży eliksir", "eliksir", "mały eliksir");
	// Losowe jedzenie dla koni
	private static final List<String> JEDZENIE = Arrays.asList("jabłko", "marchew", "chleb");

	private static final int REQUIRED_MINUTES = 1; // 1 minuta

	private static Logger logger = Logger.getLogger(PochorowaneKonie.class);
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Marcela w Zakopańskiej stajni.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomagać stajennemu i weterynarzowi.");
			return res;
		}
		res.add("Stajenny Marcel kazał mi podejść do weterynarza, który już znajduje się przy jego koniach.");
		if (player.isQuestInState(QUEST_SLOT, "feelgood")) {
			res.add("Muszę zanieść wyniki badań do Dr. Feelgood");
			return res;
		}
		res.add("Zaniosłem wyniki badań i muszę poczekać 10 minut jak weterynarz skończy analizować co to za choroba.");
		if (questState.startsWith("wyniki")) {
			return res;
		}
		if (player.isQuestInState(QUEST_SLOT, "grypa końska", "tężec", "zołzy",
				"łykawość", "lipcówka", "kulawizna", "ochwat", "opoje", "nosacizna", "gruda",
				"żabka", "surra", "szpat", "sarkoidoza", "afrykański pomór", "brodawczyca")) {
			res.add("Dr. Feelgood przeanalizował wyniki Dr. Wojciecha i kazał mi przekazać mówiać " + questState + " gdy wrócę do Dr. Wojciecha.");
			return res;
		}
		res.add("Dr. Wojciech poznał prawdziwą chorobę koni.");
		if (questState.equals("poznana_choroba")) {
			return res;
		}
		if (player.isQuestInState(QUEST_SLOT, "mocne antidotum", "antidotum", "duży eliksir", "eliksir", "mały eliksir")) {
			res.add("Kazał mi przynieść " + Grammar.quantityplnoun(ILE_LEKOW, questState) + ", i powiedzieć " + questState + " gdy wrócę.");
			return res;
		}
		res.add("Zaniosłem potrzebne leki oraz kazał mi poczekać 1 godzinę.");
		if (questState.equals("godzina")) {
			return res;
		}
		res.add("Weterynarz poprsił mnie, abym przyniósł dla koni jedzenie.");
		if (questState.equals("jedzenie")) {
			return res;
		}
		if (player.isQuestInState(QUEST_SLOT, "jabłko", "marchew", "chleb")) {
			res.add("Kazał mi przynieść " + Grammar.quantityplnoun(ILE_JEDZENIA, questState) + ", i powiedzieć " + questState + " gdy wrócę.");
			return res;
		}
		res.add("Muszę poczekać 5 minut jak konie skończą jeść.");
		if (questState.equals("konie_jedza")) {
			return res;
		}
		res.add("Hurra! Konie z tego wyjdą! Muszę przekazać stajennemu dobrą wiadomość! Powiem mu, że konie wyzdrowieją.");
		if (questState.equals("stajenny")) {
			return res;
		}
		res.add("Stajenny się ucieszył! Otrzymałem od niego skórzany pas w nagrodę za pomoc.");
		if (questState.equals("done")) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}
	
	private void step1() {
		final SpeakerNPC npc = npcs.get("Stajenny Marcel");

		// gracz ukonczyl to zadanie
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Moje konie są już zdrowe, dzięki Tobie!", null);

		// oferowanie zadania
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Dobrze, że pytasz o zadanie! Będę miał pewne dla Ciebie. Chcesz mi pomóc?", null);

		// jezeli odpowie TAK
		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Wspaniale! Podejdź do #weterynarza wyżej! On Ci powie co masz robić, bo ja się na tym ni znom.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// jezeli odpowie NIE
		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Szkoda... Moje konie na prawdę potrzebują pomocy.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("weterynarza", "weterynarz", "lekarz", "vet", "wet"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Nazywa się Dr. Wojciech i jest właśnie przy moich koniach próbując je wyleczyć, ale powiedział mi wcześniej, że to jest skomplikowane oraz, że potrzebuje drugiej osoby do pomocy. Pomożesz?",
				null);
	}

	private void step2() {
		final SpeakerNPC npc = npcs.get("Dr. Wojciech");

		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start")),
				ConversationStates.ATTENDING,
				"O.. w końcu stajenny znalazł mi kogoś do pomocy! Słuchaj, tutaj nie ma co więcej mówić.. Proszę, oto wyniki badań tych koni. Zanieść je do #'Dr. Feelgood', bo mam pewne podejrzenia co to może być, ale lepiej się upewnić!",
				new MultipleActions(new EquipItemAction("wyniki badań", 1, true), new SetQuestAction(QUEST_SLOT, "feelgood")));
		
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "feelgood")),
				ConversationStates.ATTENDING,
				"Na co czekasz?! Idź do Dr. Feelgood! Szybko!",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("Dr. Feelgood", "vet", "dr", "dr.", "feelgood", "weterynarz", "wet"),
				null,
				ConversationStates.ATTENDING,
				"Dr. Feelgood znajduje się aktualnie w schronisku dla dzikich zwierząt w Ados.", null);
	}
	
	private void step3() {
		final SpeakerNPC npc = npcs.get("Dr. Feelgood");

		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "feelgood"),
						new PlayerHasItemWithHimCondition("wyniki badań")),
				ConversationStates.ATTENDING,
				"Dr. Wojciech już mnie wcześniej powiadomił, że przyjdzie pewny wojownik do mnie z wynikami.. Dobrze, daj mi je i poczekaj 10 minut. Za chwilę powiem Ci co to za choroba.",
				new MultipleActions(
						new DropItemAction("wyniki badań"),
						new IncreaseXPAction(500),
						new SetQuestAction(QUEST_SLOT, "wyniki;"),
						new SetQuestToTimeStampAction(QUEST_SLOT, 1)));
		
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "feelgood"), 
						new NotCondition(new PlayerHasItemWithHimCondition("wyniki badań"))),
				ConversationStates.ATTENDING,
				"Hej! Dr. Wojciech mnie powiadomił wcześniej, że jakiś wojownik przyjdzie do mnie z wynikami badań i gdzie je masz?! Zgubiłeś je? Masz je poszukać!",
				null);
	}
	
	private void step4() {
		final SpeakerNPC npc = npcs.get("Dr. Feelgood");

		// poczekaj 10 minut
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "wyniki;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES * 10))),
				ConversationStates.IDLE, 
				null, 
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES * 10, "Nie skończyłem jeszcze analizować tych wyników! Poczekaj jeszcze i wróć za "));

		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "wyniki;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES * 10)),
				ConversationStates.ATTENDING, 
				"Właśnie przeanalizowałem #'wyniki' Dr. Wojciecha.", 
				new SetQuestAction(QUEST_SLOT, "przeczytane"));

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("wyniki", "badań", "wynik", "choroba"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "przeczytane"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String choroba = Rand.rand(CHOROBA);
						npc.say("Konie chorują na " + choroba
								+ ". Będziesz jeszcze potrzebował lekarstwa. Wróć do Dr. Wojciecha i powiedz mu #'" + choroba +"'.");
						player.setQuest(QUEST_SLOT, choroba);
					}
				});
	}
	
	private void step5() {
		final SpeakerNPC npc = npcs.get("Dr. Wojciech");
		
		for(final String choroba : CHOROBA) {
			final List<ChatAction> reward = new LinkedList<ChatAction>();
			reward.add(new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						npc.say("Czyli to jest jednak " + choroba + "! Poczekaj jeszcze, bo będę potrzebował #'lekarstwa'.");
					} });
			reward.add(new IncreaseXPAction(500));
			reward.add(new IncreaseKarmaAction(10.0));
			reward.add(new SetQuestAction(QUEST_SLOT, "poznana_choroba"));

				npc.add(ConversationStates.ATTENDING, 
					choroba,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals(choroba);
					}
				}, 
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(reward));
		}
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("lekarstwa", "leki", "leków", "lek"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "poznana_choroba"),
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String leki = Rand.rand(LEKI);
						npc.say("Będę potrzebował " + ILE_LEKOW + " #'" + leki + "'. Jak będziesz to posiadał to przypomnij mi mówiąc #'" + leki + "'.");
						player.setQuest(QUEST_SLOT, leki);
						player.addKarma(5.0);
					}
				});
	}
	
	private void step6() {
		final SpeakerNPC npc = npcs.get("Dr. Wojciech");

		for(final String itemName : LEKI) {
			final List<ChatAction> reward = new LinkedList<ChatAction>();
			reward.add(new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (player.drop(itemName, ILE_LEKOW)) {
							npc.say("Wspaniale! Przyniosłeś " + itemName + "! Wróć do mnie za godzinę, aby sprawdzić jak się miewają konie stajennego.");
						}
					} });
			reward.add(new IncreaseXPAction(1000));
			reward.add(new IncreaseKarmaAction(10.0));
			reward.add(new SetQuestAction(QUEST_SLOT, "godzina;"));
			reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

			npc.add(ConversationStates.ATTENDING, 
				itemName,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals(itemName)
								&& player.isEquipped(itemName, ILE_LEKOW);
					}
				}, 
				ConversationStates.IDLE, 
				null,
				new MultipleActions(reward));
		}
	}
	
	private void step7() {
		final SpeakerNPC npc = npcs.get("Dr. Wojciech");

		// poczekaj 60 minut
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "godzina;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES * 60))),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES * 60, "Musimy jeszcze poczekać na efekty lekarstw! Poczekaj i wróć za "));

		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "godzina;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES * 60)),
				ConversationStates.ATTENDING, 
				"Konie już powoli dochodzą do siebie. Proszę, przynieś mi trochę #'jedzenia' dla koni. Powinno to trochę przyśpieszyć proces leczenia choroby.", 
				new MultipleActions(
						new IncreaseXPAction(3500),
						new SetQuestAction(QUEST_SLOT, "jedzenie")));
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("jedzenia", "jedzenie", "food"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "jedzenie"),
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String food = Rand.rand(JEDZENIE);
						npc.say("Będę potrzebował " + ILE_JEDZENIA + " #'" + food + "'. Przyniesiesz mi je?");
						player.setQuest(QUEST_SLOT, food);
					}
				});
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.IDLE, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String food = player.getQuest(QUEST_SLOT);
						npc.say("Dobrze, to przynieś mi to co potrzebuję oraz wróć do mnie i przypomnij mi mówiąc #'"
							+ food + "'.");
						player.addKarma(5.0);
					}
				});
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Już tak było blisko do wyleczenia koni stajennego... Cóż.. Dowidzenia.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -35.0));
	}
	
	private void step8() {
		final SpeakerNPC npc = npcs.get("Dr. Wojciech");

		for(final String itemName : JEDZENIE) {
			final List<ChatAction> reward = new LinkedList<ChatAction>();
			reward.add(new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (player.drop(itemName, ILE_JEDZENIA)) {
							npc.say("Wspaniale! Przyniosłeś " + itemName + "! Wróć za 5 minut. Konie muszą sobie spokojnie zjeść.");
						}
					} });
			reward.add(new IncreaseXPAction(1500));
			reward.add(new IncreaseKarmaAction(10.0));
			reward.add(new SetQuestAction(QUEST_SLOT, "konie_jedza;"));
			reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

			npc.add(ConversationStates.ATTENDING, 
				itemName,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals(itemName)
								&& player.isEquipped(itemName, ILE_LEKOW);
					}
				}, 
				ConversationStates.ATTENDING, 
				null,
				new MultipleActions(reward));
		}
	}
	
	private void step9() {
		final SpeakerNPC npc = npcs.get("Dr. Wojciech");

		// poczekaj 5 minut
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "konie_jedza;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES * 5))),
				ConversationStates.IDLE, 
				null, 
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES * 5, "Konie jeszcze do końca nie zjadły! Poczekaj i wróć za "));

		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "konie_jedza;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES * 5)),
				ConversationStates.ATTENDING, 
				"Konie z tego wyjdą po jakimś czasie! Wróć do stajennego i powiedz, że #'wyzdrowieją'.", 
				new MultipleActions(
						new IncreaseXPAction(500),
						new SetQuestAction(QUEST_SLOT, "stajenny")));
	}

	private void step10() {
		final SpeakerNPC npc = npcs.get("Stajenny Marcel");

		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("wyzdrowieją", "wyleczone", "konie", "doktor", "zdrowieją"),
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "stajenny")),
				ConversationStates.IDLE, 
				"Ufff... Świetnie to słyszeć! Dziękuję Ci za pomoc oraz weterynarzowi! Proszę, oto skórzany pas, możliwe, że kiedyś Ci się przyda!", 
				new MultipleActions(
						new IncreaseXPAction(10000),
						new EquipItemAction("skórzany pas", 1, true),
						new SetQuestAction(QUEST_SLOT, "done")));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pochorowane konie",
				"Stajenny Marcel potrzebuje pomocy w wyleczeniu jego koni.",
				false);
		step1();
		step2();
		step3();
		step4();
		step5();
		step6();
		step7();
		step8();
		step9();
		step10();
	}

	@Override
	public String getName() {
		return "PochorowaneKonie";
	}

	@Override
	public String getNPCName() {
		return "Stajenny Marcel";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
