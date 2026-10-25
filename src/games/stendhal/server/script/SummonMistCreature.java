package games.stendhal.server.script;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.core.rule.creature.EliteCreatureService;
import games.stendhal.server.core.scripting.AbstractAdminScript;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.maps.AlternativeCreatures;

/** Summons one mist creature using the same transformation as authored mist maps. */
public final class SummonMistCreature extends AbstractAdminScript {
	@Override
	protected int getMinParams() { return 1; }

	@Override
	protected List<String> getParamStrings() {
		return Arrays.asList("<nazwa potwora> [elite=true] [factor=0.1]");
	}

	@Override
	protected List<String> getParamDetails() {
		return Arrays.asList("Potwór pojawia się obok administratora na bieżącej mapie.",
				"factor: dodatkowa siła wersji mgielnej; 0.1 oznacza +10% (zakres 0..10).",
				"elite=true: dodatkowo stosuje standardowe premie elitarnego potwora.");
	}

	@Override
	protected void run(final List<String> args) {
		if (admin == null || admin.getAdminLevel() < 35) {
			sendError("Nie masz uprawnień do przywoływania potworów.");
			return;
		}
		final List<String> nameParts = new ArrayList<String>();
		double factor = 0.1;
		boolean elite = false;
		try {
			for (final String arg : args) {
				if (arg.startsWith("factor=")) {
					factor = Double.parseDouble(arg.substring(7));
				} else if (arg.startsWith("elite=")) {
					final String value = arg.substring(6);
					if (!"true".equals(value) && !"false".equals(value)) {
						throw new IllegalArgumentException("elite musi mieć wartość true albo false.");
					}
					elite = Boolean.parseBoolean(value);
				} else if (arg.contains("=")) {
					throw new IllegalArgumentException("Nieznana opcja: " + arg);
				} else {
					nameParts.add(arg);
				}
			}
			if (!Double.isFinite(factor) || factor < 0 || factor > 10) {
				throw new IllegalArgumentException("factor musi być liczbą od 0 do 10.");
			}
		} catch (final IllegalArgumentException error) {
			sendError("Nieprawidłowe parametry: " + error.getMessage());
			return;
		}
		final String name = String.join(" ", nameParts).trim();
		final Creature prototype = name.isEmpty() ? null
				: SingletonRepository.getEntityManager().getCreature(name);
		if (prototype == null || prototype.isAbnormal()
				|| prototype.getName().startsWith("mgielny ")) {
			sendError("Podaj nazwę zwykłego potwora, np. dwugłowy smok.");
			return;
		}
		// Match normal respawns: templates may leave visibility/resistance at zero.
		final Creature creature = prototype.getNewInstance();
		AlternativeCreatures.applyMistVariant(creature, factor);
		if (elite && !EliteCreatureService.promote(creature)) {
			sendError("Ten potwór nie może otrzymać wersji elitarnej. Nic nie przywołano.");
			return;
		}
		if (admin.getZone() == null || !StendhalRPAction.placeat(admin.getZone(), creature,
				admin.getX(), admin.getY())) {
			sendError("Brak wolnego miejsca na przywołanie potwora.");
			return;
		}
		new GameEvent(admin.getName(), "summon_mist", name,
				Double.toString(factor), Boolean.toString(elite)).raise();
		sendInfo("Przywołano " + creature.getName() + (elite ? " (elitarny)" : "") + ".");
	}
}
