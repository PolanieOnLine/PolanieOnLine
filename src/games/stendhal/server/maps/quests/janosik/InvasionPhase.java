/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.janosik;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

public class InvasionPhase extends RAQuest {
	private final int minPhaseChangeTime;
	private final int maxPhaseChangeTime;
	protected LinkedList<Creature> monsters = new LinkedList<Creature>();

	private void addConversations(final SpeakerNPC mainNPC) {
		RA_Phase myphase = INVASION;

		// Player asking about rats at invasion time.
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("zbójnicy", "zbójników", "zbójników!"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						npc.say("Wciąż jest " + Grammar.isare(RAQuestHelperFunctions.getMonsters().size()) +
								" około "+Integer.toString(RAQuestHelperFunctions.getMonsters().size())+
								" żywych zbójników.");
					}
				});

		//Player asked about details at invasion time.
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("details", "szczegóły"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"Zakopane jest zaatakowane przez zbójników! "
				+ "Nie chcę cię teraz nagrodzić czy "
				+ "wyjaśniać szczegóły Tobie "
				+ "póki banda zbójników nie zostanie wygnana z krainy.",
				null);

		// Player asked about reward at invasion time.
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("reward", "nagroda", "nagrodę"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"Zakopane jest zaatakowane przez zbójników! "
				+ "Nie chcę cię teraz nagrodzić "
				+ "póki banda zbójników nie zostanie wygnana z krainy.",
				null);
	}

	/**
	 * Create InvasionPhase.
	 *
	 * @param timings
	 */
	public InvasionPhase(Map<String, Integer> timings) {
		super(timings);
		minPhaseChangeTime = timings.get(INVASION_TIME_MIN);
		maxPhaseChangeTime = timings.get(INVASION_TIME_MAX);
		this.monsters=RAQuestHelperFunctions.getMonsters();
		addConversations(RAQuestHelperFunctions.getMainNPC());
	}

	@Override
	public int getMinTimeOut() {
		return minPhaseChangeTime;
	}

	@Override
	public int getMaxTimeOut() {
		return maxPhaseChangeTime;
	}

	/**
	 * rats invasion starts :-)
	 * Iterate through each zone and select the min and max rat count based on zone size
	 * Places rat if possible, if not skip this rat (so if 6 rats chosen perhaps only 3 are placed)
	 */
	private void summonMonsters() {

		final MonstersObserver monstersObserver = new MonstersObserver();

		// generating rats in zones
		for(int j=0; j<(MONSTER_ZONES.size()); j++) {
			final StendhalRPZone zone = (StendhalRPZone) SingletonRepository.getRPWorld().getRPZone(
					MONSTER_ZONES.get(j));
			final int maxRats = (int) Math.round(Math.sqrt(zone.getWidth()*zone.getHeight())/4);
			final int minRats = (int) Math.round(Math.sqrt(zone.getWidth()*zone.getHeight())/12);
			final int ratsCount = Rand.rand(maxRats-minRats)+minRats;
			logger.debug(ratsCount+ " monsters selected at " + zone.getName());
			for(int i=0 ; i<ratsCount; i++) {
				final int x=Rand.rand(zone.getWidth());
				final int y=Rand.rand(zone.getHeight());
				final Creature tempCreature = RAQuestHelperFunctions.getRandomMonster();
				final Creature monster = new Creature(tempCreature.getNewInstance());

				// chosen place is occupied
				if (zone.collides(monster,x,y)) {
					// Could not place the creature here.
					// Treat it like it was never exists.
					logger.debug("MONSTERS " + zone.getName() + " " + x + " " + y + " collided.");
					continue;
				} else if (zone.getName().startsWith("0")) {
					// If we can't make it here, we can't make it anywhere ...
					// just checking the 0 level zones atm
					// the rat is not in the zone yet so we can't call the smaller version of the searchPath method
					final List<Node> path = Path.searchPath(zone, x, y, zone.getWidth()/2,
							zone.getHeight()/2, (64+64)*2);
					if (path == null || path.size() == 0){
						logger.debug("MONSTERS " + zone.getName() + " " + x + " " + y + " no path to " + zone.getWidth()/2 + " " + zone.getHeight()/2);
						continue;
					}
				}
				// spawn creature
				monster.registerObjectsForNotification(monstersObserver);
				/* -- commented because of these noises reflects on all archrats in game -- */
				// add unique noises to humanoids
				if (tempCreature.getName().equals("zbójnik górski herszt")) {
					final LinkedList<String> ll = new LinkedList<String>(
							Arrays.asList("Rabujcie co cennego!"));
					LinkedHashMap<String, LinkedList<String>> lhm =
						new LinkedHashMap<String, LinkedList<String>>();
					// add to all states except death.
					lhm.put("idle", ll);
					lhm.put("fight", ll);
					lhm.put("follow", ll);
					monster.setNoises(lhm);
				}

				StendhalRPAction.placeat(zone, monster, x, y);
				monsters.add(monster);
			}
		}
	}

	/**
	 * function to control amount of alive rats.
	 * @param dead
	 * 			- creature that was just died.
	 */
	private void notifyDead(final RPEntity dead) {
		if (!monsters.remove(dead)) {
			logger.warn("killed creature isn't in control list ("+dead.toString()+").");
		}
		if (monsters.size()==0) {
			phaseToDefaultPhase(
					new LinkedList<String>(Arrays.asList("janosik")));
		}
    }

	/**
	 *  Rats are dead :-)
	 */

	@Override
	public String getSwitchingToDefPhaseMessage() {
		final String text = "Gazda Wojtek krzyczy: żaden #zbójnik w podhalu nie przetrwał, "+
							"zostali tylko ci, którzy od zawsze chowali się w "+
							"kopalniach. "+
							"Zapraszam bohaterów, aby ich #wynagrodzić.";
		return(text);
	}

	/**
	 *  Rats now living under all buildings. Need to call Pied Piper :-)
	 */
	@Override
	public String getSwitchingToNextPhaseMessage() {
		final String text =
			"Gazda Wojtek krzyczy: Banda #zbójników nagle opanowała miasto. "+
			"Będę musiał wezwać rycerzy z Zakonu Cienia. "+
			"Swoją drogą dziękuję wszystkim, którzy pomogli oczyścić Zakopane i "+
			"zapraszam Was do odebrania #nagrody.";
		return(text);
	}

	/**
	 * removing rats from the world
	 */
	private void removeAllMonsters() {
		final int sz=monsters.size();
		int i=0;
		while(monsters.size()!=0) {
			try {
			final Creature monster = monsters.get(0);
			monster.stopAttack();
			monster.clearDropItemList();
			monster.getZone().remove(monster);
			monsters.remove(0);
			i++;
			} catch (IndexOutOfBoundsException ioobe) {
				// index is greater then size???
				logger.error("removeAllMonsters IndexOutOfBoundException at "+
						Integer.toString(i)+" position. Total "+
						Integer.toString(sz)+" elements.", ioobe);
			}
		}
	}

	/**
	 * Red alert! Rats in the Ados city!
	 *
	 * @return Ados mayor's call for help message
	 */
	protected String monstersProblem() {
		final String text = "Gazda Wojtek krzyczy: Miasto Zakopane oraz okolice tatr zostały napadnięte przez bandę #zbójników!"+
			              " Każdy kto pomoże oczyścić miasto zostanie nagrodzony!";
		return(text);
	}

	@Override
	public void prepare() {
		summonMonsters();
		super.startShouts(timings.get(SHOUT_TIME), monstersProblem());
	}

	@Override
	public void phaseToDefaultPhase(List<String> comments) {
		comments.add("last monster killed");
		super.phaseToDefaultPhase(comments);
	}


	@Override
	public void phaseToNextPhase(IRAQuest nextPhase, List<String> comments) {
		comments.add("switch phase, "+monsters.size()+" monsters still alive.");
		removeAllMonsters();
		super.phaseToNextPhase(nextPhase, comments);
	}

    /**
     *  Implementation of Observer interface.
     *  Update function will record the fact of rat's killing
     *  in player's quest slot.
     */
	class MonstersObserver implements Observer {
		@Override
		public void update (Observable obj, Object arg) {
	        if (arg instanceof CircumstancesOfDeath) {
	    		final CircumstancesOfDeath circs=(CircumstancesOfDeath)arg;
	        	if(MONSTER_ZONES.contains(circs.getZone().getName())) {
	        	if(circs.getKiller() instanceof Player) {
	        		final Player player = (Player) circs.getKiller();
	        		killsRecorder(player, circs.getVictim());
	        	}
	        	notifyDead(circs.getVictim());
	        	}
	        }
	    }
	}

	/**
	 *  method for making records about killing rats
	 *  in player's quest slot.
	 *
	 *  @param player
	 *  			- player which killed rat.
	 *  @param victim
	 *  			- rat object
	 */
	private void killsRecorder(Player player, final RPEntity victim) {

		final String str = victim.getName();
		final int i = MONSTER_TYPES.indexOf(str);
		if(i==-1) {
			//no such creature in reward table, will not count it
			logger.warn("Unknown creature killed: "+
					    victim.getName());
			return;
		}

		if((player.getQuest(QUEST_SLOT)==null)||
		   (player.getQuest(QUEST_SLOT).equals("done")||
		   (player.getQuest(QUEST_SLOT).equals("")))){
			// player just killed his first creature.
		    player.setQuest(QUEST_SLOT, "monsters;0;0;0;0;0;0;0;0");
		}

		// we using here and after "i+1" because player's quest index 0
		// is occupied by quest stage description.
		if ("".equals(player.getQuest(QUEST_SLOT,i+1))){
			// something really wrong, will correct this...
			player.setQuest(QUEST_SLOT,"monsters;0;0;0;0;0;0;0;0");
		}
		int kills;
		try {
			kills = Integer.parseInt(player.getQuest(QUEST_SLOT, i+1))+1;
		} catch (NumberFormatException nfe) {
			// have no records about this creature in player's slot.
			// treat it as he never killed this creature before.
			kills=1;
		}
		player.setQuest(QUEST_SLOT, i+1, Integer.toString(kills));
	}

	@Override
	public RA_Phase getPhase() {
		return RA_Phase.RA_INVASION;
	}
}
