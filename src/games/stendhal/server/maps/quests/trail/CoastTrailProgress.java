/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * Persistent, personal rescue clues and signal preparation on the shared coast.
 */
final class CoastTrailProgress {
	static final String WEST = "0_gdansk_city_nw", EAST = "0_gdansk_city_ne",
			CELLAR = "int_gdansk_storage_-1";
	static final String SAILOR = "Lubor", FERRYMAN = "Staszek",
			KNIGHT = "Malteis", BARMAID = "Rosa";

	enum Encounter {
		BEACH("rabuś wybrzeża", EAST, 108, 64, Step.COAST_LIGHTS, "report"),
		CHIEF(
				"herszt nadbrzeżnych rabusiów",
				CELLAR,
				28,
				26,
				Step.COAST_WRECKERS,
				"papers"),
		SABOTEUR(
				"niszczyciel sygnałów",
				EAST,
				56,
				63,
				Step.COAST_RETURN,
				"signal");

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

	private CoastTrailProgress() {
	}

	static boolean active(Player player, Step step) {
		return player != null && player.hasQuest(step.slot)
				&& !player.isQuestCompleted(step.slot)
				&& player.isQuestCompleted(step.previousSlot());
	}

	static boolean advance(Player player, Step step, String before,
			String after) {
		if (!active(player, step) || !player.isQuestInState(step.slot, before))
			return false;
		player.setQuest(step.slot, after);
		return true;
	}

	static boolean needs(Player player, Encounter role) {
		return active(player, role.step)
				&& player.isQuestInState(role.step.slot, "fight");
	}

	static int traces(Player player) {
		String state = player.getQuest(Step.COAST_WRECK.slot, 0);
		if ("survivor".equals(state) || "report".equals(state)
				|| "done".equals(state))
			return 3;
		String bits = player.getQuest(Step.COAST_WRECK.slot, 1);
		return "1".equals(bits) ? 1 : "2".equals(bits) ? 2 : 0;
	}

	static boolean trace(Player player, int bit) {
		if (!active(player, Step.COAST_WRECK)
				|| !player.isQuestInState(Step.COAST_WRECK.slot, 0, "traces"))
			return false;
		int before = traces(player), after = before | bit;
		if (before == after)
			return false;
		player.setQuest(Step.COAST_WRECK.slot,
				after == 3 ? "survivor" : "traces;" + after);
		return true;
	}

	static void recordKill(Player player, RPEntity victim) {
		if (!(victim instanceof CoastTrailGuard))
			return;
		Encounter role = ((CoastTrailGuard) victim).role;
		if (!needs(player, role) || victim.getZone() == null
				|| player.getZone() != victim.getZone()
				|| !role.zone.equals(victim.getZone().getName())
				|| !player.nextTo(victim, 16))
			return;
		CreatureRespawnPoint source = ((CoastTrailGuard) victim).getRespawnPoint();
		if (source == null || source.getZone() != victim.getZone()
				|| source.getX() != role.x || source.getY() != role.y)
			return;
		player.setQuest(role.step.slot, role.next);
		player.sendPrivateText(role == Encounter.BEACH
				? "Rabuś ustępuje. W jego obozowisku został znak składu przy porcie. Lubor powinien o nim usłyszeć."
				: role == Encounter.CHIEF
						? "Herszt nie zdoła już uciec. Przy beczkach zostały jego zapiski i osłony skradzionych lamp."
						: "Rabuś wypuszcza narzędzia z dłoni. Możesz podejść do latarni pilota i nadać sygnał dla Jaskółki.");
	}

	static String objective(Player player, Step step) {
		String state = player.getQuest(step.slot, 0);
		switch (step) {
		case COAST_WRECK:
			if ("survivor".equals(state))
				return "Porozmawiaj z rozbitkiem Luborem na plaży Gdańska NW (110,60). Powiedz: zaginiona Mewa.";
			if ("report".equals(state))
				return "Wróć do Staszka na przystani promu w Gdańsku NW. Powiedz: zaginiona Mewa.";
			int bits = traces(player);
			return "Na plaży Gdańska NW zbadaj "
					+ ((bits & 1) == 0 ? "skrzynię z Mewy (97,57)" : "")
					+ (bits == 0 ? " i " : "")
					+ ((bits & 2) == 0 ? "strzępy olinowania (105,58)" : "")
					+ ". Szukaj na wschód od przystani promu.";
		case COAST_LIGHTS:
			if ("lookout".equals(state))
				return "Obejrzyj palenisko na wschodnim krańcu plaży Gdańska NE (105,62).";
			return "report".equals(state)
					? "Wróć do Lubora w Gdańsku NW z wieściami o fałszywym świetle."
					: "Pokonaj rabusia wybrzeża przy fałszywym palenisku w Gdańsku NE (108,64).";
		case COAST_CARGO:
			if ("cargo".equals(state))
				return "Sprawdź skrzynię w piwnicy składu (27,12). Wejście do składu: Gdańsk N (27,77); schody w budynku prowadzą do piwnicy.";
			return "rosa".equals(state)
					? "Zapytaj Rosę w tawernie Gdańska N o ładunek z Mewy."
					: "Zanieś Malteisowi relację Rosy. Powiedz: ładunek z Mewy.";
		case COAST_WRECKERS:
			if ("papers".equals(state))
				return "Zabierz zapiski przywódcy z głębi piwnicy składu (30,26).";
			return "report".equals(state)
					? "Pokaż Malteisowi plan rabusiów. Powiedz: rabusie wybrzeża."
					: "Pokonaj herszta nadbrzeżnych rabusiów w głębi piwnicy składu (28,26).";
		case COAST_LAMPS:
			if ("rigging".equals(state))
				return "Napraw linki lamp sygnałowych na plaży Gdańska NE (56,60).";
			if ("lamps".equals(state))
				return "1".equals(player.getQuest(step.slot, 1))
						? "Ustaw zieloną lampę po wschodniej stronie plaży Gdańska NE (75,57)."
						: "Najpierw ustaw czerwoną lampę po zachodniej stronie plaży Gdańska NE (40,57).";
			return "Zawiadom Lubora w Gdańsku NW, że lampy są gotowe. Powiedz: bezpieczny brzeg.";
		case COAST_RETURN:
			if ("fight".equals(state))
				return "Pokonaj niszczyciela sygnałów na plaży Gdańska NE (56,63).";
			if ("signal".equals(state))
				return "Nadaj sygnał latarnią pilota w Gdańsku NE (56,57).";
			return "lookout".equals(state)
					? "Zapytaj Lubora w Gdańsku NW, czy statek obrał bezpieczny kurs. Powiedz: bezpieczny kurs."
					: "Wróć do Staszka na przystani promu w Gdańsku NW. Zostaw miejsce na płaszcz i monety. Powiedz: ratunek dla Jaskółki.";
		default:
			throw new IllegalArgumentException("Not a coast quest");
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
		case COAST_WRECK:
			if ((traces(player) & 1) != 0)
				rows.add(
						"Zgnieciona skrzynia nosi nazwę Mewa. Rozbicie wydarzyło się blisko wejścia do portu.");
			if ((traces(player) & 2) != 0)
				rows.add(
						"Lina została przecięta nożem. Ktoś odcinał ładunek po rozbiciu statku.");
			if ("report".equals(state) || done)
				rows.add(
						"Lubor przeżył. Mewa weszła na mieliznę, płynąc w stronę światła ustawionego na brzegu.");
			break;
		case COAST_LIGHTS:
			if (!"lookout".equals(state))
				rows.add(
						"Palenisko miało blaszane osłony kierujące światło w stronę morza. To nie było ognisko rybaków.");
			if ("report".equals(state) || done)
				rows.add(
						"Pokonany rabuś zostawił znak składu przy gdańskim porcie.");
			break;
		case COAST_CARGO:
			if (!"cargo".equals(state))
				rows.add(
						"W piwnicy składu leży ładunek Mewy. Na skrzyni są stare oznaczenia transportów kupowanych przez Radomira.");
			if ("report".equals(state) || done)
				rows.add(
						"Rosa widziała ludzi sprzedających rzeczy rozbitków. Ich przywódca wracał do piwnicy składu.");
			break;
		case COAST_WRECKERS:
			if (!"fight".equals(state))
				rows.add(
						"Herszt rabusiów został pokonany. Nie ucieknie z dowodami.");
			if ("report".equals(state) || done)
				rows.add(
						"Plan wskazuje Jaskółkę jako następny cel. Odzyskano osłony lamp i zapas lin do naprawy sygnałów.");
			break;
		case COAST_LAMPS:
			if (!"rigging".equals(state))
				rows.add(
						"Zniszczone mocowania lamp zostały naprawione odzyskanymi linami.");
			if ("lamps;1".equals(player.getQuest(step.slot))
					|| "report".equals(state) || done)
				rows.add(
						"Czerwona lampa po zachodniej stronie brzegu jest ustawiona.");
			if ("report".equals(state) || done)
				rows.add(
						"Zielona lampa po wschodniej stronie brzegu jest ustawiona. Brakuje ostatniego sygnału dla pilota.");
			break;
		case COAST_RETURN:
			if (!"fight".equals(state))
				rows.add("Ostatni rabuś nie zniszczy już przygotowanych lamp.");
			if ("lookout".equals(state) || "ready".equals(state) || done)
				rows.add("Nadano umówiony sygnał latarnią pilota.");
			if ("ready".equals(state) || done)
				rows.add(
						"Lubor zobaczył, jak Jaskółka omija mieliznę i kieruje się do portu.");
			if (done)
				rows.add(
						"Jaskółka dopłynęła. Staszek podziękował mi w imieniu uratowanych pasażerów.");
			break;
		default:
			throw new IllegalArgumentException("Not a coast quest");
		}
		return rows;
	}
}
