/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
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

public class KolekcjonerRekawic extends AbstractQuest implements BringListOfItemsQuest {

	private static final List<String> NEEDED_GLOVES = Arrays.asList("skórzane rękawice",
			"skórzane wzmocnione rękawice", "skórzane twarde rękawice", "rękawice elfickie", "rękawice karmazynowe",
			"rękawice lazurowe", "rękawice szmaragdowe", "lodowe rękawice", "ogniste rękawice", "rękawice chaosu",
			"rękawice cieni", "rękawice mainiocyjskie", "rękawice xenocyjskie");

	private static final String QUEST_SLOT = "gloves_collector";

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
				"Rękawice dla Kolekcjonera",
				"Anastazja szuka rękawic w wielu kolorach.",
				false);
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Anastazja");

		// player asks about an individual cloak before accepting the quest
		for(final String itemName : NEEDED_GLOVES) {
			npc.add(ConversationStates.QUEST_OFFERED, itemName, null,
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("Nie znam " + obj.getOriginal() + ". Możesz podać nazwę innych rękawic?");
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
		return "Hej nieznajomy! Nie chciałbys coś mi przynieść? Miałabym dla ciebie wyzywające #zadanie na skompletowanie dla mnie rękawic!";
	}

	@Override
	public String welcomeDuringActiveQuest() {
		return "Cześć! Zebrałeś już dla mnie jakieś #rękawice?";
	}

	@Override
	public String welcomeAfterQuestIsCompleted() {
		return "Witaj ponownie! Rękawice wciąż prezentują się wspaniale. Dziękuję!";
	}

	@Override
	public String respondToQuest() {
		return "Mam obsesję na punkcie #rękawic! Chciałabym je wszystkie skolekcjonować!";
	}

	@Override
	public String respondToQuestAcception() {
		return "Super! Czekam z niecierpliowścią!";
	}

	@Override
	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Cześć! Rękawice prezentują się wspaniale. Dziękuję!";
	}

	@Override
	public String respondToQuestRefusal() {
		return "Och... nie jesteś zbyt miły. Do widzenia.";
	}

	@Override
	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Ekstra! Jakie #rękawice przyniosłeś?";
	}

	@Override
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Chcę " + Grammar.quantityplnoun(missingItems.size(), "rękawic", "rękawice")
				+ ". To są " + Grammar.enumerateCollection(missingItems)
				+ ". Dasz radę przynieść któreś z nich?";
	}

	@Override
	public String askForMissingItems(final List<String> missingItems) {
		return "Chcę " + Grammar.quantityplnoun(missingItems.size(), "rękawic", "rękawice")
				+ ". To są " + Grammar.enumerateCollection(missingItems)
				+ ". Masz już przy sobie jakiś?";
	}

	@Override
	public String respondToItemBrought() {
		return "Łał, dziękuję! Co jeszcze mi przyniosłeś?";
	}

	@Override
	public String respondToLastItemBrought() {
		return "Och, wszystkie wyglądają tak pięknie, dziękuję. Przyjmij ten specjalny prezent ode mnie oraz siostry!";
	}

	@Override
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Och, jestem rozczarowana. Nie masz " + Grammar.a_noun(itemName) + " ze sobą.";
	}

	@Override
	public String respondToOfferOfNotMissingItem() {
		return "Już je przynosiłeś...";
	}

	@Override
	public String respondToOfferOfNotNeededItem() {
		return "Nie mogę uwieżyć, że takie istnieją...";
	}

	@Override
	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze wróć później.";
	}

	@Override
	public void rewardPlayer(final Player player) {
		final Item killer_gloves = SingletonRepository.getEntityManager().getItem("rękawice zabójcy");
		killer_gloves.setBoundTo(player.getName());
		player.equipOrPutOnGround(killer_gloves);
		player.addKarma(30.0);
		player.addXP(100000);
	}

	@Override
	public String getName() {
		return "KolekcjonerRekawic";
	}

	// You can start collecting just with a simple cloak which you can buy, but maybe not a good idea to send to Fado too early.
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
