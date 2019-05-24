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
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

import java.util.Arrays;
import java.util.List;

/**
 * QUEST: The Weapons Collector
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Balduin, a hermit living on a mountain between Semos and Ados </li>
 * </ul>
 * <p>
 * STEPS:
 * <ul>
 * <li> Balduin asks you for some weapons. </li>
 * <li> You get one of the weapons somehow, e.g. by killing a monster. </li>
 * <li> You bring the weapon up the mountain and give it to Balduin. </li>
 * <li> Repeat until Balduin received all weapons. (Of course you can bring up several weapons at the same time.) </li>
 * <li> Balduin gives you an ice sword in exchange. </li>
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> ice sword </li>
 * <li> 5000 XP </li>
 * <li> 30 karma </li>
 * </ul>
 * <p>
 * REPETITIONS: None
 */
public class WeaponsCollector extends AbstractQuest implements
		BringListOfItemsQuest {

	private static final List<String> neededWeapons = Arrays.asList("berdysz",
			"miecz zaczepny", "pałasz", "kiścień", "miecz", "katana",
			"złoty buzdygan", "bułat", "kosa", "miecz elficki");

	private static final String QUEST_SLOT = "weapons_collector";

  private BringListOfItemsQuestLogic bringItems;

	@Override
	public List<String> getHistory(final Player player) {
		return bringItems.getHistory(player);
	}

	@Override
	public void addToWorld() {
			fillQuestInfo(
				"Kolekcjoner Broni",
				"Balduin, pustelnik, który żyje w górach Ados, ma ekscytujące wyzwanie dla Ciebie.",
				true);
		setupAbstractQuest();
	}

	private void setupAbstractQuest() {
		final BringListOfItemsQuest concreteQuest = this;
		bringItems = new BringListOfItemsQuestLogic(concreteQuest);
		bringItems.addToWorld();
	}

	@Override
	public SpeakerNPC getNPC() {
		return npcs.get("Balduin");
	}

	@Override
	public List<String> getNeededItems() {
		return neededWeapons;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("collection", "kolekcji", "kolekcja");
	}

	@Override
	public List<String> getAdditionalTriggerPhraseForQuest() {
		return ConversationPhrases.EMPTY;
	}

	@Override
	public double getKarmaDiffForQuestResponse() {
		return 0;
	}

	@Override
	public String welcomeBeforeStartingQuest() {
		return "Pozdrawiam. Jestem Balduin. Interesujesz się bronią? "
				+ "Kolekcjonowałem ją odkąd byłem "
				+ "młody. Może zrobisz dla mnie małe #zadanie.";
	}

	@Override
	public String welcomeDuringActiveQuest() {
		return "Witaj ponownie. Mam nadzieję, że przyszedłeś, aby mi pomóc w uzupełnieniu mojej #kolekcji.";
	}

	@Override
	public String welcomeAfterQuestIsCompleted() {
		return "Witam! Jeszcze raz dziękuję za skompletowanie mojej kolekcji.";
	}

	@Override
	public boolean shouldWelcomeAfterQuestIsCompleted() {
		// because of WeaponsCollector2
		return false;
	}

	@Override
	public String respondToQuest() {
		return "Kolekcjonowałem broń od dłuższego czasu "
				+ "wciąż nie mam tego czego chcę. Czy mógłbyś mi "
				+ "pomóc w skompletowaniu mojej #kolekcji?";
	}

	@Override
	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Moja kolekcja jest już skompletowana! Jeszcze raz dziękuję.";
	}

	@Override
	public String respondToQuestAcception() {
		return "Jeżeli pomożesz mi w skompletowaniu mojej kolekcji to dam Tobie "
				+ "coś bardzo interesującego i użytecznego na wymianę. Dowidzenia";
	}

	@Override
	public String respondToQuestRefusal() {
		return "Cóż. Może ktoś inny mi pomoże. Dowidzenia";
	}

	@Override
	public String askForMissingItems(final List<String> missingItems) {
		return "Wciąż brakuje mi " + Grammar.isare(missingItems.size()) + " "
				+ Grammar.quantityplnoun(missingItems.size(), "weapon", "a")
				+ " w mojej kolekcji: "
				+ Grammar.enumerateCollection(missingItems)
				+ ". Czy masz coś ze sobą?";
	}
	@Override
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Brakuje mi " + Grammar.isare(missingItems.size()) + " "
				+ Grammar.quantityplnoun(missingItems.size(), "weapon", "a")
				+ " z mojej kolekcji: "
				+ Grammar.enumerateCollection(missingItems)
				+ ". Przyniesiesz mi je?";
	}


	@Override
	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Daj mi znać jeżeli coś znajdziesz "
				+ Grammar.itthem(missingItems.size()) + ". Żegnaj.";
	}

	@Override
	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Co dla mnie znalazłeś?";
	}

	@Override
	public String respondToItemBrought() {
		return "Bardzo dziękuję! Masz coś jeszcze dla mnie?";
	}

	@Override
	public String respondToLastItemBrought() {
		return "Nareszcie moja kolekcja jest kompletna! Dziękuję bardzo i "
				+ "weź ten #miecz #lodowy w zamian!";
	}

	@Override
	public void rewardPlayer(final Player player) {
		final Item iceSword = SingletonRepository.getEntityManager().getItem("miecz lodowy");
		iceSword.setBoundTo(player.getName());
		player.equipOrPutOnGround(iceSword);
		player.addXP(50000);
		player.addKarma(30);
	}

	@Override
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Może jestem stary, ale nic nie masz "
				+ Grammar.a_noun(itemName)
				+ ". Czego tak naprawdę chcesz ode mnie?";
	}

	@Override
	public String respondToOfferOfNotMissingItem() {
		return "Mam już to. Czy masz dla mnie inną broń?";
	}

	@Override
	public String respondToOfferOfNotNeededItem() {
		return "To nie jest interesująca broń";
	}

	@Override
	public String getName() {
		return "WeaponsCollector";
	}

 	// it can be a long quest so they can always start it before they can necessarily finish all
	@Override
	public int getMinLevel() {
		return 30;
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
