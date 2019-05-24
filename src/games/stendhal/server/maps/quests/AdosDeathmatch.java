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

import games.stendhal.common.Direction;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasPetOrSheepCondition;
import games.stendhal.server.entity.npc.condition.PlayerInAreaCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.deathmatch.BailAction;
import games.stendhal.server.maps.deathmatch.DeathmatchInfo;
import games.stendhal.server.maps.deathmatch.DoneAction;
import games.stendhal.server.maps.deathmatch.LeaveAction;
import games.stendhal.server.maps.deathmatch.StartAction;
import games.stendhal.server.util.Area;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Creates the Ados Deathmatch Game.
 */
public class AdosDeathmatch extends AbstractQuest {

		/** the logger instance. */
	private static final Logger logger = Logger.getLogger(AdosDeathmatch.class);

	private StendhalRPZone zone;

	private static Area arena;

	private DeathmatchInfo deathmatchInfo;

	public AdosDeathmatch() {
	    // constructor for quest system
	    logger.debug("little constructor for quest system", new Throwable());
	}

	@Override
	public String getSlotName() {
		return "adosdeathmatch";
	}

	public AdosDeathmatch(final StendhalRPZone zone, final Area area) {
		this.zone = zone;
		arena = area;
		logger.debug("big constructor for zone", new Throwable());
		final Spot entrance = new Spot(zone, 96, 75);
		deathmatchInfo = new DeathmatchInfo(arena, zone, entrance);
		// do not let players scroll out of deathmatch
		Rectangle r = area.getShape().getBounds();
		zone.disallowOut(r.x, r.y, r.width, r.height);
	}

	/**
	 * Shows the player the potential trophy.
	 *
	 * @param x
	 *            x-position of helmet
	 * @param y
	 *            y-position of helmet
	 */
	public void createHelmet(final int x, final int y) {
		final Item helmet = SingletonRepository.getEntityManager()
				.getItem("zdobyczny hełm");
		helmet.setDescription("Oto główna nagroda dla zwycięzców Deathmatcha.");
		helmet.setPosition(x, y);
		zone.add(helmet, false);
	}

	/**
	 * Create the Deathmatch assistant.
	 * 
	 * @param name name of the assistant
	 * @param x x coordinate of the assistant
	 * @param y y coordinate of the assistant
	 */
	public void createNPC(final String name, final int x, final int y) {

		// We create an NPC
		final SpeakerNPC npc = new SpeakerNPC(name) {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {

				// player is outside the fence. after 'hi' use ConversationStates.INFORMATION_1 only.
				add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(name),
								new NotCondition(new PlayerInAreaCondition(arena))),
						ConversationStates.INFORMATION_1,
						"Witam na Deathmatchu w Ados! Porozmawiaj z #Thonatus jeżeli chcesz dołączyć",
						null);
				add(
						ConversationStates.INFORMATION_1,
						Arrays.asList("Thonatus", "Thonatusa"),
						null,
						ConversationStates.INFORMATION_1,
						"Thonatus rekrutuje do Deathmatcha. Znajdziesz go na #bagnach na południowy-zachód od Ados.",
						null);

                add(
					ConversationStates.INFORMATION_1,
					Arrays.asList("swamp", "bagnach"),
					null,
					ConversationStates.INFORMATION_1,
					"Tak jak powiedziałem, na południowym-zachodzie. Ale uważaj na bagnach kryją się potężne potwory.",
					null);


				add(
					ConversationStates.INFORMATION_1,
					"deathmatch",
					null,
					ConversationStates.INFORMATION_1,
					"Jeżeli zaakceptujesz #wyzwanie od #Thonatusa to przybędziesz tutaj. Wtedy silni przeciwnicy będą się pojawiać w koło Ciebie i musisz #zwyciężyć.",
					null);

                add(
                    ConversationStates.INFORMATION_1,
                    Arrays.asList("challenge", "wyzwanie"),
                    null,
                    ConversationStates.INFORMATION_1,
                    "Zapamiętaj słowo śmierć na Deathmatchu. Nie akceptuj wyzwania, póki nie będziesz pewien, że możesz się dobrze bronić. Aha i sprawdź czy nie ma tam już jakiegoś wojownika!",
                    null);

				add(
                    ConversationStates.INFORMATION_1,
                    Arrays.asList("victory", "zwyciężyć", "zwycięstwo"),
                    null,
                    ConversationStates.INFORMATION_1,
                    "Nagrodą za wygrany Deathmatch jest hełm, pokazany tutaj. Jeżeli za każdy razem wygrasz Deathmatch, to jego obrona będzie stopniowo wzrastać.",
                    null);

				// player is inside
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(name),
								new PlayerInAreaCondition(arena)),
						ConversationStates.ATTENDING,
						"Witam na Deathmatchu w Ados! Potrzebujesz #pomocy?", null);
				addJob("Jestem asystentem deathmatcha. Powiedz jeżeli będziesz potrzebował #pomocy.");
				addHelp("Powiedz '#start' kiedy będziesz gotowy! Zabijaj #wszystko co się #pojawi. Powiedz 'zwycięstwo' kiedy przeżyjesz.");
				addGoodbye("Mam nadzieję, że dobrze się bawiłeś na Deathmatchu!");

				add(
						ConversationStates.ATTENDING,
						Arrays.asList("everything", "appears", "deathmatch", "wszystko", "pojawi"),
						ConversationStates.ATTENDING,
						"W każdej rundzie staniesz twarzą w twarz z silniejszymi przeciwnikami. Broń się, zabij ich lub powiedz mi #poddaję się! Ale ostrzegam że jeśli zrezygnujesz to zapłacisz kaucję",
						null);
				add(
						ConversationStates.ATTENDING,
						Arrays.asList("trophy", "hełm", "helmet","zdobyczny hełm"),
						ConversationStates.ATTENDING,
						"Jeżeli wygrasz deathmatch to nagrodzimy Cię zdobycznym hełmem. Każde #zwycięstwo będzie go wzmacniać.",
						null);

				// 'start' command will start spawning creatures
				add(ConversationStates.ATTENDING, Arrays.asList("start", "go",
						"fight", "walka"), null, ConversationStates.IDLE, null,
						new StartAction(deathmatchInfo));

				// 'victory' command will scan, if all creatures are killed and
				// reward the player
				add(ConversationStates.ATTENDING, Arrays.asList("victory",
						"done", "yay", "zwycięstwo", "zrobione"), null, ConversationStates.ATTENDING,
						null, new DoneAction());

				// 'leave' command will send the victorious player home
				add(ConversationStates.ATTENDING, Arrays
						.asList("leave", "home", "wychodzę", "dom", "wyjdź"), null,
						ConversationStates.ATTENDING, null, new LeaveAction());

				// 'bail' command will teleport the player out of it
				add(ConversationStates.ANY, Arrays.asList("bail", "flee",
						"run", "exit", "wycofuję", "poddaję", "rezygnuję", "wycofać"), null, ConversationStates.ATTENDING,
						null, new BailAction());
			}
		};

		npc.setEntityClass("darkwizardnpc");
		npc.setPosition(x, y);
		npc.setDescription("Oto Thanatos. Obserwuje silnych wojowników na ich Deathmatchu.");
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		// The assistant is near the spikes, so give him better ears for the
		// safety of the players
		npc.setPerceptionRange(7);
		zone.add(npc);
	}


	static class DeathMatchEmptyCondition implements ChatCondition {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			final List<Player> dmplayers = arena.getPlayers();
			return dmplayers.size() == 0;
		}
	}

	private void recruiterInformation() {
		final SpeakerNPC npc2 = npcs.get("Thonatus");

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("heroes", "who", "hero", "status", "kto", "bohater"), 
				 new NotCondition(new DeathMatchEmptyCondition()), ConversationStates.ATTENDING,
				 null,
				 new ChatAction() {
					 @Override
					 public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						 final List<Player> dmplayers = arena.getPlayers();
						 final List<String> dmplayernames = new LinkedList<String>();
						 for (Player dmplayer : dmplayers) {
							 dmplayernames.add(dmplayer.getName());
						 }
						 // List the players inside deathmatch
						 npc.say("Teraz trwają walki na arenie deathmatcha. Jeżeli chciałbyś pójść i dołączyć do "
								 + Grammar.enumerateCollection(dmplayernames) + ", to powiedz #wyzwanie.");
					 }
				 });

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("heroes", "who", "hero", "status", "kto", "bohater") , new DeathMatchEmptyCondition(), 
				 ConversationStates.ATTENDING,
				 "Jesteś takim bohaterem? Mogę Cię zabrać na takie #wyzwanie", null);

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "wyzwaznie"), 
				 new AndCondition(new LevelGreaterThanCondition(19), 
						  new DeathMatchEmptyCondition(),
						  new NotCondition(new PlayerHasPetOrSheepCondition())), 
				 ConversationStates.IDLE, null,				 
				 new TeleportAction("0_ados_wall_n", 100, 86, Direction.DOWN));


		npc2.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "wyzwaznie"), 
			 new AndCondition(new LevelGreaterThanCondition(19), 
					  new NotCondition(new DeathMatchEmptyCondition()),
					  new NotCondition(new PlayerHasPetOrSheepCondition())), 
				 ConversationStates.QUESTION_1, null,				 
				 new ChatAction() {
					 @Override
					 public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						 final List<Player> dmplayers = arena.getPlayers();
						 final List<String> dmplayernames = new LinkedList<String>();
						 for (Player dmplayer : dmplayers) {
							 dmplayernames.add(dmplayer.getName());
						 }
						 // List the players inside deathmatch
						 npc.say("Teraz trwają walki na arenie deathmatcha. Jeżeli chciałbyś pójść i dołączyć do "
								 + Grammar.enumerateCollection(dmplayernames) + "?");
					 }
				 });

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "wyzwaznie"), 
			 new AndCondition(new LevelGreaterThanCondition(19), 
					  new PlayerHasPetOrSheepCondition()),
			 ConversationStates.ATTENDING, "Przepraszam, ale to byłoby zbyt straszne dla twojego zwierzaka tam.",
				 null);


		npc2.add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES, null,
				 ConversationStates.IDLE, null,				 
				 new TeleportAction("0_ados_wall_n", 100, 86, Direction.DOWN));


		npc2.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null,
				 ConversationStates.ATTENDING, "Jesteś nieco bojaźliwy, ale nie szkodzi. Jeśli coś jeszcze chcesz, wystarczy powiedzieć.",				 
				 null);

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "wyzwaznie"),
				 new LevelLessThanCondition(20), 
				 ConversationStates.ATTENDING, "Przepraszam, ale jesteś zbyt słaby na #Deathmatch, wróć co najmniej na poziomie 20.",
				 null);
	}



	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Deathmatch Ados",
				"Thanatos szuka bohaterów do walki na arenie Deathmatcha.",
				true);
		recruiterInformation();
	}
	@Override
	public String getName() {
		return "AdosDeathmatch";
	}
	@Override
	public int getMinLevel() {
		return 20;
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
	public String getRegion() {
		return Region.ADOS_CITY;
	}
	
	@Override
	public String getNPCName() {
		return "Thonatus";
	}
}
