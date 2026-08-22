/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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

import java.util.Arrays;
import java.util.List;

import games.stendhal.common.ItemTools;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

public class GoralskiCollector extends AbstractQuest implements BringListOfItemsQuest {
	private static final String QUEST_SLOT = "goralski_kolekcjoner1";
	private final SpeakerNPC npc = npcs.get("Gazda Bartek");

	private static final List<String> MOUNTAINEER_ITEMS = Arrays.asList("góralski gorset", "kierpce",
			"chusta góralska", "ciupaga", "góralska spódnica", "góralska biała spódnica");

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
				"Gazda Bartek chce zachować pamięć o dawnym góralskim stroju i prosi o zebranie tradycyjnych części ubioru.",
				false);
	}

	private void step_1() {
		for(final String itemName : MOUNTAINEER_ITEMS) {
			npc.add(ConversationStates.QUEST_OFFERED, itemName, null,
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("Nie znam " + obj.getOriginal() + ". Możesz podać nazwę przedmiotu, o który pytasz?");
						} else {
							final Item item = SingletonRepository.getEntityManager().getItem(itemName);
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("Tego właśnie szukam do izby pamięci. To ");

							if (item == null) {
								stringBuilder.append(itemName);
							} else {
								stringBuilder.append(ItemTools.itemNameToDisplayName(item.getItemSubclass()));
							}

							stringBuilder.append(". Jeśli znajdziesz taki egzemplarz, przynieś go proszę.");
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
		return npcs.get(npc.getName());
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
		return "Witojże. Próbuję ocalić pamięć o dawnym góralskim stroju. Chcesz usłyszeć o #'ubraniach', których szukam?";
	}

	@Override
	public String welcomeDuringActiveQuest() {
		return "Dobrze cię widzieć. Izba pamięci wciąż nie jest kompletna. Masz przy sobie jakieś #'rzeczy'?";
	}

	@Override
	public String welcomeAfterQuestIsCompleted() {
		return "Dzięki tobie pierwszy góralski strój w izbie pamięci jest już kompletny. Dziękuję.";
	}

	@Override
	public String respondToQuest() {
		return "Coraz mniej ludzi pamięta, jak wyglądał pełny góralski strój. Chcę urządzić małą izbę pamięci i zachować te #rzeczy dla młodszych. Pomożesz mi skompletować pierwszy zestaw?";
	}

	@Override
	public String respondToQuestAcception() {
		return "Dziękuję. Każdy przyniesiony przedmiot będzie częścią historii, której nie chcę zgubić.";
	}

	@Override
	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Pierwszy strój jest już kompletny i może zostać w izbie pamięci na długie lata. Dziękuję.";
	}

	@Override
	public String respondToQuestRefusal() {
		return "Rozumiem. Jeśli zmienisz zdanie, wróć do mnie.";
	}

	@Override
	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Dobrze. Jakie #rzeczy udało ci się odnaleźć?";
	}

	@Override
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Do pierwszego stroju potrzebuję " + Grammar.quantityplnoun(missingItems.size(), "przedmiot")
				+ ". Są to #'" + Grammar.enumerateCollection(missingItems)
				+ "'. Pomożesz mi je odnaleźć?";
	}

	@Override
	public String askForMissingItems(final List<String> missingItems) {
		return "Do ukończenia stroju brakuje " + Grammar.quantityplnoun(missingItems.size(), "przedmiot")
				+ ". Są to " + Grammar.enumerateCollection(missingItems)
				+ ". Masz któryś przy sobie?";
	}

	@Override
	public String respondToItemBrought() {
		return "Dziękuję. Ten przedmiot trafi do izby pamięci. Masz coś jeszcze?";
	}

	@Override
	public String respondToLastItemBrought() {
		return "Udało się skompletować cały strój. Dzięki tobie nie zginie pamięć o tym, jak nosili się dawni górale. Przyjmij te korale. Leżały w mojej skrzyni od lat, a tobie mogą się jeszcze przydać.";
	}

	@Override
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Nie masz przy sobie " + itemName + ". Sprawdź proszę, czy niczego nie zostawiłeś po drodze.";
	}

	@Override
	public String respondToOfferOfNotMissingItem() {
		return "Ten egzemplarz już mamy. Poszukajmy pozostałych części stroju.";
	}

	@Override
	public String respondToOfferOfNotNeededItem() {
		return "Tego przedmiotu nie potrzebuję do tego stroju.";
	}

	@Override
	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze. Wróć, kiedy uda ci się coś odnaleźć.";
	}

	@Override
	public void rewardPlayer(final Player player) {
		final Item korale = SingletonRepository.getEntityManager().getItem(
				"korale", ItemCreationContext.questReward());
		korale.setBoundTo(player.getName());
		player.equipOrPutOnGround(korale);
		player.addKarma(15.0);
		player.addXP(35000);
	}

	@Override
	public String getName() {
		return "Góralski Kolekcjoner I";
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
