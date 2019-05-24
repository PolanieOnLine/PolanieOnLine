/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: The Jailed Barbarian
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Lorenz, the jailed barbarian in a hut on Amazon Island</li>
 * <li>Esclara the Amazon Princess</li>
 * <li>Ylflia the Princess of Kalavan</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>1. Lorenz ask you for a scythe to bring him</li>
 * <li>2. You have to ask Princess Esclara for a 'reason'</li>
 * <li>3. You have to bring him an egg</li>
 * <li>4. You have to inform Princess Ylflia</li>
 * <li>5. You have to bring him a barbarian armor</li>
 * <li>6. You get a reward.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>You get 20 gold bars</li>
 * <li>Karma: 15</li>
 * <li>You get 52,000 experience points in all</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Not repeatable.</li>
 * </ul>
 */

 public class ZagadkiBrzezdoma extends AbstractQuest {

	private static final String QUEST_SLOT = "zagadki";
	private static Logger logger = Logger.getLogger(ZagadkiBrzezdoma.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step1() {
		final SpeakerNPC npc = npcs.get("Brzezdom");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Szukam odpowiedzi na wiele pytań. Czy możesz mi pomóc?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za pomoc! W końcu moja wiedza jest większa.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Wspaniale! Pierwszym pytaniem jakie mnie nurtuje jest: kto dał zaczarowany #pas i #kierpce Janosikowi? "
				+ " Wiadomość najlepiej zdobyć czytając góralskie legendy. To taka mała podpowiedź."
				+ " Proszę przynieś mi te kierpce. Moje obuwie szybko zdziera się, niedługo nie będę miał w czym chodzić.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 20.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Odejdź, ktoś inny mi pomoże!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -20.0));
	}

	private void step2() {
	final SpeakerNPC npc = npcs.get("Brzezdom");

		npc.add(ConversationStates.ATTENDING, "maryna",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new PlayerHasItemWithHimCondition("kierpce")),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.drop("kierpce");
					player.addKarma(10);
					player.addXP(1000);
					npc.say("A widzisz! Jednak ta czarownica dała mu te rzeczy!! Pierwszą część mamy za sobą!"
					+ " Stary Baca chciałby wiedzieć kogo spotkał juhas Kuba pasąc owieczki?"
					+ " Udaj się do niego z odpowiedzią.");
					player.setQuest(QUEST_SLOT, "baca");
				};
		});

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("czarownica", "kierpce", "pas"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("kierpce"))),
			ConversationStates.ATTENDING,
			"Masz mi przynieść kierpce które dostał Janosik od tajemniczej osoby."
			+ " Oraz powiedzieć kto mu to dał.",
			null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING,
				"Już się ciebie pytałem o imię tej osoby. Przynieś mi także kierpce.",
				null);
	}

	private void step3() {
	final SpeakerNPC npc = npcs.get("Stary Baca");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("król", "króla"),
				new QuestInStateCondition(QUEST_SLOT, "baca"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					npc.say("Dziękuję bardzo. A to łajdak można było wykorzystać wojsko króla, dlaczego mi nic nie powiedział...."
					+ " Widzę, że dobrze radzisz sobie ze zbieraniem wiadomości. Mam małą prośbę."
					+ " Kowal Andrzej spiera się ze mną jaki jest najwyższy #szczyt Tatr. Idź do niego i powiedz mu jaki aby w końcu skończył sprzeczać się.");
					player.setQuest(QUEST_SLOT, "gerlach");
				};
		});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("szczyt", "tatr"),
				new QuestInStateCondition(QUEST_SLOT, "gerlach"),
				ConversationStates.ATTENDING, "Kłóci się ze mną o to już od bardzo dawna. Chcę aby ten spór się skończył."
				+ " Ty jako bezstronna osoba pogodzisz nas. Gdy go spotkasz powiedz mu nazwę tego szczytu.",
				null);	
	}

	private void step4() {
		final SpeakerNPC npc = npcs.get("Kowal Andrzej");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("gerlach", "gierlach", "garłuch"),
				new QuestInStateCondition(QUEST_SLOT, "gerlach"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					npc.say("Hm... No cóż jednak Brzezdom miał rację. Cieszę się że w końcu ten spór został zakończony."
					 + " Mam dla ciebie małe zadanie. Tylko ty możesz je wykonać. Zdobądź góralski gorset i zanieś go Marynie."
					 + " Przy okazji mógłbyś dowiedzieć się jak ma na imię największy bajkopisarz Podhala. Powiedz Marynie jego imię, ucieszy się z pewnością.");
					player.setQuest(QUEST_SLOT, "gorset");
				};
		});
	}

	private void step5() {
	final SpeakerNPC npc = npcs.get("Maryna");	

		npc.add(ConversationStates.ATTENDING, Arrays.asList("sabała", "sabalik", "jan krzeptowski"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gorset"),
				new PlayerHasItemWithHimCondition("góralski gorset")),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.drop("góralski gorset");
					player.addKarma(30);
					player.addXP(10000);
					npc.say("A więc przysyła cię kowal. Sabała znany też jako sabalik, innymi słowy Jan Krzeptowski- bajkopisarzem, kto by pomyślał. A gorset też jest niczego sobie ☺."
					+ " Zrób coś dla mnie. Pędź proszę teraz do Bogusia i powiedz mu kto jest #winien, że w Zakopanym nie rośnie kapusta.");
					player.setQuest(QUEST_SLOT, "kapusta");
				};
		});

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("sabała", "sabalik", "jan krzeptowski"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gorset"), new NotCondition(new PlayerHasItemWithHimCondition("góralski gorset"))),
			ConversationStates.ATTENDING,
			"Kowal przesłał mi wiadomość, że dostarczysz mi gorset. Gdzie go masz?!",
			null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "gorset"),
				ConversationStates.ATTENDING,
				"Proszę przynieś mi góralski gorset.",
				null);

		npc.add(ConversationStates.ATTENDING, Arrays.asList("winien", "winna"),
				new QuestInStateCondition(QUEST_SLOT, "kapusta"),
				ConversationStates.ATTENDING, "Chodzą pogłoski, że kapusta w Zakopanem nie rośnie. Dowiedz się kto lub co jest tego przyczyną i powiedz Bogusiowi. Znajdziesz go w szpitalu w Zakopanym.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "kapusta"),
				ConversationStates.ATTENDING,
				"Potrzebuję ciebie, abyś dowiedział się kto lub co jest winne tej sytuacji.",
				null);
	}

	private void step6() {
	final SpeakerNPC npc = npcs.get("Boguś");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("gąsienica", "gąsienice"),
				new QuestInStateCondition(QUEST_SLOT, "kapusta"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					npc.say("Ha ha ha ☺ Dałeś nabrać się Marynie. Toż to stary kawał jej o kapuście i gąsienicach."
					+ " Ale dość żartów. Jak już tu jesteś zrobisz coś dla mnie. Udaj się do Edgarda. Znajdziesz go na ziemiach Zakonu Rycerzy Cienia."
					+ " Spaceruje koło zamku, przekaż mu moje #pozdrowienia i powiedz mu kto był odpowiednikiem Zeusa w wierzeniach słowiańskich."
					+ " Odpowiedź znajdziesz na #'http://www.polskagra.net/'" );
					player.setQuest(QUEST_SLOT, "bogowie");
				};
		});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("greetings", "pozdrawiam", "pozdrowienia"),
				new QuestInStateCondition(QUEST_SLOT, "bogowie"),
				ConversationStates.ATTENDING, "Idź i przekaż Edgardowi odpowiedź na nurtujące go pytanie.",
				null);
	}

	private void step7() {
	final SpeakerNPC npc = npcs.get("Edgard");

		npc.add(ConversationStates.ATTENDING,  "perun",
				new QuestInStateCondition(QUEST_SLOT, "bogowie"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					npc.say("Dziękuję przyjacielu. Moja wiedza poszerzyła się, widzę iż potrafisz bardzo dużo."
					+ " Mam zadanie dla tak wszechstronnego wojownika. Zdobądź czarną zbroje i zanieś go Bercikowi. Powodzenia.");
					player.setQuest(QUEST_SLOT, "armor");
				};
		});
	}

	private void step8() {
	final SpeakerNPC npc = npcs.get("Bercik");
	
		npc.add(ConversationStates.ATTENDING, Arrays.asList("armor", "zbroja", "zbroję"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "armor"),
				new PlayerHasItemWithHimCondition("czarna zbroja")),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.drop("czarna zbroja");
					 final StackableItem gold = (StackableItem) SingletonRepository.getEntityManager().getItem("sztabka złota");
					final int goldamount = 30;
					gold.setQuantity(goldamount);
					player.equipOrPutOnGround(gold);
					player.addKarma(35);
					player.addXP(50000);
					npc.say("Wspaniała zbroja, dziękuję ci za nią w nagrodę przyjmij ten skromny podarek."
					+ " Znalazłem nowe urobisko w kopalni pod Kościeliskiem ale podobno pod Zakopanem jest tego więcej."
					+ " Przetopieniem bryłek na sztabki zajmują się czeladnicy mistrza Drogosza.");
					player.setQuest(QUEST_SLOT, "done");
				};
		});

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("armor", "zbroja", "zbroję"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "armor"), new NotCondition(new PlayerHasItemWithHimCondition("czarna zbroja"))),
			ConversationStates.ATTENDING,
			"Nie posiadasz black armor przy sobie! Edgard mówił, że można liczyć na ciebie! Idź i zdobądź go dla mnie!",
			null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "armor"),
				ConversationStates.ATTENDING,
				"Czekam na black #armor od Ciebie.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Zagadki Brzezdoma",
			"Znajdź odpowiedzi na pytania Brzezdoma.",
			false);

		step1();
		step2();
		step3();
		step4();
		step5();
		step6();
		step7();
		step8();
	}

	@Override
	public String getName() {
		return "ZagadkiBrzezdoma";
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Spotkałem Brzezdoma na łąkach Kościeliska.");
		res.add("Nurtuje go pytanie kto dał pas i kierpce Janosikowi. Mam mu też przynieść kierpce.");
		if ("rejected".equals(questState)) {
			res.add("Nie mam głowy do zagadek, może innym razem.");
			return res;
		} 
		if ("start".equals(questState)) {
			return res;
		} 
		res.add("Odpowiedziałem Brzedomowi na pytanie, dostarczyłem też kierpce. Starego Bace nurtuje pytanie kogo spotkał juhas Kuba. Mam do niego iść z odpowiedzią.");
		if ("baca".equals(questState)) {
			return res;
		} 
		res.add("Stary baca poprosił mnie abym poszedł do kowala Andrzeja i powiedział mu jaki jest najwiekrzy szczyt w Tatrach.");
		if ("gerlach".equals(questState)) {
			return res;
		}
		res.add("Kowal Andrzej kazał mi zanieść Marynie gorset i powiedziec imie i nazwisko lub pseudonim najwiekrzego bajkopisarza Podhala.");
		if ("gorset".equals(questState)) {
			return res;
		}
		res.add("Maryna poprosiła mnie abym znalazł odpowiedź na pytanie 'dlaczego w Zakopanym nie rośnie kapusta'. Odpowiedzi mam udzielić Bogusiowi.");
		if ("kapusta".equals(questState)) {
			return res;
		}
		res.add("Boguś Kazał mi iść do Edgarda. Znajdę go na ziemiach ZRC. Mam mu powiedzieć imie odpowiednika Zeusa w wierzeniach słowiańskich.");
		if ("bogowie".equals(questState)) {
			return res;
		}
		res.add("Edgart kazał zdoobyć mi czarną zbroję i zanieść ją do Bercika.");
		if ("armor".equals(questState)) {
			return res;
		}
		res.add("Dostałem od Bercika za zbroje sztabki złota.");
		if (isCompleted(player)) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	@Override
	public int getMinLevel() {
		return 60;
	}
	@Override
	public String getNPCName() {
		return "Brzezdom";
	}
}
