package games.stendhal.server.maps.quests.janosik;

import java.util.LinkedList;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;

public class RAQuestHelperFunctions implements IRAQuestConstants {

	private static LinkedList<Creature> zbojnicy = new LinkedList<Creature>();

	public static final String MAIN_NPC_NAME = "Gazda Wojtek";

	public static SpeakerNPC getMainNPC() {
		return SingletonRepository.getNPCList().get(MAIN_NPC_NAME);
	}

	/**
	 * function for calculating reward's moneys for player
	 *
	 * @param player
	 * 			- player which must be rewarded
	 * @return
	 * 			gold amount for hunting rats.
	 */
	public static int calculateReward(Player player) {
		int moneys = 0;
		int kills = 0;
		for(int i=0; i<MONSTER_TYPES.size(); i++) {
			try {
				final String killed = player.getQuest(QUEST_SLOT,i+1);
				// have player quest slot or not yet?
				if (killed != null) {
					kills=Integer.decode(killed);
				}
			} catch (NumberFormatException nfe) {
				// player's quest slot don't contain valid number
				// so he didn't killed such creatures.
			}
			moneys = moneys + kills*MONSTER_REWARDS.get(i);
		}
		return(moneys);
	}

	public void setMonsters(LinkedList<Creature> monsters) {
		RAQuestHelperFunctions.zbojnicy = monsters;
	}

	public static LinkedList<Creature> getMonsters() {
		return zbojnicy;
	}

	/**
	 * Get the amount of rats.
	 *
	 * @return rat count
	 */
	public static int getMonstersCount() {
		return(getMonsters().size());
	}

	public static void setupKnight(SpeakerNPC npc) {
		npc.setEntityClass("recruiter3npc");
		npc.initHP(1000);
		npc.setResistance(0);
		npc.setVisibility(100);
		npc.setAllowToActAlone(true);
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new GreetingMatchesNameCondition(npc.getName()), true,
				ConversationStates.IDLE,
				"Witaj, jestem trochę zajęty wraz z mym zakonem pozbywaniem się najazdu rozbójników!",
				null);
		npc.addEmotionReply("hugs", "smile to");
	}

	/**
	 * return random rat from allowed list
	 * @return a random rat creature
	 */
	public static Creature getRandomMonster() {
		// Gaussian distribution
		int tc=Rand.randGaussian(0,MONSTER_TYPES.size());
		if ((tc>(MONSTER_TYPES.size()-1)) || (tc<0)) {
			tc=0;
		}
		final Creature tempCreature =
			new Creature((Creature) SingletonRepository.getEntityManager().getEntity(MONSTER_TYPES.get(tc)));
		return tempCreature;
	}
}
