/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
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
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.JailAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NakedCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Speak with Ketteh
 * 
 * PARTICIPANTS: - Ketteh Wehoh, a woman 
 * 
 * STEPS: - Talk to Ketteh to activate the quest and keep speaking with Ketteh.
 * 
 * REWARD: - No XP - No money
 * 
 * REPETITIONS: - As much as wanted.
 */
public class MeetKetteh extends AbstractQuest {
	private static final String QUEST_SLOT = "Ketteh";
	private static final int GRACE_PERIOD = 5;
	private static final int JAIL_TIME = 10;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step1() {
		final SpeakerNPC npc = npcs.get("Ketteh Wehoh");
		
		// force Ketteh to notice naked players that she has already warned
		// but leave a 5 minute (or GRACE_PERIOD) gap if she only just warned them
		npc.addInitChatMessage(
				new AndCondition(
						new NakedCondition(),
						new OrCondition(
								new AndCondition(
										new QuestInStateCondition(QUEST_SLOT, 0,"seen_naked"),
										new TimePassedCondition(QUEST_SLOT,1,GRACE_PERIOD)),
						        new QuestInStateCondition(QUEST_SLOT,"seen"),
						        new QuestInStateCondition(QUEST_SLOT,"learnt_manners"),
						        // done was an old state that was used when naked but then clothed, 
						        // but they should do learnt_manners too
						        new QuestInStateCondition(QUEST_SLOT,"done"))),
				new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
		});

		// player is naked but may not have been warned recently, warn them and stamp the quest slot
		// this can be initiated by the npc as above
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NakedCondition(),
						new QuestNotInStateCondition(QUEST_SLOT, 0,"seen_naked")), 
				ConversationStates.ATTENDING,
				"Kim jesteś? Aiii!!! Jesteś nagi! Szybko nciśnij prawy przycisk na sobie i wybierz USTAW WYGLĄD! Jeżeli tego nie zrobisz to zawołam straże.",
				new MultipleActions(
						new SetQuestAction(QUEST_SLOT,0, "seen_naked"), 
						new SetQuestToTimeStampAction(QUEST_SLOT,1)));

		// player is naked and has been warned,
		// they started another conversation or the init chat message prompted this interaction as above
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NakedCondition(),
						new QuestInStateCondition(QUEST_SLOT, 0, "seen_naked")), 
				ConversationStates.ATTENDING,
				// this message doesn't get seen by the player himself as he gets sent to jail, but it would explain to bystanders why he is gone
				"Łee WCIĄŻ nie założyłeś na siebie ubrań. Idź do więzienia!",
				// Jail the player
				new MultipleActions(
						new SetQuestAction(QUEST_SLOT,0, "seen_naked"), 
						new SetQuestToTimeStampAction(QUEST_SLOT,1), 
						new JailAction(JAIL_TIME,"Ketteh Wehoh jailed you for being naked in the town hall after warning")));
		
		// player was previously seen naked but is now clothed
		// continue the quest to learn manners
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(new NakedCondition()), 
						new QuestInStateCondition(QUEST_SLOT, 0,"seen_naked")),
				ConversationStates.ATTENDING, 
				null,
				new MultipleActions(
						new SayTextAction("Witaj ponownie [name]. Tak się cieszę, że masz na sobie jakieś ubrania. Teraz możemy kontynuować lekcję o #manierach. Czy wiesz, że jak ktoś coś powie na niebiesko to grzecznie jest to powtórzyć? Powtórz za mną: #maniery."),
						new SetQuestAction(QUEST_SLOT, "seen")));

		// normal situation: player is clothed and meets Ketteh for the first time.
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(new NakedCondition()), 
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING, 
				null,
				new MultipleActions(
						new SayTextAction("Witaj [name] miło cię poznać. Mamy coś wspólnego - dobre maniery . Czy wiedziałeś, że jeżeli ktoś powie słowo na #niebiesko to grzecznie będzie powtórzyć je danej osobie. Powtarzaj za mną: #maniery."),
						new SetQuestAction(QUEST_SLOT, "seen")));
		
		// player has finished the quest by learning manners or was marked as done in an old state
		// also, they are still clothed
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(new NakedCondition()), 
						new OrCondition(new QuestInStateCondition(QUEST_SLOT, "learnt_manners"),new QuestInStateCondition(QUEST_SLOT, "done"))),
				ConversationStates.ATTENDING, 
				null,
				new SayTextAction("Witaj ponownie [name]."));
		
		// player had started the quest but didn't finish it and are still clothed
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(new NakedCondition()), 
						new QuestInStateCondition(QUEST_SLOT, "seen")),
				ConversationStates.ATTENDING, 
				null,
				new SayTextAction("Witaj ponownie [name]. Mam nadzieję, że jesteś tutaj, aby kontynuować lekcję o #manierach."));
		
		// player refuses to put clothes on (or just says No while naked)
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES, new NakedCondition(),
				ConversationStates.IDLE,
				"Jeżeli nie założysz jakiś ubrań na siebie i wyjdziesz to zacznę wołać straże!",
				null);

		// only allow quest completed if not naked
		npc.add(ConversationStates.ATTENDING, Arrays.asList("manners", "manier", "maniery", "manierach"), 
				new NotCondition(new NakedCondition()),
				ConversationStates.ATTENDING, 
				"Jeżeli zdarzy Ci się porozmawiać z jakimś innym mieszkańcem to zawsze powinieneś zacząć rozmowę od powiedzenia \"cześć\". Ludzie tutaj są całkiem przewidywalni i zawsze lubią rozmawiać na temat \"praca\". Odpowiedzą jeżeli zapytasz o \"pomoc\" i jeżeli chcesz wykonać \"zadanie\" dla nich to powiedz to im. Jeżeli wyglądają jak handlarze to możesz ich zapytać jaka jest ich \"oferta\". Aby zakończyć rozmowę powiedz \"dowidzenia\".",
				new SetQuestAction(QUEST_SLOT, "learnt_manners"));
		
		// not prompted to say this any more when naked, but just in case we don't want them to have an empty reply
		npc.add(ConversationStates.ATTENDING, "manners", 
				new NakedCondition(),
				ConversationStates.ATTENDING, 
				"Dobre maniery zaczynają się od założenia ubrań na siebie! Możesz dostać zaawansowaną lekcję, gdy będziesz w pełni ubrany.",
				null);
		
		// this is just because the blue highlighting was used as a demonstration
		npc.add(ConversationStates.ATTENDING, "blue", 
				null,
				ConversationStates.ATTENDING, 
				"Och nie jesteś zbyt mądry!",
				null);
	}
	


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie Ketteh Wehoh",
				"Elegancka kobieta siedząca przed w ratuszu w Semos i dbająca o nowych mieszkańców Faiumoni, aby nie trzęśli się z zimna. Jest lokalną Strażniczką Dobrych Obyczajów i Manier.",
				false);
		step1();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Ketteh Wehoh w ratuszu w Semos.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("seen_naked".equals(questState)) {
			res.add("Była w szoku i krzyczał na mnie, że nie powinienem biegać nago. Lepiej założe jakieś ubrania nim mnie znów zobaczy.");
		}
		if ("seen".equals(questState)) {
			res.add("Zaoferowała mi jak się zachować, ale muszę wrócić i dokończyć lekcję.");
		}
		if ("seen".equals(questState) || "done".equals(questState) || "learnt_manners".equals(questState)) {
			res.add("Ona jest bardzo uprzejma i opowiedziała mi o właściwym zachowaniu w Faiumoni.");
		}
        if (isCompleted(player)) {
            res.add("Mogę spotkać się i porozmawiać z nią ponownie, w każdej chwili. Lepiej jak będę się dobrze zachowywać!");
		}
		return res;
	}

	@Override
	public String getName() {
		return "MeetKetteh";
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new OrCondition(
            new QuestInStateCondition(QUEST_SLOT,"learnt_manners"),
            new QuestInStateCondition(QUEST_SLOT,"done")).fire(player, null, null);
	}

    @Override
    public boolean isRepeatable(final Player player) {
        return true;
    }

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ketteh Wehoh";
	}
}
