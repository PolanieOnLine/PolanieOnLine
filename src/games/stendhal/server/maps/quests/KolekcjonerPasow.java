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

public class KolekcjonerPasow extends AbstractQuest implements BringListOfItemsQuest {
	private static final String QUEST_SLOT = "belts_collector";

	private static final List<String> NEEDED_BELTS = Arrays.asList("pas skórzany",
			"wzmocniony pas skórzany", "pas kolczy", "lodowy pas", "ognisty pas", "złoty pas kolczy",
			"pas karmazynowy", "pas lazurowy", "pas barbarzyńcy", "pas elficki", "pas zbójnicki",
			"pas cieni", "pas kamienny", "pas chaosu", "pas krasnoludzki", "złoty pas", "pas olbrzymi",
			"pas mainiocyjski", "pas xenocyjski");

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
		final SpeakerNPC npc = npcs.get("Eltefia");

		for(final String itemName : NEEDED_BELTS) {
			npc.add(ConversationStates.QUEST_OFFERED, itemName, null,
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("Nie znam " + obj.getOriginal() + ". Możesz podać nazwę pasa, o który pytasz?");
						} else {
							final Item item = SingletonRepository.getEntityManager().getItem(itemName);
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("Chcę obejrzeć ten pas jako wzór rzemiosła. To ");

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
		return npcs.get("Eltefia");
	}

	@Override
	public List<String> getNeededItems() {
		return NEEDED_BELTS;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("belts", "pasy", "pas", "pasów");
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
		return "Witaj. Chcę kiedyś projektować własne pasy, ale najpierw muszę poznać rzemiosło różnych ludów. Mam dla ciebie #zadanie.";
	}

	@Override
	public String welcomeDuringActiveQuest() {
		return "Cześć. Udało ci się zdobyć jakieś #pasy do mojej kolekcji wzorów?";
	}

	@Override
	public String welcomeAfterQuestIsCompleted() {
		return "Dzięki zebranym wzorom mogę wreszcie zacząć projektować własne pasy. Dziękuję.";
	}

	@Override
	public String respondToQuest() {
		return "Nie chcę kopiować jednego stylu. Chcę porównać #pasy ludzi, elfów, krasnoludów i innych ludów, zobaczyć jak dobierają materiały oraz wzmocnienia, a potem stworzyć własny projekt. Pomożesz mi zebrać wzory?";
	}

	@Override
	public String respondToQuestAcception() {
		return "Świetnie. Każdy pas obejrzę dokładnie i zapiszę, czego mogę się z niego nauczyć.";
	}

	@Override
	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Mam już wszystkie potrzebne wzory. Teraz czas zamienić notatki w prawdziwe projekty.";
	}

	@Override
	public String respondToQuestRefusal() {
		return "Rozumiem. Jeśli zmienisz zdanie, wróć do mnie.";
	}

	@Override
	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Dobrze. Jakie #pasy udało ci się zdobyć?";
	}

	@Override
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Do porównania potrzebuję " + Grammar.quantityplnoun(missingItems.size(), "pasów")
				+ ". Są to " + Grammar.enumerateCollection(missingItems)
				+ ". Pomożesz mi je zdobyć?";
	}

	@Override
	public String askForMissingItems(final List<String> missingItems) {
		return "Do ukończenia zbioru wzorów potrzebuję jeszcze " + Grammar.quantityplnoun(missingItems.size(), "pasów")
				+ ". Są to " + Grammar.enumerateCollection(missingItems)
				+ ". Masz któryś przy sobie?";
	}

	@Override
	public String respondToItemBrought() {
		return "Dziękuję. Ten wzór dużo mi pokaże. Masz coś jeszcze?";
	}

	@Override
	public String respondToLastItemBrought() {
		return "To ostatni wzór. Teraz widzę, jak wiele można osiągnąć samym doborem materiału, zapięcia i wzmocnienia. Przyjmij ode mnie prezent za całą pomoc.";
	}

	@Override
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Nie masz przy sobie " + itemName + ". Wróć, gdy uda ci się go zdobyć.";
	}

	@Override
	public String respondToOfferOfNotMissingItem() {
		return "Ten wzór już mam opisany. Poszukajmy pozostałych.";
	}

	@Override
	public String respondToOfferOfNotNeededItem() {
		return "Tego pasa nie potrzebuję do obecnego porównania.";
	}

	@Override
	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze. Wróć, kiedy zdobędziesz kolejny wzór.";
	}

	@Override
	public void rewardPlayer(final Player player) {
		final Item killer_gloves = SingletonRepository.getEntityManager().getItem(
				"rękawice zabójcy", ItemCreationContext.quest());
		killer_gloves.setBoundTo(player.getName());
		player.equipOrPutOnGround(killer_gloves);
		player.addKarma(40.0);
		player.addXP(150000);
	}

	@Override
	public void addToWorld() {
		step_1();
		setupAbstractQuest();
		fillQuestInfo(
				"Pasy Kolekcjonerki",
				"Eltefia chce poznać rzemiosło różnych ludów, aby na podstawie zebranych pasów stworzyć własne projekty.",
				false);
	}

	@Override
	public String getName() {
		return "Pasy Kolekcjonerki";
	}

	@Override
	public int getMinLevel() {
		return 70;
	}

	@Override
	public String getRegion() {
		return Region.WARSZAWA;
	}

	@Override
	public String getNPCName() {
		return "Eltefia";
	}
}
