package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;

public class Gornictwo extends AbstractQuest {
	private static final String QUEST_SLOT = "gornictwo";
	private static final String MINES_EXAM = "cech_gornika";

	private static final int REQUIRED_MINUTES = 10;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Górnictwo";
	}

	@Override
	public String getNPCName() {
		return "Górnik";
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Górnictwo",
			"Górnik odnaleziony w kopalni pod Zakopcem potrzebuje pomocy w uzupełnieniu informacji o minerałach.",
			false);
		step1();
		stepDigSulfur();
		stepPassedExam();
		stepDigIron();
		stepDigCopper();
		stepPickForging();
		stepDigGold();
		stepDigJewelry();
		stepDigShadow();
		stepDigSilver();
		stepPickGoldForging();
		stepDigAmetyst();
		stepDigObsidian();
		stepDigPlatinum();
		stepDigMithril();
		stepDigLast();
	}

	private void step1() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		// Checks if the player completed quest
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Dzięki Tobie mam już wszystkie opisane minerały w swojej książce!", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Zechcesz pomóc mi zebrać wszystkie informacje na temat kamieni?", null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"W takim razie podnieś mój stary już kilof, może na niewiele się nada, lecz jeszcze Tobie posłuży oraz postaraj się wydobyć dla mnie 2 rudy siarki.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Trudno, samemu zdobędę potrzebne mi informacje.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void stepDigSulfur() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isEquipped("siarka", 2)) {
						if (player.isQuestCompleted(MINES_EXAM)) {
							raiser.say("Super! Dziękuję za rudy siarki! Teraz poprosiłbym abyś " + Grammar.genderVerb(player.getGender(), "wykopał") + " dla mnie 5 rud żelaza.");
							player.drop("siarka", 2);
							player.incMiningXP(1000);
							player.addXP(500);
							player.addKarma(5);
							player.setQuest(QUEST_SLOT, "dig_iron");
						} else {
							raiser.say("Super! Dziękuję za rudy siarki. Do dalszej eksploracji potrzebujesz nieco większej wiedzy! Odszukaj #Bercika i postaraj się zdać egzamin na górnika. Jak już zdasz, wróć do mnie!");
							player.drop("siarka", 2);
							player.addXP(500);
							player.addKarma(5);
							player.setQuest(QUEST_SLOT, "exam");
						}
					} else {
						raiser.say("Nie próbuj mnie oszukiwać... Wiem, że nie " + Grammar.genderVerb(player.getGender(), "wykopałeś") + " jeszcze tego surowca.");
					}
				}
			});

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("Bercikiem", "Bercik"), null,
			ConversationStates.ATTENDING,
			"Odnajdziesz go na południowym kościelisku, zamiekszuje w pewnym domku obok mostka.", null);
	}

	private void stepPassedExam() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "exam")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isQuestCompleted(MINES_EXAM)) {
						raiser.say("Moje gratulacje zdania egzaminu! Teraz poprosiłbym abyś " + Grammar.genderVerb(player.getGender(), "wykopał") + " dla mnie 5 rud żelaza.");
						player.incMiningXP(1000);
						player.setQuest(QUEST_SLOT, "dig_iron");
					} else {
						raiser.say("Nadal wyczum iż brak ci wiedzy na temat różnych kamieni...");
					}
				}
			});
	}

	private void stepDigIron() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_iron")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("ruda żelaza", 5)) {
						raiser.say("No proszę, dziękuję za pomoc w wydobyciu kolejnego surowca! Wydobądź teraz dla mnie 3 rudy miedzi oraz przynieś 5 polan, a wykonam dla ciebie wytrzymalszy kilof!");
						player.drop("ruda żelaza", 5);
						player.incMiningXP(2000);
						player.addXP(1000);
						player.setQuest(QUEST_SLOT, "dig_copper");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie rud żelaza...");
					}
				}
			});
	}

	private void stepDigCopper() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_copper")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("ruda miedzi", 3) && player.isEquipped("polano", 5)) {
						raiser.say("Świetnie, wróć do mnie za 10 minut, aby odebrać swój nowy kilof!");
						player.drop("ruda miedzi", 3);
						player.drop("polano", 5);
						player.incMiningXP(2500);
						player.addXP(1250);
						player.setQuest(QUEST_SLOT, "pick_forging;" + System.currentTimeMillis());
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie rudy miedzi oraz polan...");
					}
				}
			});
	}

	private void stepPickForging() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "pick_forging;")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
					final long timeRemaining = Long.parseLong(tokens[1]) + delay
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Wciąż odlewam dla ciebie nowy kilof. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ", aby go odebrać.");
						return;
					}

					raiser.say("A oto twój nowy kilof! Teraz będziesz " + Grammar.genderVerb(player.getGender(), "mógł") + " wykopać dla mnie 7 bryłek złota. Złoże złota znajdziejsz niedaleko przejścia na poziom wyżej kopalni.");
					player.addXP(500);
					player.addKarma(10);
					final Item item = SingletonRepository.getEntityManager().getItem("kilof stalowy");
					item.setBoundTo(player.getName());
					player.equipOrPutOnGround(item);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "dig_gold");
				}
			});
	}

	private void stepDigGold() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_gold")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("bryłka złota", 7)) {
						raiser.say("Widzę, że już masz bryłki złota, następnie proszę cię o wydobycie po 7 kryształów szmaragdu, szafiru oraz rubinu.");
						player.drop("bryłka złota", 7);
						player.incMiningXP(3000);
						player.addXP(1550);
						player.setQuest(QUEST_SLOT, "dig_jewelry");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie bryłek złota...");
					}
				}
			});
	}

	private void stepDigJewelry() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_jewelry")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("kryształ szmaragdu", 7) && player.isEquipped("kryształ szafiru", 7) && player.isEquipped("kryształ rubinu", 7)) {
						raiser.say("Ooo... super! Teraz proszę cię o wydobycia zupełnie nowego surowca! Są pogłoski iż gdzieś w tej kopalni znajduje się ruda cieni! Chcę, abyś " + Grammar.genderVerb(player.getGender(), "przyniósł") + " mi 12 rud tego surowca.");
						player.drop("kryształ szmaragdu", 7);
						player.drop("kryształ szafiru", 7);
						player.drop("kryształ rubinu", 7);
						player.incMiningXP(7000);
						player.addXP(2000);
						player.setQuest(QUEST_SLOT, "dig_shadow");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie kryształów...");
					}
				}
			});
	}

	private void stepDigShadow() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_shadow")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("ruda cieni", 12)) {
						raiser.say("Ależ ten surowiec ma dziwne właściwości! Dziękuję, nie sądziłem, że to była prawda! Następnie chciałbym się prosić o wykopanie 10 rud srebra. Przynieś jeszcze 7 polan oraz kilof stalowy, a wtedy wykonam dla ciebie jeszcze mocniejszy kilof, dzięki niemu będziesz jeszcze szybciej " + Grammar.genderVerb(player.getGender(), "kopał") + ".");
						player.drop("ruda cieni", 12);
						player.incMiningXP(5000);
						player.addXP(2200);
						player.setQuest(QUEST_SLOT, "dig_silver");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie rudy cieni...");
					}
				}
			});
	}

	private void stepDigSilver() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_silver")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("ruda srebra", 10) && player.isEquipped("polano", 7) && player.isEquipped("kilof stalowy")) {
						raiser.say("Już jesteśmy blisko ukończenia mojej książki górniczej! Zacznę odlewać nowy kilof dla ciebie, odczekaj 10 minut i wróć do mnie.");
						player.drop("ruda srebra", 10);
						player.drop("polano", 7);
						player.drop("kilof stalowy", 1);
						player.incMiningXP(5500);
						player.addXP(2000);
						player.setQuest(QUEST_SLOT, "pickgold_forging;" + System.currentTimeMillis());
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie rudy srebra, polana oraz kilofa stalowego...");
					}
				}
			});
	}

	private void stepPickGoldForging() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "pickgold_forging;")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
					final long timeRemaining = Long.parseLong(tokens[1]) + delay
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Wciąż odlewam dla ciebie nowy kilof. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ", aby go odebrać.");
						return;
					}

					raiser.say("A oto twój nowy kilof! Teraz spróbuj odnaleźć rudę ametystu i przynieś mi 7 kryształów.");
					player.addXP(500);
					player.addKarma(10);
					final Item item = SingletonRepository.getEntityManager().getItem("kilof złoty");
					item.setBoundTo(player.getName());
					player.equipOrPutOnGround(item);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "dig_ametyst");
				}
			});
	}

	private void stepDigAmetyst() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_ametyst")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("kryształ ametystu", 7)) {
						raiser.say("Ależ piękny surowiec, prawda? Kolejny surowiec, o który cię poproszę to będzie 14 kryształów obsydianu, myślę, że nie sprawi ci to kłopotów.");
						player.drop("kryształ ametystu", 7);
						player.incMiningXP(7000);
						player.addXP(2300);
						player.setQuest(QUEST_SLOT, "dig_obsidian");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie kryształów ametystu...");
					}
				}
			});
	}

	private void stepDigObsidian() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_obsidian")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("kryształ obsydianu", 14)) {
						raiser.say("Śliczny kryształ! Wydobądź teraz 10 rud platyny.");
						player.drop("kryształ obsydianu", 14);
						player.incMiningXP(12000);
						player.addXP(3000);
						player.setQuest(QUEST_SLOT, "dig_platinum");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie kryształów obsydianu...");
					}
				}
			});
	}

	private void stepDigPlatinum() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_platinum")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("ruda platyny", 10)) {
						raiser.say("Oh... tego surowca jeszcze nie miałem w swoim spisie, za to ci bardzo dziękuję! Następnie wykop dla mnie 20 bryłek mithrilu oraz 10 kryształów diamentu.");
						player.drop("ruda platyny", 10);
						player.incMiningXP(8000);
						player.addXP(2500);
						player.setQuest(QUEST_SLOT, "dig_mithril");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie bryłek mithrilu oraz kryształów diamentu...");
					}
				}
			});
	}

	private void stepDigMithril() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_mithril")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("bryłka mithrilu", 20) && player.isEquipped("kryształ diamentu", 10) && player.isEquipped("kilof złoty")) {
						raiser.say("Prześliczne surowce! W nagrodę otrzymasz mój własny kilof wykonany z obsydianu przez co pozwolę sobie zabrać twój kilof złoty w ramach nowego. Cudownie się nim wykopuje surowce! Będziesz " + Grammar.genderVerb(player.getGender(), "chciał") + " go przetestować, prawda? Wykop w takim razie dla mnie po 30 bryłek złota, mithrilu, kryształów obsydianu, ametystu oraz diamentu nowym kilofem!");
						player.drop("bryłka mithrilu", 20);
						player.drop("kryształ diamentu", 10);
						player.drop("kilof złoty", 1);
						player.incMiningXP(50000);
						player.addXP(10000);
						player.addKarma(10);
						final Item item = SingletonRepository.getEntityManager().getItem("kilof obsydianowy");
						item.setBoundTo(player.getName());
						player.equipOrPutOnGround(item);
						player.notifyWorldAboutChanges();
						player.setQuest(QUEST_SLOT, "dig_last");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie bryłek mithrilu, kryształów diamentu oraz kilofa złotego...");
					}
				}
			});
	}

	private void stepDigLast() {
		final SpeakerNPC npc = npcs.get(getNPCName());

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "dig_last")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser raiser) {
					if (player.isEquipped("bryłka złota", 30) && player.isEquipped("bryłka mithrilu", 30) && player.isEquipped("kryształ obsydianu", 30) && player.isEquipped("kryształ ametystu", 30) && player.isEquipped("kryształ diamentu", 30)) {
						raiser.say("Niesamowite narzędzie, prawda? Moja książka o górnictwie w końcu zostanie ukończona! Pięknie ci dziękuję za pomoc.");
						player.drop("bryłka złota", 30);
						player.drop("bryłka mithrilu", 30);
						player.drop("kryształ obsydianu", 30);
						player.drop("kryształ ametystu", 30);
						player.drop("kryształ diamentu", 30);
						player.incMiningXP(100000);
						player.addXP(25000);
						player.addKarma(20);
						player.setQuest(QUEST_SLOT, "done");
					} else {
						raiser.say("Nie mam pojęcie czy ludzie myślą iż ja nie widzę tego, ale jednak... Widzę, że nie masz przy sobie bryłek oraz kryształów...");
					}
				}
			});
	}

	@Override
	public List<String> getHistory(Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Rozmawiałem z Górnikiem w kopalni pod Zakopcem.");

		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę brudzić swych rąk w kopalniach.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Postanowiłem pomóc Górnikowi uzupełnić informacje w swojej książce o minerałach.");
		}

		if (player.isQuestInState(QUEST_SLOT, 0, "start")) {
			res.add("Jako pierwszy minerał poprosił mnie o wydobycie 2 rudy siarki.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "exam")) {
			res.add("Muszę zdać egzamin na górnika, aby dalej pomagać w zdobywaniu informacji o surowcach.");
		}

		if (questState.startsWith("pick_forging") || questState.startsWith("pickgold_forging")) {
			if (new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES).fire(player, null, null)) {
				res.add("Prawdopodobnie Górnik ukończył odlewanie nowego kilofa dla mnie.");
			} else {
				res.add("Górnik kazał poczekać 10 minut na nowy kilof.");
			}
		}

		if (player.isQuestInState(QUEST_SLOT, 0, "dig_iron")) {
			res.add("Muszę wykopać 5 rud żelaza.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_copper")) {
			res.add("Muszę wykopać 3 rud miedzi oraz przynieść 5 sztuk drewna.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_gold")) {
			res.add("Muszę wykopać 7 bryłek złota.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_jewelry")) {
			res.add("Muszę wykopać po 7 kryształów szmaragdu, szafiru oraz rubinu.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_shadow")) {
			res.add("Muszę wykopać 12 rud cieni.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_silver")) {
			res.add("Muszę wykopać 10 rud srebra oraz przynieść 7 sztuk drewna.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_ametyst")) {
			res.add("Muszę wykopać 7 kryształów ametystu.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_obsidian")) {
			res.add("Muszę wykopać 14 kryształów obsydianu.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_platinum")) {
			res.add("Muszę wykopać 10 rud platyny.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_mithril")) {
			res.add("Muszę wykopać 20 bryłek mithrilu oraz 10 kryształów diamentu.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "dig_last")) {
			res.add("Muszę wykopać po 30 bryłek złota, mithrilu, kryształów obsydianu, ametystu oraz diamentu.");
		}

		if (player.isQuestInState(QUEST_SLOT, 0, "done")) {
			res.add("Wykopałem oraz przyniosłem wszystko o co poprosił mnie Górnik. W nagrodę otrzymałem kilof obsydianowy.");
		}

		return res;
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
