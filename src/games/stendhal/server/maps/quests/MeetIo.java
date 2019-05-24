/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Speak with Io PARTICIPANTS: - Io
 * 
 * STEPS: - Talk to Io to activate the quest and keep speaking with Io.
 * 
 * REWARD: - 250 XP - 50 gold coins
 * 
 * REPETITIONS: - As much as wanted, but you only get the reward once.
 */
public class MeetIo extends AbstractQuest {

	private static final String QUEST_SLOT = "meet_io";



	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem telepatkę Io Flotto w świątyni Semos.");
		if (isCompleted(player)) {
			res.add("Io nauczyła mnie sześciu podstawowych zasad telepatii i przyrzekła przypomnieć mi jeżeli będę musiał odświeżyć moją wiedzę.");
		}
		return res;
	}

	private void prepareIO() {

		final SpeakerNPC npc = npcs.get("Io Flotto");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.HELP_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Jestem telepatką i telekinetką. Mogę Tobie pomóc dzieląc się z tobą moimi mentalnymi umiejętnościami. Czy chcesz, abym nauczyła Ciebie 6 podstawowych elementów telepatii? Ja już znam odpowiedź, ale chcę być kulturalna...",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.HELP_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Czy chcesz, abym powtórzyła sześć podstawowych elementów telepatii? Ja już znam odpowiedź, ale chcę być kulturalna...",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_1,
			"Wpisz #/who aby ustalić nazwy wojowników, którzy w danym momencie grają w PolskaGra. Czy chcesz się nauczyć drugiego elementu telepatii?",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_2,
			"Wpisz #/where #nazwawojownika aby dowiedzieć się gdzie dana osoba znajduje się w PolskaGra. Możesz też użyć #/where #owca aby śledzić własną owcę. Powinieneś porozmawawiać z #Zynn aby zrozumieć system używany do sprawdzania pozycji w Polska On Line. On wie więcej o nim niż ja. Gotowy na trzecią lekcję?",
			null);

		npc.add(
			ConversationStates.INFORMATION_2,
			"Zynn",
			null,
			ConversationStates.INFORMATION_2,
			"Jego pełne imię to Zynn Iwuhos. Spędza dużo czasu w bibliotece sporządzając mapy i pisząc historyczne książki. Gotowy na następną lekcję?",
			null);

		npc.add(
			ConversationStates.INFORMATION_2,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_3,
			"Wpisz #/tell #nazwawojownika #wiadomość lub #/msg #imięwojownika #wiadomość aby z kimś porozmawiać bez względu, gdzie dana osoba się znajduje.  Możesz wpisać #// #odpowiedź aby kontynuować rozmowę z osobą, której wysłałeś wiadomość. Gotowy na czwartą radę?",
			null);

		npc.add(
			ConversationStates.INFORMATION_3,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_4,
			"Naciśnij w tym samym czasie #Shift+Góra aby przywołać rzeczy, które wcześniej powiedziałeś, gdy chcesz się powtórzyć. Możesz też użyć #Ctrl+L jeżeli masz kłopoty. Dobrze możemy przejść do piątej lekcji?",
			null);

		npc.add(
			ConversationStates.INFORMATION_4,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_5,
			"Wpisz #/support #<wiadomość> aby zgłosić problem administratorowi, który się Tobie przydarzył. Możesz też spróbować IRCa. Jeżeli wciąż masz problemy to uruchom klienta IRC i wejdź na kanał ##arianne na serwerze #irc.freenode.net\n Dobrze czas na ostatnią lekcję na temat mentalnej manipulacji!",
			null);

		npc.add(
			ConversationStates.INFORMATION_5,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_6,
			"Możesz podróżować na płaszczyźnie astralnej o każdej porze zapisując i zamykając grę wpisując #/quit albo naciskając klawisz #Esc lub zamykając okno. Dobrze! Hmm, Sądzę, że chcesz się nauczyć jak latać w powietrzu jak ja.",
			null);

		/** Give the reward to the patient newcomer user */
		final String answer = "*ziewnięcie* Może pokaże Tobie później... Nie chcę Cię obciążyć zbyt wieloma informacjami naraz. O każdej porze możesz dostać streszczenie wszystkich tych lekcji wpisując #/help.\n";
		npc.add(ConversationStates.INFORMATION_6,
			ConversationPhrases.YES_MESSAGES, 
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.IDLE, 
			answer + "Hej! Wiem o czym myślisz i nie podoba mi się to!",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("money", 50));
		reward.add(new IncreaseXPAction(250));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));		

		npc.add(ConversationStates.INFORMATION_6,
			ConversationPhrases.YES_MESSAGES, 
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.IDLE, 
			answer + "Pamiętaj, aby nic nie rozproszyło twojej koncentracji.",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.ANY,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Jeżeli zdecydujesz się o poszerzeniu granic swojego umysłu to wpadnij do mnie. Na razie!",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie Io",
				"Io Flotto może nauczyć jak komunikować się.",
				false);
		prepareIO();
	}

	@Override
	public String getName() {
		return "MeetIo";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Io Flotto";
	}
}
