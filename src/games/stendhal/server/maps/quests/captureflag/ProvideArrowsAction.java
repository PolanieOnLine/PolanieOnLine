package games.stendhal.server.maps.quests.captureflag;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 * provide special CTF arrows (fumble and slowdown) to a player
 *
 * NOTE: i thought this should be separate, because we could check that
 *       player is able to have a flag (playing, ...).  but it's too
 *       late for that check by the time this is fired ...
 *       so i probably need to implement some predicate/condition
 *
 * @author sjtsp
 */
public class ProvideArrowsAction implements ChatAction {

	@Override
	public void fire(Player player, Sentence sentence, EventRaiser npc) {

		if (!CaptureFlagSupport.isPlaying(player)) {
			player.sendPrivateText("Najpierw dołącz do gry Capture the Flag.");
			return;
		}

		if (!(player.isEquipped("ctf bow") || player.isEquipped("łuk zf"))) {
			player.sendPrivateText("Potrzebujesz łuku ZF, aby korzystać ze specjalnych strzał.");
			return;
		}

		if (!CaptureFlagSupport.supplyIfMissing(player, sentence, npc, "strzała pogrzebania", 100)) {
			if (npc != null) {
				npc.say("Masz już strzały pogrzebania.");
			}
		}
	}
}
