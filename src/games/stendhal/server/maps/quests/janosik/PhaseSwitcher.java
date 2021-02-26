package games.stendhal.server.maps.quests.janosik;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import games.stendhal.server.maps.quests.Janosik;

/**
 * helper class for normal switching phase to next phase,
 * wrapper of observer around a function.
 *
 * @author yoriy
 */
public final class PhaseSwitcher implements Observer {

	private IRAQuest myphase;

	@Override
	public void update(Observable arg0, Object arg1) {
		myphase.phaseToNextPhase(
				Janosik.getNextPhaseClass(Janosik.getPhase()),
				Arrays.asList("normal switching"));
	}

	public PhaseSwitcher(IRAQuest phase) {
		myphase = phase;
	}

}
