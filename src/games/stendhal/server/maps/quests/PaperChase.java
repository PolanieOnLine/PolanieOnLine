/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.events.TeleportListener;
import games.stendhal.server.core.events.TeleportNotifier;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.LoadSignFromHallOfFameAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetHallOfFameToAgeDiffAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToPlayerAgeAction;
import games.stendhal.server.entity.npc.action.SetQuestToYearAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.SystemPropertyCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * A kind of paper chase.
 *
 * @author hendrik
 */
public class PaperChase extends AbstractQuest implements TeleportListener {
	private static final String QUEST_SLOT = "paper_chase_20[year]";
	private static final String FAME_TYPE = QUEST_SLOT.substring(QUEST_SLOT.length() - 1);

	private static final int TELEPORT_PENALTY_IN_MINUTES = 10;

	private static final List<String> NPC_IDLE = Arrays.asList("Tad", "Haunchy Meatoch", "Pdiddi", "Ketteh Wehoh");

	private List<String> points = Arrays.asList("Nishiya", "Marcus", "Eheneumniranin", "Balduin", "Rachel", "Fritz",
												"Alice Farmer", "Elisabeth", "Sue", "Old Mother Helena", "Hazel",
												"Captain Brownbeard", "Jane", "Seremela", "Phalk", "Fidorea");

	private Map<String, String> texts = new HashMap<String, String>();

	private Map<String, String> greetings = new HashMap<String, String>();

	private LoadSignFromHallOfFameAction loadSignFromHallOfFame;

	private void setupGreetings() {
		// Each greeting is said by the previous NPC to point to the NPC in the key.
		greetings.put("Marcus", "Moje owce wiedziały, że jesteś w drodze do mnie. ");
		greetings.put("Eheneumniranin", "Dawno temu ktoś mnie tu odwiedził. Fajnie, że mnie znalazłeś. ");
		greetings.put("Balduin", "Ach, znalazłeś mnie podczas zbierania snopków zboża moim sierpem. Wspaniale! ");
		greetings.put("Rachel", "Wietrznie tutaj? Mam nadziej, że ostatnia wskazówka jak mnie znaleść nie była zbyt łatwa. ");
		greetings.put("Fritz", "Och, kocham klientów banku Ados! Oni są tacy słodcy! ");
		greetings.put("Alice Farmer", "Pachnie tu rybą, prawda? To jest duch oceanu! ");
		greetings.put("Elisabeth", "Fantastyczne wakacje do tej pory... i tyle do odkrycia! ");
		greetings.put("Sue", "Uwielbiam czekoladę! Znalazłeś mnie, może następnym razem przyniesiesz mi tabliczkę czekolady. ");
		greetings.put("Old Mother Helena", "Wszystkie te kwiaty wokół dają mi ciepłe uczucie. Mam nadzieję, że ci się podobają, dziękuję za odwiedzenie mnie! ");
		greetings.put("Hazel", "Och, cześć, tak miło, że mnie tu znalazłeś. Przyjdź i dołącz do mnie wkrótce, by pozwolić mi ugotować dla ciebie niezłą zupę. ");
		greetings.put("Captain Brownbeard", "Muzeum to naprawdę piękne miejsce do pracy. Cudownie, że mnie tu znalazłeś. ");
		greetings.put("Jane", "Yaaarrrr! Moja łódź zabierze cię nad morze, morze!  *śpiewanie* ");
		greetings.put("Seremela", "Na plaży jest gorąco, mam nadzieję, że użyłeś kremu do opalania. ");
		greetings.put("Phalk", "Piękne kwiaty w tym mieście! Niestety, te elfy nie doceniają ich zbytnio. ");
		greetings.put("Fidorea", "Młody wojowniku, zrobiłeś wspaniałe rzeczy w swojej podróży! Teraz wróć, aby zakończyć. Musisz być spragniony! ");
	}

	private void setupTexts() {
		texts.put("Marcus", "Następna osoba, którą powinieneś znaleźć, zajmuje się złodziejami i innymi przestępcami. "
				  + "Pracuje w forcie niedaleko Semos.");
		texts.put("Eheneumniranin", "Następnie musisz znaleźć półelfiego elfa na farmie Ados. Zawsze jest zajęty zbieraniem zboża.");
		texts.put("Balduin", "Następna osoba na twoje drodze siedzi na szczycie wietrznej góry.");
		texts.put("Rachel", "Następna dama, która pracuje w banku, może opowiedzieć ci o swojej pracy.");
		texts.put("Fritz", "Proszę, znajdź starego rybaka w Ados, który może opowiedzieć ci wspaniałe historie o rybach. Ma też córkę Caroline.");
		texts.put("Alice Farmer", "Następną osobą, której musisz szukać, są wakacje na Ados wraz z całą rodziną. Ona także wie wszystko o jedzeniu i napojach.");
		texts.put("Elisabeth", "Teraz musisz znaleźć młodą dziewczynę, która bawi się na placu zabaw w Kirdneh i uwielbia czekoladę.");
		texts.put("Sue", "Proszę, znajdź miłego ogrodnika, który jest właścicielem szklarni z pomidorami w pobliżu Kalavanu.");
		texts.put("Old Mother Helena", "Teraz idź i spróbuj znaleść miłą starszą pani, która jest bardzo sławna ze względu na swoje zupy, które są pożywne i smaczne.");
		texts.put("Hazel", "Znam naprawdę miłą kobietę, która może ci pomóc. Pracuje w muzeum i uwielbia swoją pracę.");
		texts.put("Captain Brownbeard", "Teraz musisz podróżować promem i porozmawiać ze starą solą, która zaprowadzi cię do następnej osoby, z którą się spotkasz.");
		texts.put("Jane", "Harrr yarrr kolejna dama lubi opalać się razem z mężem na plaży Athor.");
		texts.put("Seremela", "Nie tak dawno temu, następną osobę, którą musisz znaleźć, otworzyła piękny sklep z kwiatami. Widziałem wokół siebie wiele długonogich stworzeń, ukrytych w mieście leżącym w lesie.");
		texts.put("Phalk", "Następną osobą, którą musisz znaleźć, jest stary wojownik, który pilnuje kopalń, na północ do Samos.");
		texts.put("Fidorea", "Ostatnia osobą z którą musisz porozmawiać to ta, która to wszystko zaczęła.");
	}

	/**
	 * Handles all normal points in this paper chase (without the first and last.
	 * one)
	 */
	private class PaperChasePoint implements ChatAction {
		private final int idx;

		PaperChasePoint(final int idx) {
			this.idx = idx;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			final String state = points.get(idx);
			final String next = points.get(idx + 1);
			final String questState = player.getQuest(QUEST_SLOT, 0);

			// player does not have this quest or finished it
			if (questState == null) {
				raiser.say("Porozmawiaj z Fidorea w Mine Town, aby zacząć paper chase.");
				return;
			}

			final String nextNPC = questState;

			// is the player supposed to speak to another NPC?
			if (!nextNPC.equals(state)) {
				raiser.say("Co powiedziałeś? \"" + texts.get(nextNPC) + "\" To oczywiście nie ja, to pomyłka.");
				return;
			}

			// send player to the next NPC and record it in quest state
			raiser.say(greetings.get(next) + texts.get(next) + " Powodzenia!");
			player.setQuest(QUEST_SLOT, 0, next);
			player.addXP((idx + 1) * 10);
		}

	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	/**
	 * Adds the task to the specified NPC. Note that the start and end of this
	 * quest have to be coded specially.
	 *
	 * @param idx
	 *            index of way point
	 */
	private void addTaskToNPC(final int idx) {
		final String state = points.get(idx);
		final SpeakerNPC npc = npcs.get(state);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("paper", "chase", "paperchase"), new SystemPropertyCondition("stendhal.minetown"),
				ConversationStates.ATTENDING, null, new PaperChasePoint(idx));
		if (NPC_IDLE.contains(state)) {
			npc.add(ConversationStates.ANY, Arrays.asList("paper", "chase", "paperchase"), new SystemPropertyCondition("stendhal.minetown"),
					ConversationStates.ANY, null, new PaperChasePoint(idx));
		}
	}


	private void createHallOfFameSign() {
		loadSignFromHallOfFame = new LoadSignFromHallOfFameAction(null, "Ci, którzy podróżowali po świecie w imieniu Fidorei:\n", FAME_TYPE, 2000, true);
		loadSignFromHallOfFame.fire(null, null, null);
	}

	/**
	 * sets the sign to show the hall of fame
	 *
	 * @param sign a Sign or <code>null</code>.
	 */
	public void setSign(Sign sign) {
		loadSignFromHallOfFame.setSign(sign);
		loadSignFromHallOfFame.fire(null, null, null);
	}

	public void addToStarterNPCs() {
		SpeakerNPC npc = npcs.get("Fidorea");

		ChatAction startAction = new MultipleActions(
			new SetQuestAction(QUEST_SLOT, 0, points.get(0)),
			new SetQuestToPlayerAgeAction(QUEST_SLOT, 1),
			new SetQuestToYearAction(QUEST_SLOT, 2));

		// Fidorea introduces the quests
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING,
			"Nic nie mogę dla Ciebie zrobić. Dziękuję, że pytałeś.",
			null);
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.QUEST_OFFERED,
			"Ci, którzy zostali w domu ze względu na obowiązek przygotowania #paper #chase.",
			null);
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("paper", "chase"),
			new SystemPropertyCondition("stendhal.minetown"),
			ConversationStates.ATTENDING,
			"Musisz zapytać się każdej osoby po drodze o #paper #chase. Na początku musisz pójść do Semos Village, tam znajdziesz sprzedawcę owiec. "
			+ "Ostrzegam: możesz się teleportować podczas podróży, ale każdy teleport będzie kosztował dodatkowe " + TELEPORT_PENALTY_IN_MINUTES + " minut w punktacji.",
			startAction);


		// add normal way points (without first and last)
		for (int i = 0; i < points.size() - 1; i++) {
			addTaskToNPC(i);
		}

		// Fidorea does the post processing of this quest
		npc.add(ConversationStates.ATTENDING, Arrays.asList("paper", "chase"),
				new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING, "Oh, to dobre #zadanie.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("paper", "chase"),
			new AndCondition(
					new QuestStartedCondition(QUEST_SLOT),
					new QuestNotInStateCondition(QUEST_SLOT, 0, "Fidorea"),
					new QuestNotInStateCondition(QUEST_SLOT, 0, "done"),
					new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING, "Sądzę, że wciąż musisz pogadać z kilkoma osobami.", null);

		ChatAction reward = new MultipleActions(
			new IncreaseKarmaAction(15),
			new IncreaseXPAction(400),
			new SetQuestAction(QUEST_SLOT, 0, "done"),
			new EquipItemAction("niezapisany zwój", 5),
			new SetHallOfFameToAgeDiffAction(QUEST_SLOT, 1, FAME_TYPE),
			loadSignFromHallOfFame);

		npc.add(ConversationStates.ATTENDING, Arrays.asList("paper", "chase"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "Fidorea"), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING,
			"Bardzo dobrze. Ukończyłeś zadanie rozmawiając ze wszystkimi osobami na świecie. Zapiszę Twoje imię, aby każdy mógł je poznać. Tutaj w nagrodę kilka magicznych zwojów. Pomogą Tobie w podróżach.",
			reward);
	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Paper Chase",
				"Plotki głoszą, że odbywa się w okolicach Faiumoni. Może ktoś kto mieszka w pobliżu będzie coś wiedział.",
				false);
		setupGreetings();
		setupTexts();
		createHallOfFameSign();
		TeleportNotifier.get().registerListener(this);
	}


	@Override
	public String getName() {
		return "PaperChase";
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}


	@Override
	public void onTeleport(Player player, boolean playerAction) {
		if (!playerAction) {
			return;
		}

		if (player.hasQuest(QUEST_SLOT) && !player.getQuest(QUEST_SLOT, 0).equals("done")) {
			int startAgeWithPenalty = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT, 1), 0) - TELEPORT_PENALTY_IN_MINUTES;
			player.setQuest(QUEST_SLOT, 1, Integer.toString(startAgeWithPenalty));
		}
	}


	@Override
	public String getNPCName() {
		return "Fidorea";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}
