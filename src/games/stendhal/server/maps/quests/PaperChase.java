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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A kind of paper chase.
 *
 * @author hendrik
 */
public class PaperChase extends AbstractQuest implements TeleportListener {
	private static final String QUEST_SLOT = "paper_chase_2015";
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
		greetings.put("Adena", "Czekałem już na ciebie :) ");
		greetings.put("Valo", "Ahh już jesteś! Zgaduję, że piękny zapach moich warzyw przyprowadził cię tutaj. ");
		greetings.put("Balduin", "Oh znalazłeś drogę w tym pięknym starym kościele Ados. ");
		greetings.put("Gaston", "Wietrznie tutaj? Mam nadziej, że ostatnia wskazówka jak mnie znaleść nie była zbyt łatwa. ");
		greetings.put("Lobelia", "Bonjour! Widzę, że dotarłeś do mnie przez te ulice mojego rodzinnego miasta szczurów. ");
		greetings.put("Ortiv Milquetoast", "Czyż te kwiatki nie są piękne, podejdź i przyjrzyj się im dokładnie! ");
		greetings.put("Pam", "Uh, ah! Uff to ty. Masz mnie! ");
		greetings.put("Old Mother Helena", "Wiedziałam, że ktoś mnie znajdzie na plaży! ");
		greetings.put("Imorgen", "Oh witaj. Miło, że mnie tutaj znalazłeś. Wróć i przyłącz się kiedyś, aby spróbować mojej zupy. ");
		greetings.put("Anastasia", "*śpiewa* jakieś zioła do kocioła, trochę więcej i trochę więcej *śpiewa*... ");
		greetings.put("Vulcanus", "Ciii Hughie próbuje spać... ");
		greetings.put("Wrvil", "Czy wiedziałeś, że moje imię pochodzi z greki? ");
		greetings.put("Fidorea", "*Hau!* Znalazełeś mnie tutaj. Ładnie!");
	}
	

	private void setupTexts() {
		texts.put("Adena", "Następna osoba, którą powinieneś spotkać prowadzi swój własny interes. "
				  + "Sprzedaje świeże warzywa i jedzenie z farm, które są niedaleko Semos.");
		texts.put("Valo", "Następna osoba na twojej trasie żyje i pracuje w kościele. Może on sporządzić użyteczne mikstury dla ciebie.");
		texts.put("Balduin", "Następna osoba na twoje drodze siedzi na szczycie wietrznej góry.");
		texts.put("Gaston", "Następna osoba jest naprawdę dobrym piekarzem i może stworzyć dobre jedzenie z masła i czekolady. Oba jego imionai jedzenie, które tworzy są w obcym dla mnie języku.");
		texts.put("Lobelia", "Następna mistyczna osoba rozgląda się uważnie dookoła siebie i wie dużo o twoich przyszłych podróżach...");
		texts.put("Ortiv Milquetoast", "Będziesz potrzebował trochę opanowania ponieważ nasepna osoba naprawdę boi się Zabójców i Bandytów.");
		texts.put("Pam", "Następna dama kocha kąpiele słoneczne na specjalnej wyspied i jest dobrą przyjaciółką Zary."); 
		texts.put("Old Mother Helena", "Teraz idź i spróbuj znaleść miłą starszą pani, która jest bardzo sławna ze względu na swoje zupy, które są pożywne i smaczne.");
		texts.put("Imorgen", "Hobby następnej osoby, którą musisz znaleść jest śpiew i picie specjalnych mistycznych mikstur. Mieszka w otoczeniu drzew w małej chatce razem ze swoją babcią.");
		texts.put("Anastasia", "Proszę idź i poszukaj pani, która opiekuje się chorym synem. Mieszkają oni w malowniczej wsi.");
		texts.put("Vulcanus", "Teraz musisz znaleść syna boga, który przywita cię po grecku.");
		texts.put("Wrvil", "Teraz spotkaj się z mężem barmanki, która jest znana ze swoich mocnych napojów.");
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
				raiser.say("Co powiedziałeś? \"" + texts.get(nextNPC) + "\" To pomyłka.");
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
		loadSignFromHallOfFame = new LoadSignFromHallOfFameAction(null, "Ci którzy podróżowali po świecie zachowaniem Fidorea:\n", "P", 2000, true);
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
			"Musisz zapytać się każdej osoby po drodze o #paper #chase. Na początku musisz pójść Semos Road, aby odnaleść młodego chłopca, który ostrzega wojowników o niebezpieczeństwu w Faiumoni."
			+ "Ostrzegam: możesz się teleportować podczas podróży, ale każdy teleport będzie kosztował dodatkowe " + TELEPORT_PENALTY_IN_MINUTES + " minut w punktacji.",
			startAction);


		// add normal way points (without first and last)
		for (int i = 0; i < points.size() - 1; i++) {
			addTaskToNPC(i);
		}

		// Fidorea does the post processing of this quest
		npc.add(ConversationStates.ATTENDING, Arrays.asList("paper", "chase"), 
				new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING, "Oh to dobre #zadanie.", null);
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
			new SetHallOfFameToAgeDiffAction(QUEST_SLOT, 1, "P"),
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
