package games.stendhal.server.maps.quests.captureflag;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.captureflag.CaptureFlagSupport;



/**
 * throw two flags on the ground after a player has requested them
 *
 * NOTE: i thought this should be separate, because we could check that
 *       player is able to have a flag (playing, ...).  but it's too
 *       late for that check by the time this is fired ...
 *       so i probably need to implement some predicate/condition
 *
 * @author sjtsp
 */
public class ProvideCTFFlagsAction implements ChatAction {

	@Override
	public void fire(Player player, Sentence sentence, EventRaiser npc) {

		if (!CaptureFlagSupport.isPlaying(player)) {
			player.sendPrivateText("Najpierw dołącz do gry Capture the Flag.");
			return;
		}

		if (player.getNumberOfEquipped("flaga") > 0) {
			player.sendPrivateText("Masz już flagę w dłoni.");
			return;
		}

		// will put it in player's hand, or on ground
		new EquipItemAction("flaga").fire(player, sentence, npc);

	}
}
