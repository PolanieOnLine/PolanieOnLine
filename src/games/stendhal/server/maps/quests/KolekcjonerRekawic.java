/***************************************************************************
 *                   (C) Copyright 2020-2021 - Stendhal                    *
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

public class KolekcjonerRekawic extends AbstractQuest implements BringListOfItemsQuest {
	private static final String QUEST_SLOT = "gloves_collector";

	private static final List<String> NEEDED_GLOVES = Arrays.asList("skórzane rękawice",
			"skórzane wzmocnione rękawice", "skórzane twarde rękawice", "rękawice elfickie", "rękawice karmazynowe",
			"rękawice lazurowe", "rękawice szmaragdowe", "lodowe rękawice", "ogniste rękawice", "rękawice chaosu",
			"rękawice cieni", "rękawice mainiocyjskie", "rękawice xenocyjskie");

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

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Anastazja");

		for(final String itemName : NEEDED_GLOVES) {
			npc.add(ConversationStates.QUEST_OFFERED, itemName, null,
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("Nie znam " + obj.getOriginal() + ". Możesz podać nazwę rękawic, o które pytasz?");
						} else {
							final Item item = SingletonRepository.getEntityManager().getItem(itemName);
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("Chcę obejrzeć te rękawice jako wzór wykonania. To ");

							if (item == null) {
								stringBuilder.append(itemName);
							} else {
								stringBuilder.append(ItemTools.itemNameToDisplayName(item.getItemSubclass()));
							}

							stringBuilder.append(". Jeśli znajdziesz taki egzemplarz, przynieś go do mojej pracowni.");
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
		return Arrays.asList("quest", "task", "zadanie");
	}

	@Override
	public SpeakerNPC getNPC() {
		return npcs.get("Anastazja");
	}

	@Override
	public List<String> getNeededItems() {
		return NEEDED_GLOVES;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("gloves", "rękawice");
	}

	@Override
	public double getKarmaDiffForQuestResponse() {
		return 2.0;
	}

	@Override
	public boolean shouldWelcomeAfterQuestIsCompleted() {
		return false;
	}

	@Override
	public String welcomeBeforeStartingQuest() {
		return "Witaj. Chcę zostać projektantką mody, ale dobry projekt musi być nie tylko ładny. Potrzebuję pomocy przy #zadaniu związanym z rękawicami.";
	}

	@Override
	public String welcomeDuringActiveQuest() {
		return "Cześć. Udało ci się zdobyć jakieś #rękawice do moich badań?";
	}

	@Override
	public String welcomeAfterQuestIsCompleted() {
		return "Dzięki zebranym rękawicom wiem już dużo więcej o materiałach, ochronie dłoni i swobodzie ruchu. Dziękuję.";
	}

	@Override
	public String respondToQuest() {
		return "Chcę porównać #rękawice wykonywane przez różne ludy. Jedne stawiają na lekkość, inne na ochronę, a jeszcze inne wykorzystują niezwykłe materiały. Jeśli poznam te rozwiązania, będę mogła tworzyć lepsze własne projekty. Pomożesz mi zebrać wzory?";
	}

	@Override
	public String respondToQuestAcception() {
		return "Dziękuję. Każdą parę dokładnie obejrzę i zapiszę, co wyróżnia jej wykonanie.";
	}

	@Override
	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Mam już wszystkie potrzebne wzory. Teraz mogę zacząć tworzyć własne projekty z dużo większą wiedzą.";
	}

	@Override
	public String respondToQuestRefusal() {
		return "Rozumiem. Jeśli zmienisz zdanie, wróć do mnie.";
	}

	@Override
	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Dobrze. Jakie #rękawice udało ci się zdobyć?";
	}

	@Override
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Do porównania potrzebuję " + Grammar.quantityplnoun(missingItems.size(), "rękawic")
				+ ". Są to " + Grammar.enumerateCollection(missingItems)
				+ ". Pomożesz mi je zdobyć?";
	}

	@Override
	public String askForMissingItems(final List<String> missingItems) {
		return "Do ukończenia zbioru wzorów potrzebuję jeszcze " + Grammar.quantityplnoun(missingItems.size(), "rękawic")
				+ ". Są to " + Grammar.enumerateCollection(missingItems)
				+ ". Masz którąś parę przy sobie?";
	}

	@Override
	public String respondToItemBrought() {
		return "Dziękuję. Ten wzór dużo mi pokaże. Masz coś jeszcze?";
	}

	@Override
	public String respondToLastItemBrought() {
		return "To ostatnia para. Teraz mam pełne porównanie lekkich, ciężkich i niezwykłych rękawic. Przyjmij ode mnie prezent za całą pomoc.";
	}

	@Override
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Nie masz przy sobie " + itemName + ". Wróć, gdy uda ci się je zdobyć.";
	}

	@Override
	public String respondToOfferOfNotMissingItem() {
		return "Ten wzór już mam opisany. Poszukajmy pozostałych.";
	}

	@Override
	public String respondToOfferOfNotNeededItem() {
		return "Tych rękawic nie potrzebuję do obecnego porównania.";
	}

	@Override
	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze. Wróć, kiedy zdobędziesz kolejną parę.";
	}

	@Override
	public void rewardPlayer(final Player player) {
		final Item killer_belt = SingletonRepository.getEntityManager().getItem(
				"pas zabójcy", ItemCreationContext.questReward());
		killer_belt.setBoundTo(player.getName());
		player.equipOrPutOnGround(killer_belt);
		player.addKarma(30.0);
		player.addXP(100000);
	}

	@Override
	public void addToWorld() {
		step_1();
		setupAbstractQuest();
		fillQuestInfo(
				"Rękawice Kolekcjonerki",
				"Anastazja chce poznać sposoby wykonywania rękawic przez różne ludy, aby wykorzystać tę wiedzę we własnych projektach.",
				false);
	}

	@Override
	public String getName() {
		return "Rękawice Kolekcjonerki";
	}

	@Override
	public int getMinLevel() {
		return 55;
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return "Anastazja";
	}
}
