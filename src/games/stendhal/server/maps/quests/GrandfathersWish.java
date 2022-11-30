/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EnableFeatureAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.grandfatherswish.MylingSpawner;

/**
 * Quest to increase number of bag slots.
 *
 * NPCs:
 * - Elias Breland
 * - Niall Breland
 * - Marianne
 * - Priest Calenus
 *
 * Required items:
 * - rope ladder
 * - holy water
 *
 * Reward:
 * - 5000 XP
 * - 500 karma
 * - 3 more bag slots
 */
public class GrandfathersWish extends AbstractQuest {
	public static final String QUEST_SLOT = "grandfatherswish";
	private static final int min_level = 100;

	private final SpeakerNPC elias = npcs.get("Elias Breland");

	private static MylingSpawner spawner;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Życzenia Dziadka";
	}

	@Override
	public String getRegion() {
		return Region.DENIRAN;
	}

	@Override
	public String getNPCName() {
		return elias.getName();
	}

	@Override
	public int getMinLevel() {
		return min_level;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final String[] states = player.getQuest(QUEST_SLOT).split(";");
		final String quest_state = states[0];
		String find_myling = null;
		String holy_water = null;
		String cure_myling = null;
		for (final String st: states) {
			if (st.startsWith("find_myling:")) {
				find_myling = st.split(":")[1];
			}
			if (st.startsWith("holy_water:")) {
				holy_water = st.split(":")[1];
			}
			if (st.startsWith("cure_myling:")) {
				cure_myling = st.split(":")[1];
			}
		}

		final List<String> res = new ArrayList<>();
		res.add(elias.getName() + " chce wiedzieć, co stało się z jego wnukiem, z którym"
			+ " żył w separacji.");

		if (quest_state.equals("rejected")) {
			res.add("Nie mam czasu na zniedołężniałych starców.");
		} else {
			res.add("Zgodziłem się na dochodzenie.");
			if (find_myling != null) {
				res.add("Marianne wspomniała, że ​​Niall chciał zbadać"
					+ " cmentarz w pobliżu Semos City.");
				if (find_myling.equals("done")) {
					res.add("Niall został zmieniony w mylinga. Elias będzie"
						+ " zdruzgotany. Ale muszę mu powiedzieć.");
				}
			}
			if (holy_water != null) {
				res.add("Być może jest jeszcze nadzieja. Muszę znaleźć kapłana i poprosić"
					+ " o wodę święconą, aby pomóc przywrócić Nialla do normalnego stanu.");
				if (!holy_water.equals("start")) {
					res.add("Odnalazłem kapłana Calenusa. Poprosił mnie o zebranie kilku rzeczy. Potrzebuje"
						+ " flaszy z wodą oraz nieco węgla drzewnego.");
					if (holy_water.equals("done")) {
						res.add("Kapłan dał mi butelkę wody święconej. "
							+ " Teraz muszę użyć tego na Niallu.");
					}
				}
			}
			if (cure_myling != null && cure_myling.equals("done")) {
				res.add("Użyłem wody święconej. Niall wyzdrowiał! Teraz"
					+ " powinienem zabrać go z powrotem do dziadka.");
			}
			if (quest_state.equals("done")) {
				res.add("Elias i jego wnuk ponownie się"
					+ " spotkali.");
			}
		}

		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Życzenia Dziadka",
			elias.getName() + " jest zasmucony utratą wnuka.",
			false
		);
		prepareRequestStep();
		prepareMarianneStep();
		prepareFindPriestStep();
		prepareHolyWaterStep();
		prepareCompleteStep();
		prepareMylingSpawner();
	}

	private void prepareRequestStep() {
		// requests quest but does not meet minimum level requirement
		elias.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelLessThanCondition(min_level)),
			ConversationStates.ATTENDING,
			"Mój wnuk zniknął ponad rok temu. Ale potrzebuję pomocy bardziej"
				+ " doświadczonego poszukiwacza przygód.",
			null);

		// requests quest
		elias.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new NotCondition(new LevelLessThanCondition(min_level))),
			ConversationStates.QUEST_OFFERED,
			"Mój wnuk zniknął ponad rok temu. Boję się najgorszego i prawie"
				+ " porzuciłem wszelką nadzieję. Ile bym dała, żeby tylko"
				+ " wiedzieć, co się z nim stało! Jeśli dowiesz się czegoś,"
				+ " przyniesiesz mi wieści?",
			null);

		// already accepted quest
		elias.add(
			ConversationStates.ANY,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dziękuję za przyjęcie mojej prośby o pomoc. Proszę, powiedz mi,"
				+ " jeśli usłyszysz jakieś wieści o tym, co stało się z moim wnukiem."
				+ " Bawił się z małą dziewczynką o imieniu #Marianne.",
			null);

		// already completed quest
		elias.add(
			ConversationStates.ANY,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dziękuję za zwrócenie mi wnuka. Jestem przepełniony radością!",
			null);

		// rejects quest
		elias.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Niestety? Co się stało z moim wnukiem!?",
			new MultipleActions(
				new SetQuestAction(QUEST_SLOT, "rejected;;;"),
				new DecreaseKarmaAction(15)));

		// accepts quest
		elias.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			ConversationStates.ATTENDING,
			"Och dziękuje! Mój wnuk ma na imię #Niall. Możesz porozmawiać"
				+ " z #Marianne. Kiedyś razem się bawili.",
			new MultipleActions(
				new SetQuestAction(QUEST_SLOT, "investigate;;;"),
				new IncreaseKarmaAction(15)));

		// ask about Niall
		elias.add(
			ConversationStates.ANY,
			Arrays.asList("Niall", "grandson", "wnuk"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Niall jest moim wnukiem. Jestem zrozpaczony jego"
				+ " zniknięciem. Zapytaj dziewczynę #Marianne. Często"
				+ " razem się bawili.",
			null);

		// ask about Marianne
		elias.add(
			ConversationStates.ANY,
			"Marianne",
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Marianne mieszka tutaj, w Deniran. Zapytaj ją o #Niall.",
			null);
	}

	private void prepareMarianneStep() {
		final SpeakerNPC marianne = npcs.get("Marianne");
		final ChatCondition investigating = new QuestActiveCondition(QUEST_SLOT);

		marianne.add(
			ConversationStates.ATTENDING,
			"Niall",
			investigating,
			ConversationStates.ATTENDING,
			"Och! Mój przyjaciel Niall! Nie widziałam go od dawna. Za"
				+ " każdym razem, gdy idę do domu jego dziadka, żeby się #bawić,"
				+ " nie ma go w domu.",
			new NPCEmoteAction("nagle wygląda bardzo melancholijnie.", false));

		marianne.add(
			ConversationStates.ATTENDING,
			Arrays.asList("play", "bawić"),
			investigating,
			ConversationStates.ATTENDING,
			"Nie tylko świetnie się z nim bawiło, ale był też bardzo pomocny."
				+ " Pomagał mi zbierać kurze jaja, kiedy za bardzo się"
				+ " #bałam zrobić to sama.",
			new NPCEmoteAction("wygląda jeszcze bardziej melancholijnie.", false));

		marianne.add(
			ConversationStates.ATTENDING,
			Arrays.asList("afraid", "bałam"),
			investigating,
			ConversationStates.ATTENDING,
			"Wiesz, co mi kiedyś powiedział? Powiedział, że chce jechać aż"
				+ " do Semos, żeby zobaczyć tam #cmentarz. Nieee! Nie ma mowy! To"
				+ " brzmi bardziej przerażająco niż kurczaki.",
			new MultipleActions(
				new NPCEmoteAction("drży.", false),
				new SetQuestAction(QUEST_SLOT, 1, "find_myling:start")));

		marianne.add(
			ConversationStates.ATTENDING,
			Arrays.asList("graveyard", "cemetary", "cmentarz"),
			investigating,
			ConversationStates.ATTENDING,
			"Mam nadzieję, że nie poszedł na ten straszny cmentarz. Kto wie,"
				+ " jakie tam są potwory.",
			null);

		marianne.add(
			ConversationStates.ATTENDING,
			"Niall",
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Słyszałam, że Niall wrócił do domu! Na pewno nie było go długo."
				+ " Cieszę się, że jest już bezpieczny w domu.",
			new NPCEmoteAction("odetchneła z ulgą.", false));
	}

	private void prepareFindPriestStep() {
			final ChatCondition found_myling = new QuestInStateCondition(QUEST_SLOT, 1, "find_myling:done");

			// tells Elias that Niall has been turned into a myling
			elias.add(
				ConversationStates.ANY,
				Arrays.asList("Niall", "myling"),
				found_myling,
				ConversationStates.ATTENDING,
				"O nie! Mój drogi wnuku! Gdyby tylko istniał sposób, aby #zmienić"
					+ " go z powrotem.",
				null);

			elias.add(
				ConversationStates.ANY,
				Arrays.asList("change", "zmienić"),
				found_myling,
				ConversationStates.ATTENDING,
				"Czekać! Słyszałem, że #'woda święcona' ma specjalne właściwości,"
					+ " gdy jest używana na nieumarłych. Może jakiś #kapłan by miał."
					+ " Proszę, idź i znajdź kapłana.",
				new SetQuestAction(QUEST_SLOT, 2, "holy_water:start"));

			elias.add(
				ConversationStates.ANY,
				Arrays.asList("Niall", "myling", "priest", "holy water", "kapłan", "ksiądz", "woda święcona"),
				new QuestInStateCondition(QUEST_SLOT, 2, "holy_water:start"),
				ConversationStates.ATTENDING,
				"Proszę! Znajdź kapłana. Może ktoś może zapewnić wodę święconą,"
					+ " aby pomóc mojemu wnukowi.",
				null);
	}

	private void prepareHolyWaterStep() {
		final SpeakerNPC priest = npcs.get("Priest Calenus");

		final ChatCondition canRequestHolyWater = new AndCondition(
			new QuestActiveCondition(QUEST_SLOT),
			new NotCondition(new PlayerHasInfostringItemWithHimCondition("woda święcona z popiołem", "Niall Breland")),
			new OrCondition(
				new QuestInStateCondition(QUEST_SLOT, 2, "holy_water:start"),
				new QuestInStateCondition(QUEST_SLOT, 2, "holy_water:done"))
		);

		final ChatAction equipWithHolyWater = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				final Item holy_water = SingletonRepository.getEntityManager().getItem("woda święcona z popiołem");
				holy_water.setDescription("Oto butelka wody święconej posypanej popiołem do uleczenia Nialla.");
				holy_water.setInfoString("Niall Breland");
				holy_water.setBoundTo(player.getName());

				player.equipOrPutOnGround(holy_water);
			}
		};

		priest.add(
			ConversationStates.ATTENDING,
			Arrays.asList("holy water", "myling", "Niall", "Elias", "woda święcona"),
			canRequestHolyWater,
			ConversationStates.ATTENDING,
			"O mój! Młody chłopak zmienił się w mylinga? Mogę pomóc,"
				+ " ale to będzie wymagało specjalnej wody święconej."
				+ " Przynieś mi butelkę wody oraz nieco węgla drzewnego.",
			new SetQuestAction(QUEST_SLOT, 2, "holy_water:bring_items"));

		priest.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 2, "holy_water:bring_items"),
				new OrCondition(
						new NotCondition(new PlayerHasItemWithHimCondition("butelka wody")),
						new NotCondition(new PlayerHasItemWithHimCondition("węgiel drzewny")))),
			ConversationStates.ATTENDING,
			"Pospiesz się, potrzebuję flaszę wody oraz trochę węgla drzewnego, abym mógł pobłogosławić.",
			null);

		priest.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 2, "holy_water:bring_items"),
				new PlayerHasItemWithHimCondition("butelka wody"),
				new PlayerHasItemWithHimCondition("węgiel drzewny")),
			ConversationStates.ATTENDING,
			"Doskonale! Pobłogosławiłem wodę. Idź i użyj go, by przywrócić młodzieńca.",
			new MultipleActions(
				new DropItemAction("butelka wody"),
				new DropItemAction("węgiel drzewny"),
				equipWithHolyWater,
				new SetQuestAction(QUEST_SLOT, 2, "holy_water:done"),
				new SetQuestAction(QUEST_SLOT, 3, "cure_myling:start")));
	}

	private void prepareCompleteStep() {
		final SpeakerNPC niall = npcs.get("Niall Breland");

		final ChatCondition canGetReward = new AndCondition(
			new QuestActiveCondition(QUEST_SLOT),
			new QuestInStateCondition(QUEST_SLOT, 3, "cure_myling:done"));

		// Niall has been healed
		elias.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			canGetReward,
			ConversationStates.ATTENDING,
			"Zwróciłeś mi mojego wnuka. Nie wiem jak teraz mógłbym ci dziękować."
				+ " Nie mam wiele do zaoferowania za twoją miłą obsługę, ale"
				+ " proszę, porozmawiaj z Niallem. Jest w piwnicy.",
				null);

		elias.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dziękuję za zwrócenie mi wnuka. Chwilę temu zszedł do piwnicy, jeśli chcesz z nim porozmawiać tam go znajdziesz.",
			null);

		niall.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			canGetReward,
			ConversationStates.ATTENDING,
			"Dziękuję Ci. Bez twojej pomocy nigdy bym nie wrócił do domu."
				+ " To jest mój plecak. Chcę, żebyś to miał. Dzięki niemu"
				+ " zmieścisz więcej rzeczy podczas podróży.",
			new MultipleActions(
				new SetQuestAction(QUEST_SLOT, 0, "done"),
				new IncreaseKarmaAction(500),
				new IncreaseXPAction(5000),
				new EnableFeatureAction("bag", "6 7")));

		niall.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Witaj ponownie. Szykuję się do kolejnej przygody z Marianne."
				+ " Ale nie martw się, będziemy trzymać się z dala od cmentarzy.",
			null);
	}

	private void prepareMylingSpawner() {
		final StendhalRPZone wellZone = SingletonRepository.getRPWorld().getZone("-1_myling_well");
		spawner = new MylingSpawner();
		spawner.setPosition(6, 5);
		wellZone.add(spawner);
		spawner.startSpawnTimer();
	}

	public static MylingSpawner getMylingSpawner() {
		return spawner;
	}
}