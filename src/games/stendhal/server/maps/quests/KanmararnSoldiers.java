/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerOwnsItemIncludingBankCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.game.RPObject;
import marauroa.common.game.SlotIsFullException;

/**
 * QUEST:
 * <p>
 * Soldiers in Kanmararn.
 *
 * NOTE:
 * <p>
 * It also starts a quest that needs NPC McPegleg that is created. It doesn't
 * harm if that script is missing, just that the IOU cannot be delivered and
 * hence the player can't get cash
 *
 * PARTICIPANTS:
 * <li> Henry
 * <li> Sergeant James
 * <li> corpse of Tom
 * <li> corpse of Charles
 * <li> corpse of Peter
 *
 * STEPS:
 * <li> optional: speak to Sergeant James to get the task to find the map
 * <li> talk to Henry to get the task to find some proof that the other 3
 * soldiers are dead.
 * <li> collect the item in each of the corpses of the three other soldiers
 * <li> bring them back to Henry to get the map - bring the map to Sergeant
 * James
 *
 * REWARD:
 * <p>
 * from Henry:
 * <li> you can keep the IOU paper (for quest MCPeglegIOU)
 * <li> 2,500 XP
 * <li> some karma (15)
 * <p>
 * from Sergeant James
 * <li> mainio boots
 * <li> some karma (15)
 *
 * REPETITIONS:
 * <li> None.
 *
 * @see McPeglegIOU
 */
public class KanmararnSoldiers extends AbstractQuest {

	private static final Logger logger = Logger.getLogger(KanmararnSoldiers.class);

	private static final String QUEST_SLOT = "soldier_henry";

	/**
	 * The maximum time (in seconds) until plundered corpses will be filled
	 * again, so that other players can do the quest as well.
	 */
	private static final int CORPSE_REFILL_SECONDS = 60;

	/* Soldier names used in quest */
	private static final String SLD_HENRY = "Henry";
	private static final String SLD_CHARLES = "Charles";
	private static final String SLD_TOM = "Tom";
	private static final String SLD_PETER = "Peter";
	private static final String SRG_JAMES = "Sergeant James";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	/**
	 * A CorpseRefiller checks, in regular intervals, if the given corpse.
	 *
	 * @author daniel
	 *
	 */
	static class CorpseRefiller implements TurnListener {
		private final Corpse corpse;

		private final String itemName;

		private final String description;

		public CorpseRefiller(final Corpse corpse, final String itemName, final String description) {
			this.corpse = corpse;
			this.itemName = itemName;
			this.description = description;
		}

		public void start() {
			SingletonRepository.getTurnNotifier().notifyInTurns(1, this);
		}

		private boolean equalsExpectedItem(final Item item) {
			if (!item.getName().equals(itemName)) {
				return false;
			}

			if (!item.getDescription().equals(description)) {
				return false;
			}

			return corpse.getName().equals(item.getInfoString());
		}

		@Override
		public void onTurnReached(final int currentTurn) {
			boolean isStillFilled = false;
			// Check if the item is still in the corpse. Note that somebody
			// might have put other stuff into the corpse.
			for (final RPObject object : corpse.getSlot("content")) {
				if (object instanceof Item) {
					final Item item = (Item) object;
					if (equalsExpectedItem(item)) {
						isStillFilled = true;
					}
				}
			}
			try {
				if (!isStillFilled) {
					// recreate the item and fill the corpse
					final Item item = SingletonRepository.getEntityManager().getItem(
							itemName);
					item.setInfoString(corpse.getName());
					item.setDescription(description);
					corpse.add(item);
					corpse.notifyWorldAboutChanges();
				}
			} catch (final SlotIsFullException e) {
				// ignore, just don't refill the corpse until someone removes
				// the other items from the corpse
				logger.warn("Quest corpse is full: " + corpse.getName());
			}
			// continue the checking cycle
			SingletonRepository.getTurnNotifier().notifyInSeconds(CORPSE_REFILL_SECONDS, this);
		}
	}



	static class HenryQuestNotCompletedCondition implements ChatCondition {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			return !player.hasQuest(QUEST_SLOT) || player.getQuest(QUEST_SLOT).equals("start");
		}
	}

	static class HenryQuestCompletedCondition implements ChatCondition {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			return player.hasQuest(QUEST_SLOT) && !player.getQuest(QUEST_SLOT).equals("start");
		}
	}

	static class GiveMapAction implements ChatAction {
		private boolean bind = false;

		public GiveMapAction(boolean bind) {
			this.bind = bind;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final Item map = SingletonRepository.getEntityManager().getItem("mapa");
			map.setInfoString(npc.getName());
			map.setDescription("Oto ręcznie narysowana mapa. Bez względu jak na nią patrzysz to nie widzisz niczego znajomego.");
			if (bind) {
				map.setBoundTo(player.getName());
			}
			player.equipOrPutOnGround(map);
			player.setQuest(QUEST_SLOT, "map");
		}
	}


	/**
	 * We add text for NPC Henry who will get us on the quest.
	 */
	private void prepareCowardSoldier() {
		final SpeakerNPC henry = npcs.get(SLD_HENRY);

		henry.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestNotStartedCondition(QUEST_SLOT),
							 new QuestNotInStateCondition(QUEST_SLOT,"map")),
			ConversationStates.QUEST_OFFERED,
			"Znajdź moją #drużynę Peter, Tom i Charles. Udowodnij, że ich znalazłeś a ja Cię wynagrodzę. Zrobisz to?",
			null);

		henry.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new QuestInStateCondition(QUEST_SLOT,"map")),
				ConversationStates.ATTENDING,
				"Jestem smutny, że większość moich przyjaciół nie żyje.",
				null);

		henry.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dziękuje! Bedę czekał na twój powrót.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));
		
		// player tries to ask for quest again after starting
		henry.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestActiveCondition(QUEST_SLOT),
				new QuestNotInStateCondition(QUEST_SLOT, "map")),
			ConversationStates.ATTENDING,
			"Już Cię poprosiłem, abyś odnalazł moich przyjaciół Peter, Tom i Charles.",
			null);

		henry.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("group", "drużyna", "drużyny"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Generał wysłał naszą piątkę do rozpoznania terenu i znalezienia #skarbu. Pomożesz mi ich znaleźć?",
			null);

        henry.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("treasure", "skarb"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Duży skarb jest #gdzieś w tych podziemiach. Pomożesz mi znaleźć moją drużynę?",
				null);

		henry.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Dobrze. Rozumiem. Też się boje #krasnali.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(2500));
		actions.add(new DropInfostringItemAction("skórzane spodnie", SLD_TOM));
		actions.add(new DropInfostringItemAction("zbroja łuskowa", SLD_PETER));
		actions.add(new IncreaseKarmaAction(15.0));
		actions.add(new GiveMapAction(false));

		henry.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(henry.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasInfostringItemWithHimCondition("skórzane spodnie", SLD_TOM),
						new PlayerHasInfostringItemWithHimCondition("karteczka", SLD_CHARLES),
						new PlayerHasInfostringItemWithHimCondition("zbroja łuskowa", SLD_PETER)),
				ConversationStates.ATTENDING,
				"Och nie! Peter, Tom i Charles nie żyją? *płacz*. W każdym razie oto twoja nagroda i zatrzymaj IOU.",
				new MultipleActions(actions));

		henry.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(henry.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(
								new AndCondition(
										new PlayerHasInfostringItemWithHimCondition("skórzane spodnie", SLD_TOM),
										new PlayerHasInfostringItemWithHimCondition("karteczka", SLD_CHARLES),
										new PlayerHasInfostringItemWithHimCondition("zbroja łuskowa", SLD_PETER)))),
				ConversationStates.ATTENDING,
				"Nie udowodniłeś, że znalazłeś wszystkich!",
				null);

		henry.add(ConversationStates.ATTENDING, Arrays.asList("map", "group", "help", "mapa", "pomoc"),
				new OrCondition(
					new	QuestCompletedCondition(QUEST_SLOT),
					new AndCondition(new HenryQuestCompletedCondition(),
					new PlayerOwnsItemIncludingBankCondition("mapa"))),
				ConversationStates.ATTENDING,
				"Jestem smutny, bo większość moich przyjaciół nie żyje.", null);

		henry.add(ConversationStates.ATTENDING, Arrays.asList("map", "mapa"),
				new AndCondition(
					new	QuestNotCompletedCondition(QUEST_SLOT),
					new HenryQuestCompletedCondition(),
					new NotCondition(new PlayerOwnsItemIncludingBankCondition("mapa"))),
				ConversationStates.ATTENDING,
				"Na szczęście narysowałem kopię mapy, ale proszę nie zgub jej.",
				new GiveMapAction(true));

		henry.add(ConversationStates.ATTENDING, Arrays.asList("map", "mapa"),
				new HenryQuestNotCompletedCondition(),
				ConversationStates.ATTENDING,
				"Jeżeli znajdziesz moich przyjaciół to dam Ci mapę", null);
	}

	/**
	 * add corpses of ex-NPCs.
	 */
	private void prepareCorpses() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("-6_kanmararn_city");

		// Now we create the corpse of the second NPC
		final Corpse tom = new Corpse("youngsoldiernpc", 5, 47);
		// he died first
		tom.setStage(4);
		tom.setName(SLD_TOM);
		tom.setKiller("patrol krasnali");
		// Add our new Ex-NPC to the game world
		zone.add(tom);

		// Add a refiller to automatically fill the corpse of unlucky Tom
		final CorpseRefiller tomRefiller = new CorpseRefiller(tom, "skórzane spodnie",
				"Oto podarte skórzane spodnie zalane krwią");
		tomRefiller.start();

		// Now we create the corpse of the third NPC
		final Corpse charles = new Corpse("youngsoldiernpc", 94, 5);
		// he died second
		charles.setStage(3);
		charles.setName(SLD_CHARLES);
		charles.setKiller("patrol krasnali");
		// Add our new Ex-NPC to the game world
		zone.add(charles);
		// Add a refiller to automatically fill the corpse of unlucky Charles
		final CorpseRefiller charlesRefiller = new CorpseRefiller(charles, "karteczka",
				"Czytasz: \"Czek na  250 money. (podpisano) McPegleg\"");
		charlesRefiller.start();

		// Now we create the corpse of the fourth NPC
		final Corpse peter = new Corpse("youngsoldiernpc", 11, 63);
		// he died recently
		peter.setStage(2);
		peter.setName(SLD_PETER);
		peter.setKiller("patrol krasnali");
		// Add our new Ex-NPC to the game world
		zone.add(peter);
		// Add a refiller to automatically fill the corpse of unlucky Peter
		final CorpseRefiller peterRefiller = new CorpseRefiller(
				peter,
				"zbroja łuskowa",
				"Oto zbroja łuskowa mocno zdeformowana przez kilka mocnych uderzeń młotem.");
		peterRefiller.start();
	}

	/**
	 * add James.
	 */
	private void prepareSergeant() {
		final SpeakerNPC james = npcs.get(SRG_JAMES);

		// quest related stuff
		james.addHelp("Pomyśl potrzebuję małej pomocy. Moja #drużyna została wybita, a #jeden uciekł. Nie dobrze, bo on miał #mapę.");
		james.addQuest("Znajdź mojego zaginionego żołnierza i przyprowadź go do mnie ... lub przynieś mi #mapę, która ma przy sobie.");
		james.addReply(Arrays.asList("group", "drużna"),
			"Było nas pięciu, trzech z nas nie żyje. Pewnie widziałeś ich zwłoki.");
		james.addReply(Arrays.asList("one", "henry", "jeden"),
			"Tak mój najmłodszy żołnierz. On uciekł.");
		james.addReply(Arrays.asList("mapa", "mapę"),
			"Mapa #skarbów, która wiedzie do serca #królestwa #krasnali.");
		james.addReply(Arrays.asList("treasure", "skarbów"),
			"Wielki skarb jest gdzieś ukryty w tych podziemiach.");
		james.addReply(Arrays.asList("dwarf", "dwarves", "dwarven", "krasnali"),
			"Są silnymi przeciwnikami! Jesteśmy w ich #królestwie.");
		james.addReply(Arrays.asList("peter", "tom", "charles"),
			"Był dobrym żołnierzem i walczył dzielnie.");
		james.addReply(Arrays.asList("kingdom", "kanmararn", "królestwa", "królestwie"),
			"Kanmararn jest legendarnym królestwem #krasnali.");
		james.addReply("dreamscape",
			"Jest pewien mężczyzna na wschód od miasta. On zna drogę.");

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(5000));
		actions.add(new DropInfostringItemAction("mapa", SLD_HENRY));
		actions.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "done", 15.0));
		actions.add(new EquipItemAction("buty mainiocyjskie", 1, true));

		james.add(ConversationStates.ATTENDING,
				Arrays.asList("map", "henry", "mapa"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "map"),
								new PlayerHasInfostringItemWithHimCondition("mapa", SLD_HENRY)),
				ConversationStates.ATTENDING,
				"Mapa! Cudownie! Dziękuję. Oto twoja nagroda. Zdobyłem te buty, gdy byłem w #dreamscape.",
				new MultipleActions(actions));

		james.add(ConversationStates.ATTENDING,
				Arrays.asList("map", "henry", "mapa"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "map"),
								new NotCondition(new PlayerHasInfostringItemWithHimCondition("mapa", SLD_HENRY))),
				ConversationStates.ATTENDING,
				"Cóż, gdzie jest mapa?",
				null);

		james.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za przyniesienie mapy!", null);

		james.add(ConversationStates.ATTENDING, ConversationPhrases.HELP_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za przyniesienie mapy!", null);

		james.add(ConversationStates.ATTENDING, Arrays.asList("map", "henry",
			 "group", "one", "mapa", "drużyna"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dziękuję za przyniesienie mapy!", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Żołnierz Kanmararn",
				"Jakiś czas temu Sergeant James wraz ze swoimi najdzielniejszymi żołnierzami rozpoczął poszukiwanie skarbu w Kanmararn mieście krasnali. Jeszcze nie wrócili. Pójdziesz ich poszukać?.",
				true);
		prepareCowardSoldier();
		prepareCorpses();
		prepareSergeant();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem przerażonego żołnierza w Kanmararn City. Zapytał mnie, czy odnazał bym jego przyjaciół: Petera, Charlesa, i Toma.");
			if ("rejected".equals(questState)) {
				res.add("Nie pomogę dla Henry.");
				return res;
			}
			if ("start".equals(questState)) {
				return res;
			}
			res.add("Niestety znalazłem tylko ich zwłoki Petera, Charlesa, i Toma. Henry był przerażony. Za fatygę dał mi mapę i jakąś karteczkę. Nie mam pojęcia po co mi to.");
			if ("map".equals(questState)) {
				return res;
			}
			res.add("Poznałem sierżanta Jamesa  i dałem mu mapę. On dał mi w zamian solidne buty mainiocyjskie.");
			if (isCompleted(player)) {
				return res;
			}
			// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
			final List<String> debug = new ArrayList<String>();
			debug.add("Stan zadania to: " + questState);
			logger.error("History doesn't have a matching quest state for " + questState);
			return debug;
	}

	@Override
	public String getName() {
		return "KanmararnSoldiers";
	}

	@Override
	public int getMinLevel() {
		return 40;
	}

	@Override
	public String getNPCName() {
		return SLD_HENRY;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_DUNGEONS;
	}
}
