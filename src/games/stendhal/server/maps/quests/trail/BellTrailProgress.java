/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

final class BellTrailProgress {
	static final String CAVE = "4_kikareukin_cave";
	static final String ISLANDS = "6_kikareukin_islands";
	static final String LEADER = "Witosław", WITNESS = "Niemir";
	static final String SCHOLAR = "Borzęta", INJURED = "Milan";
	static final int[][] ROUTE = { { 109, 28 }, { 96, 16 }, { 76, 12 },
			{ 50, 12 } };

	enum Encounter {
		WARDEN(
				"strażnik milczącego dzwonu",
				110,
				32,
				Step.BELL_GUARD,
				"fight",
				"silence"),
		ECHO("echo strażnika", 78, 10, Step.BELL_ESCAPE, "ambush", "escort;1");

		final String name, state, next;
		final int x, y;
		final Step step;

		Encounter(String name, int x, int y, Step step, String state,
				String next) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.step = step;
			this.state = state;
			this.next = next;
		}
	}

	private BellTrailProgress() {
	}

	static boolean active(Player player, Step step) {
		return player != null && player.hasQuest(step.slot)
				&& !player.isQuestCompleted(step.slot)
				&& player.isQuestCompleted(step.previousSlot());
	}

	static boolean advance(Player player, Step step, String before,
			String after) {
		if (!active(player, step)
				|| !player.isQuestInState(step.slot, before)) {
			return false;
		}
		player.setQuest(step.slot, after);
		return true;
	}

	static boolean mark(Player player, Step step, String phase, int bit,
			int required, String next) {
		if (!active(player, step)
				|| !player.isQuestInState(step.slot, 0, phase)) {
			return false;
		}
		int before = counter(player, step), after = before | bit;
		if (before == after) {
			return false;
		}
		player.setQuest(step.slot,
				after == required ? next : phase + ";" + after);
		return true;
	}

	static int counter(Player player, Step step) {
		try {
			return Integer.parseInt(player.getQuest(step.slot, 1));
		} catch (NumberFormatException exception) {
			return 0;
		}
	}

	static boolean needs(Player player, Encounter role) {
		return active(player, role.step)
				&& player.isQuestInState(role.step.slot, 0, role.state);
	}

	static void recordKill(Player player, RPEntity victim) {
		if (!(victim instanceof BellTrailGuard)) {
			return;
		}
		BellTrailGuard guard = (BellTrailGuard) victim;
		Encounter role = guard.role;
		CreatureRespawnPoint source = guard.getRespawnPoint();
		if (!needs(player, role) || source == null || victim.getZone() == null
				|| player.getZone() != victim.getZone()
				|| !ISLANDS.equals(victim.getZone().getName())
				|| !player.nextTo(victim, 16)
				|| source.getZone() != victim.getZone()
				|| source.getX() != role.x || source.getY() != role.y) {
			return;
		}
		player.setQuest(role.step.slot, role.next);
		player.sendPrivateText(role == Encounter.WARDEN
				? "Strażnik upadł. Teraz możesz poluzować mocowanie dzwonu i uciszyć go na dobre."
				: "Przy kładce znów jest cicho. Wróć po rannych i prowadź ich dalej na zachód.");
	}

	static String objective(Player player, Step step) {
		String state = player.getQuest(step.slot, 0);
		switch (step) {
		case BELL_RETURNED:
			return "witness".equals(state)
					? "Porozmawiaj z Niemirem obok Festris na piętrze 4 Kikareukin (110,76). Powiedz: wyprawa."
					: "Wróć do Festris na piętrze 4 Kikareukin. Powiedz: zaginiona wyprawa.";
		case BELL_WOUNDED:
			if ("report".equals(state)) {
				return "Opowiedz Witosławowi w obozie na północno-wschodniej wyspie Kikareukin, co usłyszeli ranni. Powiedz: ranni.";
			}
			int heard = counter(player, step);
			String witnesses = heard == 1 ? "z Milanem" : heard == 2
					? "z Borzętą" : "z Borzętą i Milanem";
			return "Porozmawiaj " + witnesses
					+ " przy skrzyniach w obozie na północno-wschodniej wyspie Kikareukin. Powiedz: ranni.";
		case BELL_VOICE:
			return "report".equals(state)
					? "Pokaż Borzęcie zapiski i opowiedz o pustym dzwonie. Wyspy Kikareukin (111,26). Powiedz: głos z mgły."
					: "Sprawdź skrzynię z zapiskami (104,24), kamień z napisem (112,30) i stary dzwon (117,34) na wyspach Kikareukin.";
		case BELL_GUARD:
			if ("wards".equals(state)) {
				return "Rozłącz dwa kamienie wiążące dzwon na wyspach Kikareukin: (108,32) i (116,30).";
			}
			if ("fight".equals(state)) {
				return "Pokonaj strażnika milczącego dzwonu na wyspach Kikareukin (110,32).";
			}
			return "silence".equals(state)
					? "Poluzuj mocowanie starego dzwonu na wyspach Kikareukin (117,34)."
					: "Wróć do Witosława w obozie na wyspach Kikareukin (108,26). Powiedz: milczący dzwon.";
		case BELL_ESCAPE:
			if ("return".equals(state)) {
				return "Ranni dotarli do zachodniego obozowiska. Wróć do Festris na piętrze 4 Kikareukin. Powiedz: powrót wyprawy.";
			}
			if ("ambush".equals(state)) {
				return "Ranni czekają przy pierwszym postoju. Pokonaj echo strażnika przy kładce na wyspach Kikareukin (78,10), potem wróć po nich.";
			}
			int checkpoint = Math.min(2, Math.max(0, counter(player, step)));
			int[] next = ROUTE[checkpoint + 1];
			return "Zbierz rannych przy Witosławie (109,26), jeśli nie idą za tobą. Prowadź ich powoli do następnego postoju ("
					+ next[0] + "," + next[1]
					+ ") na wyspach Kikareukin i sprawdź palenisko. Muszą dotrzeć obaj.";
		default:
			throw new IllegalArgumentException("Not a bell story quest");
		}
	}

	static List<String> history(Player player, Step step) {
		List<String> result = new ArrayList<>();
		result.add(step.description);
		if (player.isQuestCompleted(step.slot)) {
			switch (step) {
			case BELL_RETURNED:
				result.add(
						"Niemir słyszał głos zmarłej matki. Dowódca wciąż szuka brata przy dzwonie.");
				break;
			case BELL_WOUNDED:
				result.add(
						"Borzęta i Milan usłyszeli różne głosy. Obaj są zbyt osłabieni, żeby zejść bez osłony.");
				break;
			case BELL_VOICE:
				result.add(
						"Bronik zginął przed przybyciem wyprawy. Pusty dzwon powtarza głosy z pamięci słuchaczy.");
				break;
			case BELL_GUARD:
				result.add(
						"Kamienie są rozłączone, strażnik pokonany, a dzwon zdjęty z mocowania. Głosy ucichły.");
				break;
			case BELL_ESCAPE:
				result.add(
						"Obaj ranni dotarli do obozowiska. Festris otrzymała wiadomość o ocalonych i o losie Bronika.");
				break;
			default:
				break;
			}
		} else if (player.hasQuest(step.slot)) {
			result.add("Pozostało: " + objective(player, step));
		}
		return result;
	}
}
