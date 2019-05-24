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

public class GoralskiCollector extends AbstractQuest implements BringListOfItemsQuest {

	private static final List<String> MOUNTAINEER_ITEMS = Arrays.asList("góralski gorset",
			"chusta góralska", "ciupaga", "góralska spódnica", "góralska biała spódnica",
			"polska tarcza lekka");

	private static final String QUEST_SLOT = "goralski_kolekcjoner1";

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
				"Góralski Kolekcjoner I",
				"Gazda Bartek jest kolekcjonerem i zbiera wszystkie ubrania związane z góralstwem.",
				false);
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Gazda Bartek");

		// player asks about an individual cloak before accepting the quest
		for(final String itemName : MOUNTAINEER_ITEMS) {
			npc.add(ConversationStates.QUEST_OFFERED, itemName, null,
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("Nie znam " + obj.getOriginal() + ". Możesz podać nazwę?");
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
		return npcs.get("Gazda Bartek");
	}

	@Override
	public List<String> getNeededItems() {
		return MOUNTAINEER_ITEMS;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("items", "rzeczy", "przedmioty");
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
		return "Witojże w mej krainie górskiej. Chciałbyś zobaczyć me #'ubrania'..?";
	}

	@Override
	public String welcomeDuringActiveQuest() {
		return "Zawsze chciałem poszerzyć swoją kolejcę o kolejne przedmioty związane z góralstwem! Masz przy sobie jakieś #'rzeczy'?";
	}

	@Override
	public String welcomeAfterQuestIsCompleted() {
		return "Góralskie ubrania wyglądają jak nowe. Niepotrzebuję nowszych. Dziękuję!";
	}

	@Override
	public String respondToQuest() {
		return "Od urodzenia zawsze chciałem zbierać wszystkie #rzeczy góralskie. Chciałem je kolekcjonować!";
	}

	@Override
	public String respondToQuestAcception() {
		// player.addKarma(5.0);
		return "Wspaniale! Będę tutaj czekał jak wrócisz.";
	}

	@Override
	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Góralskie ubrania wyglądają jak nowe. Niepotrzebuję nowszych. Dziękuję!";
	}

	@Override
	public String respondToQuestRefusal() {
		// player.addKarma(-5.0);
		return "Och ... nie jesteś zbyt miły. Dowidzenia.";
	}

	@Override
	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Cudownie! Jakie #rzeczy przyniosłeś mi?";
	}

	@Override
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Chcę " + Grammar.quantityplnoun(missingItems.size(), "item", "a")
				+ ". To jest #'" + Grammar.enumerateCollection(missingItems)
				+ "'. Pomógłbyś mi je zdobyć?";
	}

	@Override
	public String askForMissingItems(final List<String> missingItems) {
		return "Chcę " + Grammar.quantityplnoun(missingItems.size(), "item", "a")
				+ ". To jest " + Grammar.enumerateCollection(missingItems)
				+ ". Przyniosłeś mi jakiś?";
	}

	@Override
	public String respondToItemBrought() {
		return "Ohh... Bardzo dziękuję! Co jeszcze przyniosłeś?";
	}

	@Override
	public String respondToLastItemBrought() {
		return "Dziękuję bardzo.. za te góralskie rzeczy.. Proszę, przyjmij korale, mogą Ci się kiedyś przydać.";
	}

	@Override
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Rozczarowałeś mnie... Nie masz " + Grammar.a_noun(itemName) + " przy sobie...";
	}

	@Override
	public String respondToOfferOfNotMissingItem() {
		return "Już mi to przyniosłeś kiedyś...";
	}

	@Override
	public String respondToOfferOfNotNeededItem() {
		return "To nie jest prawdziwa rzecz, o którą prosiłem...";
	}

	@Override
	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze wróć później.";
	}

	@Override
	public void rewardPlayer(final Player player) {
		final Item korale = SingletonRepository.getEntityManager().getItem("korale");
		korale.setBoundTo(player.getName());
		player.equipOrPutOnGround(korale);
		player.addKarma(15.0);
		player.addXP(35000);
	}

	@Override
	public String getName() {
		return "GoralskiCollector";
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}

	@Override
	public String getNPCName() {
		return "Gazda Bartek";
	}
}
