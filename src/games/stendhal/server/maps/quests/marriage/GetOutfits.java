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
package games.stendhal.server.maps.quests.marriage;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;

class GetOutfits {
	private final MarriageQuestInfo marriage;
	
	private final NPCList npcs = SingletonRepository.getNPCList();
	
	public GetOutfits(final MarriageQuestInfo marriage) {
		this.marriage = marriage;
	}
	
	private void getOutfitsStep() {
		SpeakerNPC tam = npcs.get("Tamara");
		tam.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(tam.getName()),
						new ChatCondition() {
							@Override
							public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
								return marriage.isEngaged(player);
							}
						}), 
				ConversationStates.ATTENDING,
				"Witaj! Jeśli jesteś przyszłą Panną Młodą mogę ci #pomóc przygotować się do ślubu",
				null);
		tam.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(tam.getName()),
						new ChatCondition() {
							@Override
							public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
								return !marriage.isEngaged(player);
							}
						}),
				ConversationStates.IDLE,
				"Wybacz, nie mogę ci pomóc, bo jestem zajęta przygotowaniem innej przyszłej Panny Młodej!",
				null);

		SpeakerNPC tim = npcs.get("Timothy");
		tim.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(tim.getName()),
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
							return marriage.isEngaged(player);
						}
					}),
				ConversationStates.ATTENDING,
				"Dzień dobry! Jeśli jesteś przyszłym Panem Młodym mogę ci chętnie #pomóc przygotować się do ślubu.",
				null);
		tim.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(tim.getName()),
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
							return !marriage.isEngaged(player);
						}
					}),
				ConversationStates.IDLE,
				"Wybacz, nie mogę ci pomóc, bo kroję na miarę inny garnitur.",
				null);
	}

	public void addToWorld() {
		getOutfitsStep();
	}
}
