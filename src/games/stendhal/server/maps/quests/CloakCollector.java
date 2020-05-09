/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.common.ItemTools;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Cloak Collector
 * <p>
 * PARTICIPANTS: - Josephine, a young woman who live in Ados/Fado
 * <p>
 * STEPS:
 * <ul>
 * <li> Josephine asks you to bring her a cloak in every colour available on
 * the mainland
 * <li> You bring cloaks to Josephine
 * <li> Repeat until Josephine
 * received all cloaks. (Of course you can bring several cloaks at the same
 * time.)
 * <li> Josephine gives you a reward
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> black cloak </li>
 * <li> 250,000 XP </li>
 * <li> 50 karma (+5 for accepting, -5 for rejecting) </li>
 * </ul>
 * <p>
 * REPETITIONS: - None.
 */
public class CloakCollector extends AbstractQuest implements BringListOfItemsQuest {

	private static final List<String> NEEDED_CLOAKS = Arrays.asList("peleryna",
			"peleryna elficka", "płaszcz krasnoludzki", "lazurowy płaszcz elficki", "płaszcz kamienny",
			"szmaragdowy płaszcz smoczy", "kościany płaszcz smoczy", "płaszcz licha",
			"płaszcz wampirzy", "lazurowy płaszcz smoczy");

	private static final String QUEST_SLOT = "cloaks_collector";

	private BringListOfItemsQuestLogic bringItems;

	@Override
	public List<String> getHistory(final Player player) {
		return bringItems.getHistory(player);
	}

	private void setupAbstractQuest() {
		final BringListOfItemsQuest concreteQuest = this;
		bringItems = new BringListOfItemsQuestLogic(concreteQuest);
		bringItems.addToWorld();
	}

	@Override
	public void addToWorld() {
		step_1();
		setupAbstractQuest();
		fillQuestInfo(
				"Płaszcze dla Kolekcjonera",
				"Josephine szuka płaszczy w wielu kolorach.",
				false);
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Josephine");

		// player asks about an individual cloak before accepting the quest
		for(final String itemName : NEEDED_CLOAKS) {
			npc.add(ConversationStates.QUEST_OFFERED, itemName, null,
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("Nie znam " + obj.getOriginal() + ". Możesz podać nazwę innego płaszcza?");
						} else {
							final Item item = SingletonRepository.getEntityManager().getItem(itemName);
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("Nie widziałeś jeszcze żadnego? Cóż to jest ");

							if (item == null) {
								stringBuilder.append(itemName);
							} else {
								stringBuilder.append(ItemTools.itemNameToDisplayName(item.getItemSubclass()));
							}

							stringBuilder.append(". znajdziesz to?");
							raiser.say(stringBuilder.toString());
						}
					}

					@Override
					public String toString() {
						return "describe item";
					}
			});
		}
	}

	@Override
	public List<String> getAdditionalTriggerPhraseForQuest() {
		return Arrays.asList("clothes", "ubrania");
	}

	@Override
	public SpeakerNPC getNPC() {
		return npcs.get("Josephine");
	}

	@Override
	public List<String> getNeededItems() {
		return NEEDED_CLOAKS;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("cloaks", "płaszcze");
	}

	@Override
	public double getKarmaDiffForQuestResponse() {
		return 5.0;
	}

	@Override
	public boolean shouldWelcomeAfterQuestIsCompleted() {
		return false;
	}

	@Override
	public String welcomeBeforeStartingQuest() {
		return "Cześć przystojniaku! Widzę Ciebie jak moją piękną sukienkę. Kocham #ubrania...";
	}

	@Override
	public String welcomeDuringActiveQuest() {
		return "Cześć! Przyniosłeś ze sobą jakieś #płaszcze?";
	}

	@Override
	public String welcomeAfterQuestIsCompleted() {
		return "Witaj znowu, kochany. Płaszcze wciąż wygladają wspaniale. Dziękuję!";
	}

	@Override
	public String respondToQuest() {
		return "Teraz mam obsesję na punkcie #płaszczy! Są kolorowe, a jakie wszystkie piękne!";
	}

	@Override
	public String respondToQuestAcception() {
		// player.addKarma(5.0);
		return "Wspaniale! Jestem taka podekscytowana!";
	}

	@Override
	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Witaj znowu, kochany. Płaszcze wciąż wyglądają wspaniale. Dziękuję!";
	}

	@Override
	public String respondToQuestRefusal() {
		// player.addKarma(-5.0);
		return "Och... nie jesteś zbyt miły. Do widzenia.";
	}

	@Override
	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Cudownie! Jakie #płaszcze przyniosłeś mi?";
	}

	@Override
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Chcę " + Grammar.quantityplnoun(missingItems.size(), "płaszczy", "płaszcz")
				+ ". To jest " + Grammar.enumerateCollection(missingItems)
				+ ". Dasz radę przynieść mi jakiś?";
	}

	@Override
	public String askForMissingItems(final List<String> missingItems) {
		return "Chcę " + Grammar.quantityplnoun(missingItems.size(), "płaszczy", "płaszcz")
				+ ". To jest " + Grammar.enumerateCollection(missingItems)
				+ ". Przyniosłeś jakiś?";
	}

	@Override
	public String respondToItemBrought() {
		return "Łał, dziękuję! Co jeszcze mi przyniosłeś?";
	}

	@Override
	public String respondToLastItemBrought() {
		return "Och, wszystkie wyglądają tak pięknie, dziękuję. Proszę weź ten czarny płaszcz w zamian, nie lubię tego koloru.";
	}

	@Override
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Och, jestem rozczarowana. Nie masz " + Grammar.a_noun(itemName) + " ze sobą.";
	}

	@Override
	public String respondToOfferOfNotMissingItem() {
		return "Już przyniosłeś mi ten płaszcz.";
	}

	@Override
	public String respondToOfferOfNotNeededItem() {
		return "To nie jest prawdziwy płaszcz...";
	}

	@Override
	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze wróć później.";
	}

	@Override
	public void rewardPlayer(final Player player) {
		final Item blackcloak = SingletonRepository.getEntityManager().getItem("czarny płaszcz");
		blackcloak.setBoundTo(player.getName());
		player.equipOrPutOnGround(blackcloak);
		player.addKarma(25.0);
		player.addXP(50000);
	}

	@Override
	public String getName() {
		return "CloakCollector";
	}

	// You can start collecting just with a simple cloak which you can buy, but maybe not a good idea to send to Fado too early.
	@Override
	public int getMinLevel() {
		return 15;
	}

	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}

	@Override
	public String getNPCName() {
		return "Josephine";
	}
}
