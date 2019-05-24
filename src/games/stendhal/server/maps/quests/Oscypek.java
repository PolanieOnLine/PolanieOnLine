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
// Based on few tasks. Zwracac uwage na zmiany w plikach o sprzedazy owcy, mrs.yeti(biega o czas)

package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class Oscypek extends AbstractQuest {

	private static final String QUEST_SLOT = "oscypek";
	private static final int DELAY_IN_MINUTES = 60*3;
	private static Logger logger = Logger.getLogger(Oscypek.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step1() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Ano w gospodarstwie zawsze coś się znajdzie do zrobienia. Chcesz być honielnikiem, a potem juhasem, czyli moim pomocnikiem i pomagać przy puceniu oscypków?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za pomoc! Dobrze spisałeś się",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Najpierw trza sprawdzić czy nadajesz się do tej roboty, a powiadam ci - łatwa nie jest! Nie ma nic za darmo." +
				" Na początek mogę sprzedać ci owcę ino powiedz #buy #sheep. Idź z nią na pastwisko i wróć jak będzie wystarczająco duża. ",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"To co mi głowę zawracasz? Idźze se lepiej bąki zbijać!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -20.0));
	}

	private void step2() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");
		npc.add(ConversationStates.ATTENDING, Arrays.asList("sheep", "owca"),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final Sheep sheep = player.getSheep();
						if (sheep != null) {
							if (npc.getEntity().squaredDistance(sheep) > 5 * 5) {
								npc.say("Nie widzę stąd tej owcy! Przyprowadź ją bliżej, abym mógł ocenić jej wagę.");
							} else if (sheep.getWeight() < 100) {
								// prevent newbies from selling their sheep too early
								npc.say("Ej, lichy z ciebie honielnik! Owca jeszcze nie jest dobrze wypasiona");
								player.addKarma(-10);
							} else {
								npc.say("No! Takiego juhasa trza mi było. Teraz pora na jej dojenie. Gieletę, czyli wiaderko mam, ale biegnij za ten czas do Kościeliska. Jest tam #kowal #Jacek. U niego jest moja #puciera.");
								//sheep.getZone().remove(sheep);
								player.removeSheep(sheep);
								sheep.getZone().remove(sheep);
								player.notifyWorldAboutChanges();
								player.addKarma(15);
								player.addXP(500);
								player.setQuest(QUEST_SLOT, "inter1");
							}
						} else {
							npc.say("" + player.getTitle() + " nie posiadasz owcy! W co próbujesz pogrywać?");
						}
					};
				});
	}


	private void interpretacion1() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("kowal jacek", "kowal Jacek"),
				new QuestInStateCondition(QUEST_SLOT, "inter1"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					npc.say("U kowala Jacka jest moja #puciera. Wczoraj się oberwała i pogięła. Biegnij po nią wartko!");
					player.setQuest(QUEST_SLOT, "puciera_make");
				};
		});

		npc.add(ConversationStates.ATTENDING, "puciera",
				new QuestInStateCondition(QUEST_SLOT, "puciera_make"),
				ConversationStates.ATTENDING, "Puciera to wielki garniec, w którym po dodaniu klagu z cielęcego żołądka mleko ścina się na bunc - biały ser owczy i żyntycę."
				+ " #Kowal #Jacek miał mi ją naprawić, bo wczoraj się oberwała i pogięła.",
				null);
	}


	private void odbior1() {
		final SpeakerNPC npc = npcs.get("Kowal Jacek");

		npc.add(ConversationStates.ATTENDING, "puciera",
				new QuestInStateCondition(QUEST_SLOT, "puciera_make"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final Item puciera = SingletonRepository.getEntityManager().getItem("puciera");
						puciera.setBoundTo(player.getName());
						player.equipOrPutOnGround(puciera);
						player.addKarma(10);
						player.addXP(150);
						npc.say("Witaj! Skończyłem go, zanieś wartko ten garniec do bacy");
						player.setQuest(QUEST_SLOT, "puciera_done");
					};
				});
	}


	private void step3() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");

		npc.add(ConversationStates.ATTENDING, "puciera",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "puciera_done"),
				new PlayerHasItemWithHimCondition("puciera")),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.drop("puciera");
					player.addKarma(10);
					player.addXP(150);
					npc.say("Aleś długo szedł! O mało co mleko się nie popsuło! Leć jeszcze raz do Kościeliska. Na południe obok doliny Kościeliskiej mieszka #gaździna #Maryśka." +
					" Jest u niej moja wyprana #grudziarka. Po drodze nazbieraj trochę drewna do rozpalenia watry!");
					player.setQuest(QUEST_SLOT, "inter2");
				};
		});

		npc.add(
			ConversationStates.ATTENDING, "puciera",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "puciera_done"), new NotCondition(new PlayerHasItemWithHimCondition("puciera"))),
			ConversationStates.ATTENDING,
			"Mierny z ciebie pomocnik! Gdzie #puciera? Miał mi ją naprawić #Kowal #Jacek z Kościeliska! Lepiej sie bez niej nie pokazuj, bo mleko się zmarnuje!",
			null);

		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "puciera_done"),
				ConversationStates.ATTENDING,
				"Prosiłem Ciebie o przyniesienie puciery!",
				null);
	}


	private void interpretacion2() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");

		npc.add(ConversationStates.ATTENDING, "grudziarka",
				new QuestInStateCondition(QUEST_SLOT, "inter2"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					npc.say("To ścierka płócienna do przecedzania ściętego mleka. Dla lepszego smaku buncu i oscypków kładzie sie na niej gałązkę świerkową."
				+ " Gaździna Maryśka miała mi ją uprać! Pośpiesz się!");
					player.setQuest(QUEST_SLOT, "grudziarka_make");
				};
		});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("gaździną Maryśką", "gaździną maryśką", "gazdzina marysika", "gaździna", "maryśka", "Gaździna Maryśka"),
				new QuestInStateCondition(QUEST_SLOT, "inter2"),
				ConversationStates.ATTENDING,
				"Maryśka to moja baba! Ale nie przystawiaj się do niej! Niech cię interesuję ino moja #grudziarka!",
				null);
	}


	private void odbior2() {
		final SpeakerNPC npc = npcs.get("Gaździna Maryśka");

		npc.add(ConversationStates.ATTENDING, "grudziarka",
				new QuestInStateCondition(QUEST_SLOT, "grudziarka_make"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final Item grudziarka = SingletonRepository.getEntityManager().getItem("grudziarka");
						grudziarka.setBoundTo(player.getName());
						player.equipOrPutOnGround(grudziarka);
						player.addKarma(10);
						player.addXP(150);
						npc.say("Trzymaj, jest uprana więc jej nie upapraj!");
						player.setQuest(QUEST_SLOT, "grudziarka_done");
					};
				});
	}


	private void step4() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");

		npc.add(ConversationStates.ATTENDING, "grudziarka",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "grudziarka_done"),
				new PlayerHasItemWithHimCondition("grudziarka")),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						player.drop("grudziarka");
						player.addKarma(10);
						player.addXP(150);
						npc.say("Jesteś wreszcie! Dziękuję za grudziarkę. Teraz muszę przecedzić ścięte mleko i pucyć, czyli ugnieść oscypki w drewnianej formie. Potem będą się wędzić nad watrą. Masz #drewno?");
						player.setQuest(QUEST_SLOT, "drewno");
					};
				});

		npc.add(
			ConversationStates.ATTENDING, "grudziarka",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "grudziarka_done"), new NotCondition(new PlayerHasItemWithHimCondition("grudziarka"))),
			ConversationStates.ATTENDING,
			"A gdzie #grudziarka? Pewnieś zabaciarzył z moją babą Maryśką i zapomniał co masz przynieść!",
			null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "grudziarka_done"),
				ConversationStates.ATTENDING,
				"Prosiłem Ciebie o przyniesienie #grudziarki!",
				null);
	}


	private void step5() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("polano", "drewno"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "drewno"),
				new PlayerHasItemWithHimCondition("polano")),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.drop("polano");
					player.addKarma(10);
					player.addXP(100);
					npc.say("Dziękuję. Wróć do mnie za kilka godzin, kiedy #oscypki będą gotowe.");
					player.setQuest(QUEST_SLOT, "oscypek;"+System.currentTimeMillis());
				};
		});

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("polano", "drewno"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "drewno"), new NotCondition(new PlayerHasItemWithHimCondition("polano"))),
			ConversationStates.ATTENDING,
			"To jak mam rozpalić watrę? Przynieś szybko drewno!",
			null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "drewno"),
				ConversationStates.ATTENDING,
				"Prosiłem Ciebie o przyniesienie #'drewna'!",
				null);
		}


	private void step6() {
		final SpeakerNPC npc = npcs.get("Baca Zbyszek");

		final String extraTrigger = "oscypki";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "oscypek"),
			// delay is in minutes, last parameter is argument of timestamp
			new NotCondition(new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES))),
			ConversationStates.ATTENDING,
			null,
			new SayTimeRemainingAction(QUEST_SLOT,1,DELAY_IN_MINUTES, "Jeszcze nie skończyłem wyrabiania oscypków wróć za"));

		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "oscypek"),
			// delay is in minutes, last parameter is argument of timestamp
			new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES)),
			ConversationStates.ATTENDING,
			"Dziękuję! Nadajesz się na juhasa! Oto twoja nagroda! Pyszne, wędzone oscypki i żyntyca, która została z cedzenia ściętego mleka! Smacznego!",
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final StackableItem oscypek = (StackableItem) SingletonRepository.getEntityManager().getItem("oscypek");
					final StackableItem zyntyca = (StackableItem) SingletonRepository.getEntityManager().getItem("żyntyca");
					final int oscypekamount = 20;
					final int zyntycaamount = 10;
					oscypek.setQuantity(oscypekamount);
					zyntyca.setQuantity(zyntycaamount);
					player.equipOrPutOnGround(oscypek);
					player.equipOrPutOnGround(zyntyca);
					player.addXP(6500);
					player.addKarma(50);
					player.setQuest(QUEST_SLOT, "done");
				};
			});
		}


	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Oscypek",
			"Pomoc bacy Zbyszkowi w zrobieniu oscypka.",
			false);

		step1();
		step2();
		interpretacion1();
		odbior1();
		step3();
		interpretacion2();
		odbior2();
		step4();
		step5();
		step6();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("W Zakopanem spotkałem Bace Zbyszka.");
		res.add("Zaproponował mi abym został jego juhasem. Mam pilnować aby owca miała 100 wagi.");
		if ("rejected".equals(questState)) {
			res.add("Nie mam ochoty pilnować owiec.");
			return res;
		}
		if ("start".equals(questState)) {
			return res;
		}
		res.add("Baca Zbyszek potrzebuje puciery do pucenia oscypków. Mam mu pomóc w jej zdobyciu.");
		if ("inter1".equals(questState)) {
			return res;
		}
		res.add("Podobno kowal Jack robi je. Udam się do niego po nią.");
		if ("puciera_make".equals(questState)) {
			return res;
		}
		res.add("Kowal Jacek dał mi puciere i kazał pozdrowić Bace Zbyszka.");
		if ("puciera_done".equals(questState)) {
			return res;
		}
		res.add("Baca Zbyszek podziękował mi za puciere.");
		if ("inter2".equals(questState)) {
			return res;
		}
		res.add("Baca Zbyszek nie ma czystej grudziarki. Prosił mnie abym udał się do jego żony Gaździny Maryśki po nią.");
		if ("grudziarka_make".equals(questState)) {
			return res;
		}
		res.add("Gaździna Maryśka dała mi czystą grudziarkę i ostrzegła abym jej nie wybrudził.");
		if ("grudziarka_done".equals(questState)) {
			return res;
		}
		res.add("Zaniosłem grudziarkę do Bacy Zbyszka. Mam mu jeszcze donieść drewna.");
		if ("drewno".equals(questState)) {
			return res;
		}
		res.add("Przyniosłem drewno. Baca zajoł się wyrabianiem oscypków.");
		if (questState.startsWith("oscypek")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam zgłosić się za 3 godziny do Bacy Zbyszka po nagrodę za moją pomoc.");
				} else {
				res.add("Dopiero za 3 godziny będę mógł odebrać ngrodę.");
			}
			return res;
		}
		res.add("Baca Zbyszek powiedził, że takiego juhasa pracowitego dawno nie widził. Jako zapłatę dał mi 20 oscypków i 10 żyntycy.");
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
		return "Oscypek";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return "Baca Zbyszek";
	}
}
