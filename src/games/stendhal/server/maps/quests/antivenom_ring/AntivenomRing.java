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
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
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
 * <li>Ognir (the ring maker)</li>
 * <li>Other NPCs to give hints at location of apothecary's lab: Klaas, Julius, Valo, Haizen, & Ortiv Milquetoast</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Complete Traps for Klaas quest to gain entrance into apothecary's lab.</li>
 * <li>Bring note to apothecary to Jameson.</li>
 * <li>As a favor to Klaas, Jameson will help you to strengthen your medicinal ring.</li>
 * <li>Bring Jameson a cobra venom, 2 mandragora, & 20 fairycakes.</li>
 * <li>Jameson mixes an antivenom.</li>
 * <li>Bring antivenom, medicinal ring, & 1,000 money to Ognir</li>
 * <li>Ognir makes the medicinal ring stronger</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>3000 XP (1000 from Jameson & 2000 from Ognir)</li>
 * <li>antivenom ring</li>
 * <li>Karma: 205 total (50 (+5 for starting) from Jameson & 150 from Ognir)</li>
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
	private final static String ringmaker = "Ognir";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			res.add(player.getGenderVerb("Znalazłem") + " aptekarza pustelnika w laboratorium w górach Semos.");

			final String questState = player.getQuest(QUEST_SLOT, 0);
			if (questState.equals("rejected")) {
				res.add("Trucizna jest zbyt niebezpieczna. Nie chcę sobie zaszkodzić.");
			} else {
				if (questState.equals("mixing") || questState.equals("ringmaker") || questState.equals("fusing") || questState.equals("done")) {
					res.add(player.getGenderVerb("Znalazłem") + " wszystko o co mnie poprosił " + apothecary + ".");
					if (questState.equals("mixing")) {
						res.add("Aktualnie miesza dla mnie antyjad.");
					} else if (questState.equals("ringmaker") || questState.equals("fusing") || questState.equals("done")) {
						res.add("Stworzył już specjalną mieszaninę antyjadu.");
						res.add(apothecary + " powiedział, abym odnalazł " + ringmaker + "'a, który mógłby wykorzystać antyjad do zwiększenia odporności "
								+ "mojego pierścienia leczniczego. Powinienem się go spytać o pierścień antyjadowy.");
						if (questState.equals("fusing")) {
							res.add(ringmaker + " aktualnie aplikuje antyjad do mojego pierścienia.");
						} else if (questState.equals("done")) {
							res.add(ringmaker + " skończył swą pracę nad moim pierścieniem.");
						}
					}
				} else {
					final ItemCollection itemList = new ItemCollection();
					itemList.addFromString(player.getQuest(QUEST_SLOT).replace(";", ","));

					if (itemList.size() > 0) {
						res.add("Wciąż muszę zebrać przedmioty dla " + apothecary + ": " + Grammar.enumerateCollection(itemList.toStringList()) + ".");
						res.add(apothecary + " zalecił, żebym odwiedził sanktuarium dla zwierząt w pobliżu Ados, aby dowiedzieć się, "
								+ "jak mogę zdobyć jad kobry.");
					}
				}
			}
		}

		return res;
	}

	private void prepareHintNPCs() {
		final SpeakerNPC hintNPC1 = npcs.get("Klaas");
		final SpeakerNPC hintNPC2 = npcs.get("Julius");
		final SpeakerNPC hintNPC3 = npcs.get("Valo");
		final SpeakerNPC hintNPC4 = npcs.get("Haizen");
		final SpeakerNPC hintNPC5 = npcs.get("Ortiv Milquetoast");

		final List<String> query_phrases = Arrays.asList("apothecary", "aptekarz", apothecary, "antivenom");

		// TODO: Make sure that this doensn't interfere with any other quest

		/* Klaas */

		hintNPC1.add(
				ConversationStates.ATTENDING,
				query_phrases,
				new QuestCompletedCondition("traps_for_klaas"),
				ConversationStates.ATTENDING,
				"Kiedyś znałem starego aptekarza, ale nie wiem, gdzie się osiedlił. Może ktoś w mieście Ados by o tym wiedział."
				+ " Strażnicy patrolują miasto. Widzą wiele rzeczy, których inni nie widzą. Jak przykładowo o #aptekarzu.",
				null);

		/* Julius */

		hintNPC2.add(
				ConversationStates.ATTENDING,
				query_phrases,
				null,
				ConversationStates.ATTENDING,
				"Wielokrotnie byłem świadkiem spotkania #" + hintNPC3.getName() + " ze starym aptekarzem.",
				null);

		hintNPC2.add(
				ConversationStates.ATTENDING,
				hintNPC3.getName(),
				null,
				ConversationStates.ATTENDING,
				hintNPC3.getName() + " jest uzdrowicielem, który badał mikstury lecznicze u aptekarza. Zazwyczaj jest w #Kościele.",
				null);

		hintNPC2.add(
				ConversationStates.ATTENDING,
				Arrays.asList("Church", "Kościele", "Kościół"),
				null,
				ConversationStates.ATTENDING,
				"Mam #mapę, jeśli masz problem ze znalezieniem... Och, chyba moja mapa nie jest zaktualizowana"
				+ " w tej części Ados. Jest na południe od ratusza.",
				null);

		/* Valo */

		hintNPC3.add(
				ConversationStates.ATTENDING,
				query_phrases,
				null,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new NPCEmoteAction("drapie się po brodzie", false),
					new SayTextAction("Hmmm, tak znałem osobę dawno temu, która studiowała medycynę i antytrucizny. Ostatnio słyszałem, że na #emeryturę przeniósł się w góry.")
				)
			);

		hintNPC3.add(ConversationStates.ATTENDING,
				Arrays.asList("retreat", "retreats", "retreating", "retreated", "emeryturę", "ukrył", "odstąpił"),
				null,
				ConversationStates.ATTENDING,
				"Prawdopodobnie się #ukrywa. Zwracaj uwagę na ukryte przejścia.",
				null);

		hintNPC3.add(ConversationStates.ATTENDING,
				Arrays.asList("hide", "hides", "hiding", "hidden", "ukrywa", "ukryty", "ukrycia"),
				null,
				ConversationStates.ATTENDING,
				"Przepraszam, nie mam więcej informacji. Być może #" + hintNPC4.getName() + " wiedziałby więcej.",
				null);

		hintNPC3.add(ConversationStates.ATTENDING,
				hintNPC4.getName(),
				null,
				ConversationStates.ATTENDING,
				hintNPC4.getName() + " jest czarodziejem, który mieszka na zachód od miasta Ados.",
				null);

		/* Haizen */

		hintNPC4.add(ConversationStates.ATTENDING,
				query_phrases,
				null,
				ConversationStates.ATTENDING,
				"Tak, była jedna osoba w Kalavan, ale z powodu problemów z przywódcami został zmuszony do odejścia. Słyszałem, że #ukrywa się gdzieś w rejonie Semos.",
				null);

		hintNPC4.add(ConversationStates.ATTENDING,
				Arrays.asList("hide", "hides", "hiding", "hidden", "ukrywa", "ukryty", "ukrycia"),
				null,
				ConversationStates.ATTENDING,
				"Gdybym ja się ukrywał to na pewno zbudowałbym #'sekretny pokój' z ukrytym wejściem.",
				null);

		hintNPC4.add(ConversationStates.ATTENDING,
				Arrays.asList("secret", "secrets", "secret room", "secret rooms", "ukryty pokój", "ukryte pokoje", "sekret", "sekretny", "sekretny pokój", "sekretne pokoje"),
				null,
				ConversationStates.ATTENDING,
				"Przepraszam, nie mam więcej informacji. Być może #'" + hintNPC5.getName() + "' wiedziałby więcej.",
				null);

		hintNPC4.add(ConversationStates.ATTENDING,
				Arrays.asList(hintNPC5.getName(), "Ortiv"),
				null,
				ConversationStates.ATTENDING,
				hintNPC5.getName() + " jest byłym instruktorem alchemii, przeszedł na emeryturę i mieszka we wiosce Kirdneh.",
				null);

		/* Ortiv Milquetoast */

		hintNPC5.add(
			ConversationStates.ATTENDING,
			query_phrases,
			null,
			ConversationStates.ATTENDING,
			"Musisz porozmawiać z moim kolegą Jamesonem. Został on zmuszony do #ukrycia się z powodu problemów w Kalavan."
			+ " Nie wiem gdzie, ale przynosi mi podczas każdej wizyty przepyszne gruszki.",
			null);

		hintNPC5.add(
			ConversationStates.ATTENDING,
			Arrays.asList("hide", "hides", "hiding", "hidden", "ukrywa", "ukryty", "ukrycia"),
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(
				new SayTextAction(
					"Napomknął, że zbudował sekretne laboratorium i coś o ukrytych drzwiach."
					+ " Czy wspominałem, jak smaczne są #gruszki, które przynosi?"),
				new NPCEmoteAction("oblizuje wargi", false)
				)
			);

		hintNPC5.add(
			ConversationStates.ATTENDING,
			Arrays.asList("pear", "pears", "gruszka", "gruszki"),
			null,
			ConversationStates.ATTENDING,
			"Moi przyjaciele mówią mi, że gruszki można znaleźć w górach Semos. Jeśli będziesz tam podróżował,"
			+ " to proszę, przynieś mi ich trochę.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pierścień Antyjadowy",
				"Jako przysługę dla starego przyjaciela, aptekarz Jameson pomoże wzmocnić pierścień leczniczy.",
				false);

		prepareHintNPCs();
		new ApothecaryStage(apothecary, QUEST_SLOT).addToWorld();
		new ZoologistStage(zoologist, QUEST_SLOT).addToWorld();
		new RingMakerStage(ringmaker, QUEST_SLOT).addToWorld();
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
