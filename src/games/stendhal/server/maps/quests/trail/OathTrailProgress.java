/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * Persistent, personal evidence and ceremonial steps; scenery remains shared.
 */
final class OathTrailProgress {
	static final String NORTH = "0_krakow_n_w2", WEST = "0_krakow_w2",
			SENTRY = "Wszebor";

	enum Encounter {
		WATCH("poległy wartownik", NORTH, 73, 38, Step.OATH_WATCH, "witness"),
		BANNER(
				"strażnik dawnej chorągwi",
				WEST,
				98,
				85,
				Step.OATH_BANNER,
				"banner");

		final String name, zone, next;
		final int x, y;
		final Step step;

		Encounter(String name, String zone, int x, int y, Step step,
				String next) {
			this.name = name;
			this.zone = zone;
			this.x = x;
			this.y = y;
			this.step = step;
			this.next = next;
		}
	}

	private OathTrailProgress() {
	}

	static boolean active(Player player, Step step) {
		return player != null && player.hasQuest(step.slot)
				&& !player.isQuestCompleted(step.slot)
				&& player.isQuestCompleted(step.previousSlot());
	}

	static boolean needs(Player player, Encounter role) {
		return active(player, role.step)
				&& player.isQuestInState(role.step.slot, "fight");
	}

	static boolean released(Player player) {
		return player.isQuestCompleted(Step.OATH_BURIAL.slot)
				|| (active(player, Step.OATH_BURIAL) && player.isQuestInState(
						Step.OATH_BURIAL.slot, "ready"));
	}

	static int mask(Player player) {
		String state = player.getQuest(Step.OATH_NAMES.slot, 0);
		if ("done".equals(state) || "report".equals(state))
			return 3;
		String bit = player.getQuest(Step.OATH_NAMES.slot, 1);
		return "1".equals(bit) ? 1 : "2".equals(bit) ? 2 : 0;
	}

	static boolean mark(Player player, int bit) {
		if (!active(player, Step.OATH_NAMES)
				|| !player.isQuestInState(Step.OATH_NAMES.slot, 0, "clues"))
			return false;
		int before = mask(player), after = before | bit;
		if (before == after)
			return false;
		player.setQuest(Step.OATH_NAMES.slot,
				after == 3 ? "report" : "clues;" + after);
		return true;
	}

	static boolean advance(Player player, Step step, String before,
			String after) {
		if (!active(player, step) || !player.isQuestInState(step.slot, before))
			return false;
		player.setQuest(step.slot, after);
		return true;
	}

	static void recordKill(Player player, RPEntity victim) {
		if (!(victim instanceof OathTrailGuard))
			return;
		OathTrailGuard guard = (OathTrailGuard) victim;
		Encounter role = guard.role;
		if (!needs(player, role) || player.getZone() != victim.getZone()
				|| victim.getZone() == null
				|| !role.zone.equals(victim.getZone().getName())
				|| !player.nextTo(victim, 16))
			return;
		CreatureRespawnPoint source = guard.getRespawnPoint();
		if (source == null || source.getZone() != victim.getZone()
				|| source.getX() != role.x || source.getY() != role.y)
			return;
		player.setQuest(role.step.slot, role.next);
		player.sendPrivateText(role == Encounter.WATCH
				? "Ostrze przechodzi przez bladą postać. Wartownik opuszcza broń. Wszebor, stojący obok traktu, daje ci znak, żeby podejść."
				: "Strażnik chorągwi osuwa się na ziemię i znika. Płótno zostało przy dawnym posterunku. Możesz je teraz zabrać.");
	}

	static String objective(Player player, Step step) {
		String state = player.getQuest(step.slot, 0);
		switch (step) {
		case OATH_WATCH:
			if ("witness".equals(state))
				return "Porozmawiaj z Wszeborem na dawnym trakcie na północny zachód od Krakowa (70,38). Powiedz: polegli wartownicy.";
			if ("report".equals(state))
				return "Wróć do Wagabundusa na południe od Krakowa. Powiedz: polegli wartownicy.";
			return "Pokonaj poległego wartownika na północno-zachodnich polach pod Krakowem (73,38). Nie atakuje osób, które nie prowadzą tej sprawy.";
		case OATH_NAMES:
			if ("report".equals(state))
				return "Pokaż Amileuszowi w Krakowie odrysowane znaki. Powiedz: znak oddziału.";
			int seen = mask(player);
			return "Zbadaj " + ((seen & 1) == 0 ? "resztki tarczy (53,40)" : "")
					+ (seen == 0 ? " i " : "")
					+ ((seen & 2) == 0 ? "kamień z imionami (58,40)" : "")
					+ " na polach na północny zachód od Krakowa, niedaleko Wszebora.";
		case OATH_GRAVE:
			return "report".equals(state)
					? "Wróć do Wagabundusa z wieścią o mogile. Powiedz: samotny grób."
					: "Przyjrzyj się samotnemu grobowi na zachodnich polach pod Krakowem (92,34).";
		case OATH_TRUTH:
			return "report".equals(state)
					? "Zanieś odpis Amileuszowi. Powiedz: ostatni rozkaz."
					: "Przeszukaj resztki posterunku obok samotnego grobu, na zachodnich polach pod Krakowem (94,38).";
		case OATH_BANNER:
			if ("banner".equals(state))
				return "Zabierz chorągiew leżącą przy dawnym posterunku na zachodnich polach (100,85).";
			if ("report".equals(state))
				return "Wróć do Wszebora na północno-zachodnie pola. Powiedz: chorągiew oddziału.";
			return "Pokonaj strażnika dawnej chorągwi na zachodnich polach pod Krakowem (98,85).";
		case OATH_BURIAL:
			if ("remains".equals(state))
				return "Wróć do samotnego grobu dowódcy na zachodnich polach (92,34) i przygotuj szczątki do przeniesienia.";
			if ("burial".equals(state))
				return "1".equals(player.getQuest(step.slot, 1))
						? "Złóż chorągiew przy wspólnej mogile na północno-zachodnich polach (78,28)."
						: "Złóż szczątki dowódcy we wspólnej mogile na północno-zachodnich polach (76,28).";
			if ("release".equals(state))
				return "Porozmawiaj z Wszeborem o dotrzymanym słowie.";
			return "Wróć do Wagabundusa po podziękowanie. Zostaw miejsce w plecaku. Powiedz: wspólny pochówek.";
		default:
			throw new IllegalArgumentException("Not an oath quest");
		}
	}

	static List<String> history(Player player, Step step) {
		List<String> rows = new ArrayList<>();
		if (!player.hasQuest(step.slot))
			return rows;
		rows.add(step.description);
		String state = player.getQuest(step.slot, 0);
		boolean done = player.isQuestCompleted(step.slot);
		switch (step) {
		case OATH_WATCH:
			if (!"fight".equals(state))
				rows.add(
						"Po pojedynku wartownik przestał zastępować mi drogę.");
			if ("report".equals(state) || done)
				rows.add(
						"Wszebor czeka na dowódcę Borzywoja. Obiecano mu, że dowódca wróci po swoich ludzi.");
			break;
		case OATH_NAMES:
			int seen = mask(player);
			if ((seen & 1) != 0)
				rows.add(
						"Na tarczy został znak trzech nacięć pod grotem włóczni.");
			if ((seen & 2) != 0)
				rows.add(
						"Kamień wymienia imiona poległych. Przy imieniu Borzywoja dopisano oskarżenie o ucieczkę.");
			if (done)
				rows.add(
						"Amileusz rozpoznał oddział osłaniający dawną przeprawę. Borzywoja pochowano oddzielnie.");
			break;
		case OATH_GRAVE:
			if ("report".equals(state) || done)
				rows.add(
						"Pod napisem o zdradzie zachowały się słowa: pozostał przy przeprawie. W grobie leży tarcza tego samego oddziału.");
			break;
		case OATH_TRUTH:
			if ("report".equals(state) || done)
				rows.add(
						"Rozkaz kazał Borzywojowi utrzymać przeprawę do odejścia ostatniego wozu. Dowódca wykonał rozkaz, a nie uciekł.");
			break;
		case OATH_BANNER:
			if (!"fight".equals(state))
				rows.add("Strażnik chorągwi został pokonany.");
			if ("report".equals(state) || done)
				rows.add(
						"Chorągiew została odzyskana dla poległych. Nie jest przedmiotem przeznaczonym do sprzedaży.");
			break;
		case OATH_BURIAL:
			if (!"remains".equals(state))
				rows.add(
						"Szczątki Borzywoja zostały przygotowane do wspólnego pochówku.");
			if ("release".equals(state) || "ready".equals(state) || done)
				rows.add(
						"Dowódca spoczął ze swoimi ludźmi. Przy mogile stanęła chorągiew oddziału.");
			if ("ready".equals(state) || done)
				rows.add(
						"Wszebor pożegnał towarzyszy. Sam dobrowolnie pozostanie strażnikiem ich mogiły.");
			if (done)
				rows.add(
						"Wagabundus obiecał opowiadać prawdziwą historię Borzywoja i podarował mi odnowioną tarczę dowódcy.");
			break;
		default:
			throw new IllegalArgumentException("Not an oath quest");
		}
		return rows;
	}
}
