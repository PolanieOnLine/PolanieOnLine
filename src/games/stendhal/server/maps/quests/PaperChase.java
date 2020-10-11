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

import org.apache.log4j.Logger;

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
import games.stendhal.server.util.QuestUtils;

/**
 * A kind of paper chase.
 *
 * @author hendrik
 */
public class PaperChase extends AbstractQuest implements TeleportListener {
	private static final Logger logger = Logger.getLogger(PaperChase.class);
	private static final String QUEST_SLOT = "paper_chase_20[year]";

	private static final int TELEPORT_PENALTY_IN_MINUTES = 10;

	private static final List<String> NPC_IDLE = Arrays.asList();

	private List<String> points = Arrays.asList("Fiete", "Marcellus", "Marianne", "Ermenegilda", "Pierre", "Julia", "Christina", "Fidorea");

	private Map<String, String> texts = new HashMap<String, String>();

	private Map<String, String> greetings = new HashMap<String, String>();

	private LoadSignFromHallOfFameAction loadSignFromHallOfFame;

	private void setupGreetings() {
		greetings.put("Fiete", "Ładnie! W końcu dotarłeś! Ptaki powiedziały mi wieki temu.");
		greetings.put("Marcellus", "Dzięki za odwiedzenie mnie na moim stanowisku.");
		greetings.put("Marianne", "Oh cześć. Miło cię poznać. Chciałabym poprosić o jajka od tych przerażających kurczaków.");
		greetings.put("Ermenegilda", "Och, nie bój się. Mogę cię #uleczyć, jeśli chcesz.");
		greetings.put("Pierre", "Zastanów się, czy chcesz kontynuować podróż w uroczym stroju niedźwiedzia.");
		greetings.put("Julia", "Jeśli zechcesz to odpocznij sobie od podróży i ciesz się jedną lub dwiema książkami.");
		greetings.put("Christina", "Naprawdę żałowałam, że nie udało mi się zapewnić jedzenia dla podróżnika takiego jak ty.");
	}

	private void setupTexts() {
		texts.put("Fiete", "Następna osoba, którą powinieneś znaleźć, jest odpowiedzialna za usługi portowe na południe od Deniran.");
		texts.put("Marcellus", "Następna osoba pilnuje mostu na wschód stąd.");
		texts.put("Marianne", "Następną osobą, którą powinieneś znaleźć, jest mała dziewczynka, która boi się kurczaczka.");
		texts.put("Ermenegilda", "Widzę, że spieszysz się na spotkanie z następną osobą na liście: ze straszną starą panią na rynku. Spokojnie, jest miła i uleczy potrzebujących.");
		texts.put("Pierre", "Następna osoba to znany kostiumolog.");
		texts.put("Julia", "Jestem pewien, że następna osoba uwielbia to. Jest bibliotekarką, ale całkiem uroczą.");
		texts.put("Christina", "Jeśli książka nie daje ci nieco odpoczynku, być może jakiś chleb. Piekarnia będzie następnym przystankiem w Twojej podróży.");
		texts.put("Fidorea", "Ostatnia osoba, z którą powinieneś porozmawiać, to ta, która to wszystko zaczęła. I jestem pewien, że dostaniesz tam dużo jedzenia.");
	}

	private String getFameType() {
		String questSlot = QuestUtils.evaluateQuestSlotName(QUEST_SLOT);
		return questSlot.substring(questSlot.length() - 1);
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
			raiser.say(greetings.get(state) + " " + texts.get(next) + " Powodzenia!");
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
		if (npc == null) {
			logger.error("NPC " + state + " missing for paper chase");
		}
		npc.add(ConversationStates.ATTENDING, Arrays.asList("paper", "chase", "paperchase"), new SystemPropertyCondition("stendhal.minetown"),
				ConversationStates.ATTENDING, null, new PaperChasePoint(idx));
		if (NPC_IDLE.contains(state)) {
			npc.add(ConversationStates.ANY, Arrays.asList("paper", "chase", "paperchase"), new SystemPropertyCondition("stendhal.minetown"),
					ConversationStates.ANY, null, new PaperChasePoint(idx));
		}
	}


	private void createHallOfFameSign() {
		loadSignFromHallOfFame = new LoadSignFromHallOfFameAction(null, "Ci, którzy podróżowali po świecie w imieniu Fidorei:\n", getFameType(), 2000, true);
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
			"Musisz zapytać się każdej osoby po drodze o #paper #chase. Twoja podróż rozpoczyna się na południe od Deniran w porcie. Osoba, której szukasz, jest odpowiedzialna za usługi portowe. "
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
			new SetHallOfFameToAgeDiffAction(QUEST_SLOT, 1, getFameType()),
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
