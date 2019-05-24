/* $Id$ */
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * QUEST: Ultimate Collector
 * <p>
 * PARTICIPANTS: <ul><li> Balduin  </ul>
 *
 * STEPS:
 * <ul><li> Balduin challenges you to be the ultimate weapons collector
 *     <li> Balduin asks you to complete each quest where you win a rare item
 *	   <li> Balduin asks you to bring him one extra rare item from a list
 *</ul>
 *
 * REWARD: <ul>
 * <li> You can sell black items to Balduin
 * <li> 100000 XP
 * <li> 90 karma
 * </ul>
 *
 * REPETITIONS: <ul><li> None. </ul>
 */
public class UltimateCollector extends AbstractQuest {

	/** Quest slot for this quest, the Ultimate Collector */
	private static final String QUEST_SLOT = "ultimate_collector";

	/** Club of Thorns in Kotoch: The Orc Saman is the NPC */
	private static final String CLUB_THORNS_QUEST_SLOT = "club_thorns"; // kotoch

	/** Vampire Sword quest: Hogart is the NPC */
	private static final String VAMPIRE_SWORD_QUEST_SLOT = "vs_quest"; // dwarf blacksmith

	/** Obsidian Knife quest: Alrak is the NPC */
	private static final String OBSIDIAN_KNIFE_QUEST_SLOT = "obsidian_knife"; // dwarf blacksmith

	/** Immortal Sword Quest in Kotoch: Vulcanus is the NPC */
	private static final String IMMORTAL_SWORD_QUEST_SLOT = "immortalsword_quest"; // kotoch

	/** Mithril Cloak quest: Ida is the NPC */
	private static final String MITHRIL_CLOAK_QUEST_SLOT = "mithril_cloak"; // mithril

	/** Mithril Shield quest: Baldemar is the NPC */
	private static final String MITHRIL_SHIELD_QUEST_SLOT = "mithrilshield_quest"; // mithril

	/** Cloak Collector 2nd quest: Josephine is the NPC (Completing 2nd requires 1st) */
	private static final String CLOAKSCOLLECTOR2_QUEST_SLOT = "cloaks_collector_2"; // cloaks

	/** Cloaks For Bario (Freezing Dwarf) quest: Bario is the NPC  */
	private static final String CLOAKS_FOR_BARIO_QUEST_SLOT = "cloaks_for_bario"; // cloaks

	// private static final String HELP_TOMI_QUEST_SLOT = "help_tomi"; don't require

	/** Elvish Armor quest: Lupos is the NPC */
	private static final String ELVISH_ARMOR_QUEST_SLOT = "elvish_armor"; // specific for this one

	/** Kanmararn Soldiers quest: Henry is the NPC  */
	private static final String KANMARARN_QUEST_SLOT = "soldier_henry"; // specific for this one

	/** Weapons Collector 2nd quest: Balduin is the NPC (Completing 2nd requires 1st) */
	private static final String WEAPONSCOLLECTOR2_QUEST_SLOT = "weapons_collector2";


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
		res.add("Balduin zaproponował mi zadanie na specjalną broń.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę przynosić mu więcej broni.");
			return res;
		}
		res.add("Zaakceptowałem jego ostatnie zadanie na broń i obiecałem przynieść mu specjalną i rzadką broń.");
		if (!isCompleted(player)) {
			res.add("Balduin poprosił mnie, aby mu przyniósł " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Super. Jestem teraz największym kolekcjonerem broni! Mogę Balduinowi sprzedawać czarne przedmioty.");
		}
		return res;
	}

	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Balduin");


		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj stary przyjacielu. Mam dla Ciebie kolejne #wyzwanie kolekcjonerskie.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "wyzwania"),
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(CLUB_THORNS_QUEST_SLOT),
							 new QuestNotCompletedCondition(IMMORTAL_SWORD_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Wciąż masz zadanie do wykonania w Kotoch. Szukaj dokładnie, a zostaniesz największym kolekcjonerem!",
			null);


		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "wyzwania"),
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
							 new QuestNotCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Brakuje Tobie specjalnego przedmiotu z mithrilu, który możesz wygrać jeżeli pomożesz odpowiedniej osobie. Nie możesz zostać największym kolekcjonerem bez tego.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "wyzwania"),
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(OBSIDIAN_KNIFE_QUEST_SLOT),
							 new QuestNotCompletedCondition(VAMPIRE_SWORD_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Głęboko pod ziemią żyje krasnal kowal, który wykuje specjalną broń dla Ciebie. Nie możesz zostać największym kolekcjonerem bez tego przedmiotu.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "wyzwania"),
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(CLOAKSCOLLECTOR2_QUEST_SLOT),
							 new QuestNotCompletedCondition(CLOAKS_FOR_BARIO_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Specjalny przedmiot będzie twój jeżeli uzbierasz sporo płaszczy, aby zaspokoić czyjąś próżność lub ochronić przed chłodem. To zadanie musisz wykonać.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie"),
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(ELVISH_ARMOR_QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Następny kolekcjoner przedmiotów wciąż potrzebuje twojej pomocy. Znajdziesz go w lesie Fado i dopóki nie wyświadczysz mu przysługi to nie będziesz mógł zostać największym kolekcjonerem.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "wyzwania"),
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(KANMARARN_QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Uzbierałeś sporo specjalnych przedmiotów, ale nigdy nie pomogłeś tym pod miastem Kanmararn. Powinieneś ukończyć tam zadania.",
			null);

	}

	private void requestItem() {

		final SpeakerNPC npc = npcs.get("Balduin");
		final Map<String,Integer> items = new HashMap<String, Integer>();

		// the numbers are based on depo's metric for rarity (bigger number = more rare) which may be out of date https://sourceforge.net/tracker/?func=detail&aid=2066597&group_id=1111&atid=973767
		// nothing rarer than a demon fire sword, and not included items which are quest rewards elsewhere
		items.put("nihonto",1); // 5169
		items.put("topór oburęczny magiczny",1); // 1010
		items.put("miecz cesarski",1); // 2393
		items.put("topór Durina",1); // 4331
		items.put("młot wulkanów",1); // 4474
		items.put("miecz xenocyjski",1); // 1347
		items.put("czarna kosa",1); // 3918 (pretty sure this is rarer now but a lot of old ones about to buy)
		items.put("sztylet chaosu",1); // 1691
		items.put("czarny miecz",1); // 6285

		// If all quests are completed, ask for an item
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("challenge", "wyzwanie", "wyzwania"),
				new AndCondition(
						new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(KANMARARN_QUEST_SLOT),
						new QuestCompletedCondition(ELVISH_ARMOR_QUEST_SLOT),
						new QuestCompletedCondition(CLOAKSCOLLECTOR2_QUEST_SLOT),
						new QuestCompletedCondition(CLOAKS_FOR_BARIO_QUEST_SLOT),
						new QuestCompletedCondition(OBSIDIAN_KNIFE_QUEST_SLOT),
						new QuestCompletedCondition(VAMPIRE_SWORD_QUEST_SLOT),
						new QuestCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
						new QuestCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT),
						new QuestCompletedCondition(CLUB_THORNS_QUEST_SLOT),
						new QuestCompletedCondition(IMMORTAL_SWORD_QUEST_SLOT)),
				ConversationStates.ATTENDING,
				null,
				new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Właśnie udowodniłeś mieszkańcom Faiumoni, że możesz być największym kolekcjonerem,"
					+ " ale mam dla Ciebie ostatnie wyzwanie. Proszę przynieś mi [item]."));
	}

	private void collectItem() {

		final SpeakerNPC npc = npcs.get("Balduin");

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Przyniosłeś mi ten rzadki przedmiot, o który cię prosiłem?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Hm, nie masz [item], nie próbuj mnie oszukać!"));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Wow, To niewiarygodne, mogę zobaczyć to z bliska! Wielkie dzięki. Czas, być może złożyć #ofertę.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT),
									new SetQuestAction(QUEST_SLOT, "done"),
									new IncreaseXPAction(100000),
									new IncreaseKarmaAction(90)));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Bardzo dobrze, wróć kiedy będziesz mieć [the item] z sobą."));
	}

	private void offerSteps() {
  		final SpeakerNPC npc = npcs.get("Balduin");

		// player returns after finishing the quest and says offer
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Kupię czarne przedmioty, ale mogę sobie pozwolić tylko zapłacić skromną cenę.",
				null);


		// player returns when the quest is in progress and says offer
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Kupię czarne przedmioty po ukończeniu każdego #wyzwania , które postawie Ci.", null);
	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Największy Kolekcjoner Broni",
				"Balduin, pustelnik, który żyje w górach Ados, ma ostateczne wyzwanie dla Mnie.",
				true);

		checkCollectingQuests();
		requestItem();
		collectItem();
		offerSteps();

	}

	@Override
	public String getName() {
		return "UltimateCollector";
	}

	// This is the max level of the min levels for the other quests
	@Override
	public int getMinLevel() {
		return 100;
	}

	@Override
	public String getNPCName() {
		return "Balduin";
	}

	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}
}
