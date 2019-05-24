package games.stendhal.server.entity.npc.action;

import java.util.Arrays;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.behaviour.impl.RepairerBehaviour;
import games.stendhal.server.entity.player.Player;

/**
 * Behaviour action for repairing npcs
 *
 * @author madmetzger
 */
@Dev(category=Category.IGNORE)
public class RepairingBehaviourAction extends AbstractBehaviourAction<RepairerBehaviour>{

	public RepairingBehaviourAction(RepairerBehaviour repairerBehaviour) {
		super(repairerBehaviour, Arrays.asList("repair", "napraw"), "repair");
	}

	@Override
	public void fireRequestOK(ItemParserResult res, Player player,
			Sentence sentence, EventRaiser npc) {
		String chosen = res.getChosenItemName();
		if(behaviour.canDealWith(chosen)) {
			npc.say("naprawa będzie kosztować x money");
		} else {
			npc.say("Przykro mi, ale nie mogę naprawić " + res.getChosenItemName() + ".");
		}
	}

	@Override
	public void fireRequestError(ItemParserResult res, Player player,
			Sentence sentence, EventRaiser npc) {
		npc.say("Nie rozumiem ciebie.");
	}

}