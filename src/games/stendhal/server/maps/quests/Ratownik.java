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
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class Ratownik extends AbstractQuest {

	private static final String QUEST_SLOT = "ratownik";
	private static Logger logger = Logger.getLogger(Ratownik.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step1() {
		final SpeakerNPC npc = npcs.get("Ratownik Mariusz");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"W górach zaginął jeden z turystów. Muszę zorganizować akcję ratowniczą. Wszyscy ratownicy już są oprócz juhasa. Możesz go powiadomić?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za pomoc! Pamiętaj wychodząc na szlak zostaw wiadomość w schronisku, gdzie idziesz i którą trasą. To zaoszczędzi nam w razie akcji sporo czasu.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Super! Słyszałem, że udał się w stronę Kościeliska. #Stary #baca może będzie coś wiedział.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 20.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Jak chcesz, ale pamiętaj ty też możesz w górach zabłądzić!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -20.0));

		npc.add(ConversationStates.ATTENDING, "Stary baca",
				null,
				ConversationStates.ATTENDING, "Znajdziesz go we wschodniej części  Kościeliska. Sprzedaje owczarki podhalańskie. Powiedz mu szukam Juhasa. A jak spotkasz Juhasa powiedz, że mamy akcję.",
				null);
	}

	private void step2() {
		final SpeakerNPC npc = npcs.get("Stary Baca");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Szukasz Juhasa? Ostatnio go nie widziałem. Może szedł drugą stroną doliny. Wiem, że to ulubiony szlak #Brzezdoma. Spytaj jego.");
					player.addKarma(10);
					player.addXP(250);
					player.setQuest(QUEST_SLOT, "krok_brzezdom");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("Brzezdoma", "brzezdom"),
			new QuestInStateCondition(QUEST_SLOT, "krok_brzezdom"),
			ConversationStates.ATTENDING, "Za tym lasem często spaceruje. Musisz obejść ten las w koło.",
			null);
	}

	private void step3() {
		final SpeakerNPC npc = npcs.get("Brzezdom");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_brzezdom"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Przykro mi ale tędy nie szedł. Poradzę ci coś, idź ty do gospody i spytaj #Jagny."
					+ " Kto jak kto ale ona powinna coś wiedzieć. Normalnie jak ktoś idzie w góry to zostawia wiadomość, gdzie idzie i którą trasą.");
					player.addKarma(15);
					player.addXP(750);
					player.setQuest(QUEST_SLOT, "krok_jagna");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("Jagna", "Jagny"),
				new QuestInStateCondition(QUEST_SLOT, "krok_jagna"),
				ConversationStates.ATTENDING, "Nie znasz Jagny? Jest kelnerką w gospodzie we wschodniej części Zakopanego.",
				null);
	}

	private void step4() {
		final SpeakerNPC npc = npcs.get("Jagna");

		npc.add(ConversationStates.ATTENDING,Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_jagna"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Był tu ladaco jeden jakieś dwa dni temu. Z tego co wiem miał odwiedzić #Wielkoluda. ");
					player.addKarma(10);
					player.addXP(450);	
					player.addXP(200);
					player.setQuest(QUEST_SLOT, "krok_wielkolud");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("wielkolud", "wielkoluda"),
				new QuestInStateCondition(QUEST_SLOT, "krok_wielkolud"),
				ConversationStates.ATTENDING, "Wielkoluda znajdziesz na wzgórzu w północno-wschodniej części Kościeliska. Uważaj tam na lawiny aby cię nie ubiły.",
				null);
	}

	private void step5() {
		final SpeakerNPC npc = npcs.get("Wielkolud");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_wielkolud"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Tak był tu. Ucięliśmy sobie pogawędkę o tym i tamtym, gdy nagle zaatakowały nas te przeklęte pokutniki. Odniósł drobne obrażenia i udał się do #szpitala w Zakopanem.");
					player.addKarma(20);
					player.addXP(1200);
					player.setQuest(QUEST_SLOT, "krok_szpital");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("szpital", "szpitala"),
			new QuestInStateCondition(QUEST_SLOT, "krok_szpital"),
			ConversationStates.ATTENDING, "Znajdziesz go powyżej sklepu Radzimira ma charakterystyczny krzyż na szyldzie.",
			null);
	}

	private void step6() {
		final SpeakerNPC npc = npcs.get("Boguś");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_szpital"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Przykro mi nie widziałem go ale spytaj #zielarki.");
					player.setQuest(QUEST_SLOT, "krok_zielarka");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("zielarka", "zielarki"),
			new QuestInStateCondition(QUEST_SLOT, "krok_zielarka"),
			ConversationStates.ATTENDING, "Idź korytarzem w prawo. To gaździna Jadźka jest przecież naszą zielarką.",
			null);
	}

	private void step7() {
		final SpeakerNPC npc = npcs.get("Gaździna Jadźka");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_zielarka"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Poczekaj, hm... Tak koło 9 godziny przyszedł, miał lekkie obrażenia. Podobno go i Wielkoluda pokutniki napadły."
						+ " Opatrzyłam mu rany i miał lecieć do #gaździny  #Maryśki.");
					player.addKarma(5);
					player.addXP(200);
					player.setQuest(QUEST_SLOT, "krok_maryska");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("gaździna Maryśka", "gazdzina maryska", "gaździny Maryśki", "gazdziny maryski"),
			new QuestInStateCondition(QUEST_SLOT, "krok_maryska"),
			ConversationStates.ATTENDING, "No jak! nie mów, że nie znasz? Toć to żona bacy Zbyszka, który sprzedaje owce. Chałupę mają na Kościelisku a ich sąsiadem jest stary baca.",
			null);
	}

	private void step8() {
		final SpeakerNPC npc = npcs.get("Gaździna Maryśka");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_maryska"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Oczywiście, że był powsinogą jeden. Wpadł jak piorun z jasnego nieba i wypadł nawet dwóch zdań z nim nie zamieniłam. Potrzebował coś od mojego męża bacy #Zbyszka.");
					player.addKarma(10);
					player.addXP(350);		
					player.addXP(150);
					player.setQuest(QUEST_SLOT, "krok_zbyszek");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("Zbyszek", "Zbyszka"),
			new QuestInStateCondition(QUEST_SLOT, "krok_zbyszek"),
			ConversationStates.ATTENDING, "Znajdziesz go w bacówce na południu Zakopanego. Możliwe że Juhas jeszcze tam jest.",
			null);
	}

	private void step9() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_zbyszek"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Nie dalej jak trzy godziny temu poszedł sobie. Mówił coś, że ma u #Gerwazego coś do załatwienia.");
					player.addKarma(10);
					player.addXP(300);
					player.setQuest(QUEST_SLOT, "krok_gerwazy");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("Gerwazego","gerwazy"),
			new QuestInStateCondition(QUEST_SLOT, "krok_gerwazy"),
			ConversationStates.ATTENDING, "Gerwazy jest nosiwodą tu w Zakopanem.",
			null);
	}

	private void step10() {
		final SpeakerNPC npc = npcs.get("Nosiwoda Gerwazy");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_gerwazy"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("A i owszem był u mnie. Napełniłem mu cztery bukłaki wodą, pogadaliśmy sobie chwilę i #poszedł sobie. ");
					player.addKarma(10);
					player.addXP(300);
					player.setQuest(QUEST_SLOT, "krok_fryderyk");
				};
			});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("gone", "poszedł", "poszedl"),
			new QuestInStateCondition(QUEST_SLOT, "krok_fryderyk"),
			ConversationStates.ATTENDING, "Wspominał, że dwa bukłaki wody musi oddać #Fryderykowi.",
			null);

		npc.add(ConversationStates.ATTENDING, Arrays.asList("Fryderykowi", "fryderyk"),
			new QuestInStateCondition(QUEST_SLOT, "krok_fryderyk"),
			ConversationStates.ATTENDING, "Spaceruje niedaleko Poczty tu w Zakopanem.",
			null);
	}

	private void step11() {
		final SpeakerNPC npc = npcs.get("Fryderyk");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("searching juhas", "searching", "juhas", "szukam juhasa", "szukam", "juhasa"),
			new QuestInStateCondition(QUEST_SLOT, "krok_fryderyk"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("A jakże był tu nie tak #dawno.");
					player.addKarma(10);
					player.addXP(300);
					player.setQuest(QUEST_SLOT, "krok_juhas");
				};
			});

		npc.add(ConversationStates.ATTENDING, "dawno",
			new QuestInStateCondition(QUEST_SLOT, "krok_juhas"),
			ConversationStates.ATTENDING, "Czy ja wiem? Jakieś 30 min temu. Udał się do miasta #Semos.",
			null);

		npc.add(ConversationStates.ATTENDING, "Semos",
				new QuestInStateCondition(QUEST_SLOT, "krok_juhas"),
				ConversationStates.ATTENDING, "Dojdziesz tam idąc tu przez most z lewej strony wyciągu po środku jest przez niego kładka. Później trzymaj się cały czas prawej strony lasu."
											+ " Musisz w ten las wejść. W środku niego jest tajemne przejście. W Semos kieruj swe kroki do gospody.",
				null);
	}

	private void step12() {
		final SpeakerNPC npc = npcs.get("Juhas");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("action", "akcja", "akcję", "akcje", "gopr", "ratunek", "mariusz", "ratownik mariusz"),
			new QuestInStateCondition(QUEST_SLOT, "krok_juhas"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Mówisz akcję ratowniczą zarządził Mariusz. Idź i powiedz mu, że jestem #niedysponowany."
								+ " Jak widzisz trochę wypiłem. A po pijanemu nie wolno w góry wybierać się.");
					player.addKarma(15);
					player.addXP(700);
					player.setQuest(QUEST_SLOT, "krok_mariusz");
				};
			});
	}

	private void step13() {
		final SpeakerNPC npc = npcs.get("Ratownik Mariusz");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("seedy", "niedysponowany"),
			new QuestInStateCondition(QUEST_SLOT, "krok_mariusz"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("No tak jak wypił to absolutnie nie wolno iść mu z nami na akcję. Niech pomyślę jak cię wynagrodzić?"
								+ " Trzymaj ten pazur. Dostałem go od przyjaciela, mi się do niczego nie przyda a ty podobno zbierasz takie cacka."
								+ " I pamiętaj w górach nie ma żartów.");
					final Item odznaka = SingletonRepository.getEntityManager().getItem("pazur zielonego smoka");
					odznaka.setBoundTo(player.getName());
					player.equipOrPutOnGround(odznaka);
					player.setBaseHP(10 + player.getBaseHP());
					player.heal(10, true);
					player.addKarma(25);
					player.addXP(2950);
					player.setQuest(QUEST_SLOT, "done");
				};
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Ratownik",
			"Pomoc w zebraniu ratowników.",
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
		step11();
		step12();
		step13();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Spotkałem ratownika Mariusza na obrzeżach Zakopanego");
		res.add("Ratownik Mariusz poprosił mnie abym poszukał Juhasa. Ktoś zaginął w górach i muszą zorganizować akcję ratowniczą. Podobno widziano go u starego Bacy.");
		if ("rejected".equals(questState)) {
			res.add("Nie mam czasu na szukanie Juhasa..");
			return res;
		}
		if ("start".equals(questState)) {
			return res;
		}
		res.add("Znalazłem starego Bacę ale niestety Juhasa tam nie było. Poradził mi abym udał sie do Brzezdoma.");
		if ("krok_brzezdom".equals(questState)) {
			return res;
		}
		res.add("Odnalazłem Brzezdoma. Niestety Juhasa tam też nie było. Dostałem typ abym udał się do gospody w Zakopanem i spytał Jagny o niego.");
		if ("krok_jagna".equals(questState)) {
			return res;
		}
		res.add("Do gospody dotarłem zbyt puźno. Jagna powiedziała mi aby iść do Wielkoluda. Podobno tam udał się Juhas.");
		if ("krok_wielkolud".equals(questState)) {
			return res;
		}
		res.add("Wielkolud poinformował mnie iż Juhas został napadnięty. Odniósł rany więc udał się do szpitala w Zakopanem.");
		if ("krok_szpital".equals(questState)) {
			return res;
		}
		res.add("Boguś był pierwszą osobą, którą spotkałem w szpitalu. Odesłał mnie do Gażdziny Jadźki.");
		if ("krok_szpital".equals(questState)) {
			return res;
		}
		res.add("Gaździna Jadźka poinformowała mnie, że obrażenia Juhasa nie były duże. Opatrzyła mu rany a ten udał się do gaździny Marysiki.");
		if ("krok_zielarka".equals(questState)) {
			return res;
		}
		res.add("Juhas u gaździny Marysiki był krótko, udał się do Bacy Zbyszka..");
		if ("krok_maryska".equals(questState)) {
			return res;
		}
		res.add("Baca Zbyszek kazał mi udać się do nosiwody Gerwazego.");
		if ("krok_zbyszek".equals(questState)) {
			return res;
		}
		res.add("Nosiwoda Gerwazy oznajmił mi, że Juhas był u niego po wodę. Potem udał się do Fryderyka.");
		if ("krok_gerwazy".equals(questState)) {
			return res;
		}
		res.add("Fryderyk widział Juhasa ale przez chwilę. Podobno poszedł do Semos. Mam zajrzeć do gospody w Semos.");
		if ("krok_fryderyk".equals(questState)) {
			return res;
		}
		res.add("No i znalazłem Juhasa. Niestety ten był niedysponowany. Muszę wrócić do ratownika Mariusza i mu o tym powiedzieć.");
		if ("krok_mariusz".equals(questState)) {
			return res;
		}
		res.add("Ratownik Mariusz wysłuchał co mam do powiedzenia i w nagrodę za moje zaangażowanie dał mi pazur zielonego smoka.");
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
	public String getName() {
		return "Ratownik";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ratownik Mariusz";
	}
}
