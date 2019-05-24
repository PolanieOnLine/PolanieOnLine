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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: Speak with Hayunn 
 * <p>
 * PARTICIPANTS: <ul><li> Hayunn Naratha</ul>
 *
 * STEPS: <ul>
 * <li> Talk to Hayunn to activate the quest.
 * <li> He asks you to kill a rat, also offering to teach you how
 * <li> Return and learn how to loot, identify items and heal
 * <li> Return and learn how to double click move, and get some URLs
 * </ul>
 *
 * REWARD: <ul><li> 20 XP <li> 5 gold coins <li> studded shield </ul>
 *
 * REPETITIONS: <ul><li> Get the URLs as much as wanted but you only get the reward once.</ul>
 */
public class MeeteD extends AbstractQuest {

	private static final String QUEST_SLOT = "meet_eD";

	//This is 1 minute at 300 ms per turn
	private static final int TIME_OUT = 200;


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
		res.add("FIRST_CHAT");
		if (isCompleted(player)) {
			res.add("DONE");
		}
		return res;
	}

	private void prepareeFuR() {
		final SpeakerNPC npc = npcs.get("eFuR");

		// player wants to learn how to attack
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"Pilnuję tajemnic najstarszego z klanów, klanu eDragons i udzielam o nim informacji. Czy chciałbyś dowiedzieć się więcej? #Tak?",
				null);

		//player doesn't want to learn how to attack
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"Pewnie dużo o nas słyszałeś, jesteśmy najlepsi!",
				null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(10));

		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"), new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.INFORMATION_1,
				"Opowiem Ci najpierw o Radzie Klanu. #Ok?",
				new MultipleActions(actions));

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_2,
			"Aktualnie klanem zarządza #Furionka, #chemilie i #Castaris. Mogę Ci teraz opowiedzieć czym się zajmujemy?? #Tak?",
			null);

		npc.add(
			ConversationStates.INFORMATION_2,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_3,
			"Aktualnie zajmujemy się pracą nad rozwojem gry. W wolnym czasie pomagamy sobie w każdej sprawie, pilnujemy porządku w grze i świetnie się bawimy. Chciałbyś wiedzieć, jakie stawiamy wymagania przed członkami?? #Tak?",
			null);

		npc.add(
			ConversationStates.INFORMATION_3,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_4,
			"Aby stać się jednym z nas, musisz mieć dobrą opinię w grze, być komunikatywnym i mieć level przynajmniej 70, lecz robimy wyjątki, jeżeli dany gracz nas zainteresuje. Chcesz się dowiedzieć, jakich przestrzegamy zasad?? #Tak?",
			null);

		npc.add(
			ConversationStates.INFORMATION_4,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_5,
			"Najważniejsza jest lojalność. Jeżeli chcesz zapoznać się bardziej szczegółowo z naszym regulaminem, wejdź na #http://www.edragons-klan.yoyo.pl/infopage.php?id=5 . Zostań członkiem naszej grupy! Zgłoszenia przyjmuje #chemilie . A przystąpienie do klanu niesie z sobą same korzyści. Chcesz posłuchać o nich?? #Tak?",
			null);

		npc.add(
			ConversationStates.INFORMATION_5,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_6,
			"Zysków jest pełno. Najważniejszym z nich jest przyjaźń wśród członków i dobra zabawa. Pragniesz się dowiedzieć więcej? #Tak?",
			null);

		final String epilog = "Wejdz na stronę klanu - #www.edragons-klan.yoyo.pl i dowiedz się więcej! Zaglądaj do galerii, czytaj najnowsze aktualności. Baw się dobrze!";
		
			//This is used if the player returns, asks for #help and then say #yes
			npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES, new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			epilog + "Nasz klan jest najlepszy!! Zostań z nami!!",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseXPAction(1000));
		reward.add(new IncreaseKarmaAction(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.INFORMATION_6,
				ConversationPhrases.YES_MESSAGES, new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE, 
				epilog + "Dziękuję, że wysłuchałeś tego, co miałem do powiedzenia. Baw się dobrze!!",
				new MultipleActions(reward));

		npc.add(new ConversationStates[] { ConversationStates.ATTENDING,
					ConversationStates.INFORMATION_1,
					ConversationStates.INFORMATION_2,
					ConversationStates.INFORMATION_3,
					ConversationStates.INFORMATION_4,
					ConversationStates.INFORMATION_5,
					ConversationStates.INFORMATION_6},
				ConversationPhrases.NO_MESSAGES, new NotCondition(new QuestInStateCondition(QUEST_SLOT, "start")), ConversationStates.IDLE,
				"Zapraszam do klanu. Może Ci się poszczęści i dołączysz do nas? Póki co idę dalej pilnować siedziby, aby nikt niepowołany nie wtargnął w nasze progi.",
				null);

		npc.setPlayerChatTimeout(TIME_OUT); 
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie eFuRa",
				"eFuR zaprasza do klanu eDragons.",
				false);
		prepareeFuR();
	}

	@Override
	public String getName() {
		return "MeeteD";
	}
	@Override
	public String getNPCName() {
		return "eFuR";
	}
}
