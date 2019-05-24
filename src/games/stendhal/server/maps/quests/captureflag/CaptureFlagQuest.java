/***************************************************************************
 *                    (C) Copyright 2011 - Faiumoni e. V.                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.captureflag;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * sets the capture the flag quest up.
 *
 * @author hendrik, sjtsp
 */
public class CaptureFlagQuest extends AbstractQuest {
	
	/** name for the internal slot to store quest data */
	private static final String SLOT_NAME = "capture_the_flag";
	
	/** player visible name for the quest */
	private static final String QUEST_NAME = "CaptureTheFlag";
	
	private StendhalRPZone zone = null;

	@Override
	public String getSlotName() {
		return SLOT_NAME;
	}

	@Override
	public List<String> getHistory(Player player) {
		return new LinkedList<String>();
	}

	@Override
	public String getName() {
		return QUEST_NAME;
	}

	private void addTeamManagerNPC() {
		final SpeakerNPC npc = new SpeakerNPC("Thumb") {

			@Override
			protected void createPath() {
				// don't move
			}

			@Override
			protected void createDialog() {

				addGreeting("Cześć. Dziękuję w testach w Zdobycie Flagi (ZF).  Możesz #zagrć, #zatrzymać, zarządać #flagi i #strzał.");

				addJob("Pomagamy w testowaniu pomysłów, aby ZF było lepsze.");
				
				addHelp("Możesz sprawdzić ZF z innymi wojownikami.  Gdy jeden z was da tobie #flagę do ręki.  Inni otrzymają strzały pogrzebania (i łuk), a do wstawiania flagi używa się lewego przycisku.  Zauważ, że atakowanie nie działa - jedyne co musisz robić to naciskać tylko lewy przycisk.");
				
				// TODO: count the number of *full* matches that player has participated in
				add(ConversationStates.ATTENDING,
					Arrays.asList("play", "zagrać", "zagraj"),
					new NotCondition(new PlayingCTFCondition()),
					ConversationStates.ATTENDING,
					"Teraz inni wojownicy mogą cię oznaczyć o ile masz flagę, a oni specjalne strzały.  Miłej zabawy.  Daj znać jeżeli będziesz potrzebował #pomocy",
					new JoinCaptureFlagAction());
				add(ConversationStates.ATTENDING,
					Arrays.asList("play", "zagrać", "zagraj"),
					new PlayingCTFCondition(),
					ConversationStates.ATTENDING,
					"Już grasz.",
					null);
				
				add(ConversationStates.ATTENDING,
					Arrays.asList("stop", "zatrzymaj"),
					new PlayingCTFCondition(),
					ConversationStates.ATTENDING,
					"Dziękuję za grę.  Wróć ponownie.  Wyślij nam opinie",
					new LeaveCaptureFlagAction());
				add(ConversationStates.ATTENDING,
						Arrays.asList("stop", "zatrzymaj"),
						new NotCondition(new PlayingCTFCondition()),
						ConversationStates.ATTENDING,
						"Nie grasz teraz.",
						null);

				add(ConversationStates.ATTENDING,
					Arrays.asList("flag", "flagi", "flaga"),
					new PlayingCTFCondition(),
					ConversationStates.ATTENDING,
					"Oto ona.",
					new ProvideCTFFlagsAction());
				add(ConversationStates.ATTENDING,
						Arrays.asList("flag", "flagi", "flaga"),
						new NotCondition(new PlayingCTFCondition()),
						ConversationStates.ATTENDING,
						"Musisz #zagrać, aby otrzymać testową flagę.",
						null);
				
				// TODO: just use a compound action for all types of ammo
				add(ConversationStates.ATTENDING,
					Arrays.asList("snowballs", "śnieżki"),
					new PlayingCTFCondition(),
					ConversationStates.ATTENDING,
					"Oto ona.  Teraz wszystkie strzały wyglądają tak samo.  Musisz teraz na nie spojrzeć, aby zobaczyć jakich używasz.",
					new EquipItemAction("fumble arrow", 100));
				add(ConversationStates.ATTENDING,
						Arrays.asList("snowballs", "śnieżki"),
						new NotCondition(new PlayingCTFCondition()),
						ConversationStates.ATTENDING,
						"Musisz #zagrać, aby zdobyć więcej strzał.",
						null);

				// TODO: just use a compound action for all types of ammo
				add(ConversationStates.ATTENDING,
					Arrays.asList("snowballs", "śnieżki"),
					new PlayingCTFCondition(),
					ConversationStates.ATTENDING,
					"Oto ona.  Wszystkie śnieżki wyglądają tak samo.  usisz teraz na nie spojrzeć, aby zobaczyć jakich używasz.",
					new EquipItemAction("śnieżka pogrzebania", 100));
				add(ConversationStates.ATTENDING,
						Arrays.asList("snowballs", "śnieżki"),
						new NotCondition(new PlayingCTFCondition()),
						ConversationStates.ATTENDING,
						"Musisz #zagrać, aby zdobyć więcej strzał.",
						null);
				
				// TODO: remove from game, remove all ctf gear, ...
				// TODO: the cleanup needs to happen even if player logs out, or walks away (different code path)
				addGoodbye();
			}
		};

		npc.setEntityClass("oldheronpc"); // TODO: different sprite
		npc.setPosition(100, 119);
		npc.initHP(100);
		npc.setDescription("Oto Thumb"); // TODO: Describe NPC
		zone.add(npc);
	}

	@Override
	public void addToWorld() {
		zone = SingletonRepository.getRPWorld().getZone("0_semos_mountain_n2");
		addTeamManagerNPC();
	}

	@Override
	public String getNPCName() {
		return "Thumb";
	}

}
