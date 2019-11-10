/***************************************************************************
 *                   (C) Copyright 2019 - Stendhal                         *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.antivenom_ring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: Antivenom Ring
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Jameson (the retired apothecary)</li>
 * <li>Zoey (zoologist at animal sanctuary)</li>
 * <li>Other NPCs to give hints at location of apothecary's lab: Valo, Haizen, & Ortiv Milquetoast</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Complete Traps for Klaas quest to gain entrance into apothecary's lab.</li>
 * <li>Bring note to apothecary to Jameson.</li>
 * <li>As a favor to Klaas, Jameson will help you to strengthen your medicinal ring.</li>
 * <li>Bring Jameson a medicinal ring, cobra venom, 2 mandragora and 5 fairycakes.</li>
 * <li>Jameson infuses the ring.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>2000 XP</li>
 * <li>antivenom ring</li>
 * <li>Karma: 25???</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 *
 *
 * @author AntumDeluge
 */
public class AntivenomRing extends AbstractQuest {

	private static final String QUEST_SLOT = "antivenom_ring";

	// NPCs involved in quest
	private final static String apothecary = "Jameson";
	private final static String zoologist = "Zoey";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			res.add("Znalazłem aptekarza pustelnika w laboratorium w górach Semos.");

			final String questState = player.getQuest(QUEST_SLOT);
			if (questState.equals("done")) {
				res.add("Znalazłem wszystko o co poprosił mnie " + apothecary + ".");
				res.add("Użył specjalnej mikstury na moim pierścieniu, która zwiększyła odporność na truciznę. Dostałem także PD i karmę.");
			} else if (questState.equals("rejected")) {
				res.add("Trucizna jest zbyt niebezpieczna. Nie chcę sobie zaszkodzić.");
			} else {
				if (questState.startsWith("enhancing;")) {
					res.add("Znalazłem wszystko o co poprosił mnie " + apothecary + ".");
					res.add(apothecary + " teraz ulepsza mój pierścień.");
				} else {
					ItemCollection itemList = new ItemCollection();
					itemList.addFromString(questState.replace(";", ","));

					res.add(apothecary + " poprosił mnie o przyniesienie niektórzych przedmiotów. Wciąż muszę je dostarczyć " + apothecary + " " + Grammar.enumerateCollection(itemList.toStringList()) + ".");
				}
			}
		}

		return res;
	}

	private void prepareHintNPCs() {
		final SpeakerNPC hintNPC1 = npcs.get("Valo");
		final SpeakerNPC hintNPC2 = npcs.get("Haizen");
		final SpeakerNPC hintNPC3 = npcs.get("Ortiv Milquetoast");

		// Valo is asked about an apothecary
		hintNPC1.add(ConversationStates.ATTENDING,
				Arrays.asList("apothecary", "aptekarz"),
				null,
				ConversationStates.ATTENDING,
				"Hmmm, tak znałem osobę dawno temu, która studiowała medycynę i antytrucizny. Ostatnio słyszałem, że na #emeryturę przeniósł się w góry.",
				null);

		hintNPC1.add(ConversationStates.ATTENDING,
				Arrays.asList("retreat", "retreats", "retreating", "emeryturę"),
				null,
				ConversationStates.ATTENDING,
				"Prawdopodobnie się ukrywa. Zwracaj uwagę na ukryte przejścia.",
				null);

		// Haizen is asked about an apothecary
		hintNPC2.add(ConversationStates.ATTENDING,
				Arrays.asList("apothecary", "aptekarz"),
				null,
				ConversationStates.ATTENDING,
				"Tak, była jedna osoba w Kalavan, ale z powodu problemów z przywódcami został zmuszony do odejścia. Słyszałem, że #ukrywa się gdzieś w rejonie Semos.",
				null);

		hintNPC2.add(ConversationStates.ATTENDING,
				Arrays.asList("hide", "hides", "hiding", "hidden", "ukrywa", "ukryty", "ukrycia"),
				null,
				ConversationStates.ATTENDING,
				"Gdybym ja się ukrywał to na pewno zbudowałbym ukryty pokój z ukrytym wejściem.",
				null);

		// Ortiv Milquetoast is asked about an apothecary
		hintNPC3.add(ConversationStates.ATTENDING,
				Arrays.asList("apothecary", "aptekarz"),
				null,
				ConversationStates.ATTENDING,
				"Musisz porozmawiać z moim kolegą Jamesonem. Został on zmuszony do #ukrycia się z powodu problemów w Kalavan. Nie wiem gdzie, ale przynosi mi podczas każdej wizyty przepyszne gruszki.",
				null);

		hintNPC3.add(ConversationStates.ATTENDING,
				Arrays.asList("hide", "hides", "hiding", "hidden", "ukrywa", "ukryty", "ukrycia"),
				null,
				ConversationStates.ATTENDING,
				"Napomknął, że zbudował ukryte laboratorium i coś o ukrytych drzwiach.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pierścień Antyjadowy",
				"Jak przysługę dla starego przyjaciela aptekarz Jameson wzmocni pierścień leczniczy.",
				false);

		prepareHintNPCs();
		new ApothecaryStage(apothecary, QUEST_SLOT).addToWorld();
		new ZoologistStage(zoologist, QUEST_SLOT).addToWorld();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "AntivenomRing";
	}

	public String getTitle() {
		return "AntivenomRing";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}

	@Override
	public String getNPCName() {
		return apothecary;
	}
}
