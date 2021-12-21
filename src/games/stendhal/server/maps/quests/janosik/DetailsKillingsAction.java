package games.stendhal.server.maps.quests.janosik;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

public class DetailsKillingsAction implements ChatAction, IRAQuestConstants {
	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser mayor) {
		if (RAQuestHelperFunctions.calculateReward(player)==0) {
			mayor.say("Nie zabiłeś żadnego rozbójnika podczas napadu #zbójników. "+
					  "Aby odebrać #nagrodę musisz zabić conajmniej "+
					  "jednego zbója.");
			return;
		}
		final StringBuilder sb = new StringBuilder("Cóż, od ostatniej nagrody zabiłeś ");
		long moneys = 0;
		int kills = 0;
		for(int i=0; i<MONSTER_TYPES.size(); i++) {
			try {
				kills=Integer.parseInt(player.getQuest(QUEST_SLOT,i+1));
			} catch (NumberFormatException nfe) {
				// Have no records about this creature in player's slot.
				// Treat it as he never killed this creature.
				kills=0;
			}
			// must add 'and' word before last creature in list
			if(i==(MONSTER_TYPES.size()-1)) {
				sb.append("i ");
			}

			sb.append(Grammar.quantityplnoun(kills, MONSTER_TYPES.get(i)));
			sb.append(", ");
			moneys = moneys + kills*MONSTER_REWARDS.get(i);
		}
		sb.append("więc dam Tobie ");
		sb.append(moneys);
		sb.append(" money jako #nagrodę za ciężką pracę.");
		mayor.say(sb.toString());
	}


}
