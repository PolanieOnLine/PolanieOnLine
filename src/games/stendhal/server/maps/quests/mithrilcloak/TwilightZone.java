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
package games.stendhal.server.maps.quests.mithrilcloak;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;


/**
 * @author kymara
*/

class TwilightZone {

	private MithrilCloakQuestInfo mithrilcloak;
	
	private final NPCList npcs = SingletonRepository.getNPCList();

	public TwilightZone(final MithrilCloakQuestInfo mithrilcloak) {
		this.mithrilcloak = mithrilcloak;
	}


	private void getMossStep() {

		// Careful not to overlap with quest states in RainbowBeans quest

		final int MOSS_COST = 3000;

		final SpeakerNPC npc = npcs.get("Pdiddi");

		// offer moss when prompted
		npc.add(ConversationStates.ANY,
				Arrays.asList("moss", "magical", "twilight", "ida", "cloak", "mithril cloak", "specials", "twilight moss"),
				new QuestInStateCondition(mithrilcloak.getQuestSlot(), "twilight_zone"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Zachowasz to dla siebie! Tak mam mech. Każdy kosztuje "
				+ Integer.toString(MOSS_COST) + " money. Ile potrzebujesz?",
				null);

		// responding to question of how many they want, with a number
		npc.addMatching(ConversationStates.QUEST_ITEM_QUESTION,
				// match for all numbers as trigger expression
				ExpressionType.NUMERAL, new JokerExprMatcher(),
				new TextHasNumberCondition(1, 5000),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {

                        final int required = (sentence.getNumeral().getAmount());
						if (player.drop("money" , required * MOSS_COST)) {
							npc.say("Dobrze. Oto " + Integer.toString(required) + " mroczny mech. Nie bierz go zbyt dużo na raz.");
							new EquipItemAction("mroczny mech", required, true).fire(player, sentence, npc);
						} else {
							npc.say("Dobrze. Zapytaj mnie znowu, gdy będziesz miał pieniądze.");
						}
					}
				});

		// they don't want moss yet
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				Arrays.asList("no", "none", "nothing", "nie", "nic"),
				null,
				ConversationStates.ATTENDING,
				"Dobrze. Cokolowiek chcesz.",
				null);
	}

	private void twilightZoneStep() {

		final SpeakerNPC npc = npcs.get("Ida");
		// player hasn't given elixir to lda in the twilight zone yet
		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("magical", "mithril", "cloak","mech","mroczny mech", "mithril cloak", "task", "quest", "twilight", "zadanie", "misja"),
				new QuestInStateCondition(mithrilcloak.getQuestSlot(), "twilight_zone"),
				ConversationStates.ATTENDING,
				"Co mi się dzieje? Mam gorączkę .. Widzę mrok .. nie zrozumiesz tego dopóki nie odwiedzisz mnie tutaj ... musisz się zapytać #Pdiddi jak dostać się do #twilight.",				
				null);

		npc.addReply("Pdiddi","Och jestem zawstydzona... Nic więcej nie mogę Ci o nim powiedzieć...");


		// player gave elixir and returned
		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("magical", "mithril", "cloak", "mithril cloak", "task", "quest", "twilight", "elixir", "zadanie", "misja"),
				new QuestInStateCondition(mithrilcloak.getQuestSlot(), "taking_striped_cloak"),
				ConversationStates.ATTENDING,
				"Gdy byłam chora to byłam do tyłu z pracą. Obiecałam #Josephine, że zrobię jej płaszcz w paski, ale nie mam czasu. Proszę. Mam nadzieję, że kupisz jeden i zaniesiesz jej. Prążkowany płaszcz lazurowy sprzedają w Ados abandoned keep. Dziękuję!",				
				null);


		// Ida and lda look the same but one lives in her true home sewing room and one lives in the twilight zone 
		// hence they need different names according to engine, but name will look the same on client
		final SpeakerNPC npc2 = npcs.get("lda");

		npc2.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc2.getName()),
						new QuestInStateCondition(mithrilcloak.getQuestSlot(), "twilight_zone"),
						new NotCondition(new PlayerHasItemWithHimCondition("eliksir mroku"))),
				ConversationStates.IDLE,
				"Jestem chora... tak chora... tylko silne lekarstwo pomoże mi stanąć na nogi.",
				null);

		npc2.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc2.getName()),
						new QuestInStateCondition(mithrilcloak.getQuestSlot(), "twilight_zone"),
						new PlayerHasItemWithHimCondition("eliksir mroku")),
				ConversationStates.QUEST_ITEM_QUESTION,		
				"Czy to lekarstwo jest dla mnie? Jeżeli #tak to wezmę je natychmiast. Musisz ponownie wrócić i zobaczyć mnie gdy jestem zdrowa.",
				 null);

		npc2.add(ConversationStates.QUEST_ITEM_QUESTION, 
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "twilight_zone"),
								 new PlayerHasItemWithHimCondition("eliksir mroku")
								 ),
				ConversationStates.IDLE,
				"Dziękuję!",
				new MultipleActions(
								new DropItemAction("eliksir mroku"),
								new SetQuestAction(mithrilcloak.getQuestSlot(), "taking_striped_cloak"),
								new TeleportAction("int_ados_sewing_room", 12, 20, Direction.DOWN)
								)
				);

		npc2.add(ConversationStates.QUEST_ITEM_QUESTION, 
				ConversationPhrases.NO_MESSAGES,
				new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "twilight_zone"),
								 new PlayerHasItemWithHimCondition("eliksir mroku")
								 ),
				ConversationStates.IDLE,
				"Jestem coraz bardziej chora...",
				 null);
	}

	public void addToWorld() {
		getMossStep();
		twilightZoneStep();
	}

}
