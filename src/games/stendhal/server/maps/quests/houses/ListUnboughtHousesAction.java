/**
 * 
 */
package games.stendhal.server.maps.quests.houses;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

import java.util.List;

final class ListUnboughtHousesAction implements ChatAction {
	private final String location;

	/**
	 * Creates a new ListUnboughtHousesAction.
	 * 
	 * @param location
	 *            where are the houses?
	 */
	ListUnboughtHousesAction(final String location) {
		this.location = location;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final List<String> unbought = HouseUtilities.getUnboughtHousesInLocation(location);
		if (unbought.size() > 0) {
			raiser.say("Sprawdzam moje zapiski, ale " + Grammar.enumerateCollection(unbought) + " wciąż można #kupić.");
		} else {
			raiser.say("Nie ma domów na sprzedaż w " + Grammar.makeUpperCaseWord(location) + ".");
		}
	}
}
