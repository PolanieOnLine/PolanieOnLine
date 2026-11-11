/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * Each investigator keeps their own evidence, testimony and evacuation
 * progress.
 */
final class WarsawFortressProgress {
	static final String ZONE = "int_warszawa_fortress", MARK = "rycerz Mark",
			PRINCE = "Książę";
	static final String HEALER = "Dobrosława", SOLDIER = "Wszemił";

	enum Encounter {
		GATE("wartownik buntowników", 23, 37, Step.WARSAW_GATE, "witness"),
		STEWARD("zarządca Radomir", 25, 6, Step.WARSAW_STEWARD, "documents");

		final String name, next;
		final int x, y;
		final Step step;

		Encounter(String name, int x, int y, Step step, String next) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.step = step;
			this.next = next;
		}
	}

	private WarsawFortressProgress() {
	}

	static boolean active(Player player, Step step) {
		return player != null && player.hasQuest(step.slot)
				&& !player.isQuestCompleted(step.slot)
				&& player.isQuestCompleted(step.previousSlot());
	}

	static boolean needs(Player player, Encounter encounter) {
		return active(player, encounter.step)
				&& player.isQuestInState(encounter.step.slot, "fight");
	}

	static boolean advance(Player player, Step step, String before,
			String after) {
		if (!active(player, step) || !player.isQuestInState(step.slot, before))
			return false;
		player.setQuest(step.slot, after);
		return true;
	}

	static int mask(Player player, Step step) {
		String state = player.getQuest(step.slot, 0);
		if ("report".equals(state) || "done".equals(state))
			return 3;
		String bits = player.getQuest(step.slot, 1);
		return "1".equals(bits) ? 1 : "2".equals(bits) ? 2 : 0;
	}

	static boolean mark(Player player, Step step, String state, int bit) {
		if (!active(player, step)
				|| !player.isQuestInState(step.slot, 0, state))
			return false;
		int before = mask(player, step), after = before | bit;
		if (before == after)
			return false;
		player.setQuest(step.slot, after == 3 ? "report" : state + ";" + after);
		return true;
	}

	static void recordKill(Player player, RPEntity victim) {
		if (!(victim instanceof WarsawFortressGuard))
			return;
		WarsawFortressGuard guard = (WarsawFortressGuard) victim;
		Encounter role = guard.role;
		if (!needs(player, role) || victim.getZone() == null
				|| player.getZone() != victim.getZone()
				|| !ZONE.equals(victim.getZone().getName())
				|| !player.nextTo(victim, 16))
			return;
		CreatureRespawnPoint source = guard.getRespawnPoint();
		if (source == null || source.getZone() != victim.getZone()
				|| source.getX() != role.x || source.getY() != role.y)
			return;
		player.setQuest(role.step.slot, role.next);
		player.sendPrivateText(role == Encounter.GATE
				? "Wartownik wypuszcza broń z dłoni i ustępuje z drogi. Możesz dotrzeć do lazaretu."
				: "Radomir osuwa się na kolana. Jego ludzie rzucają broń. Zabezpiecz zapiski na stole, zanim ktoś je zniszczy.");
	}

	static String objective(Player player, Step step) {
		String state = player.getQuest(step.slot, 0);
		switch (step) {
		case WARSAW_GATE:
			if ("witness".equals(state))
				return "Porozmawiaj z Dobrosławą w lazarecie, w północno-wschodniej części warowni. Powiedz: zanim ruszy szturm.";
			return "report".equals(state)
					? "Zdaj sprawę Markowi w obozie pod zamkiem. Powiedz: zanim ruszy szturm."
					: "Wejdź do warowni we wschodniej Warszawie, na północ od obozu Marka. Pokonaj wartownika na dziedzińcu, niedaleko głównej bramy.";
		case WARSAW_PAY:
			if ("report".equals(state))
				return "Pokaż księciu zapiski o wypłatach i dostawach. Powiedz: niewypłacony żołd.";
			int bits = mask(player, step);
			return "Przejrzyj "
					+ ((bits & 1) == 0 ? "księgę wypłat w kancelarii po lewej stronie sali dowodzenia"
							: "")
					+ (bits == 0 ? " i " : "")
					+ ((bits & 2) == 0 ? "spis zapasów w południowo-wschodnim magazynie"
							: "")
					+ " w warszawskiej warowni.";
		case WARSAW_WITNESSES:
			if ("report".equals(state))
				return "Opowiedz Markowi, co usłyszano w warowni. Powiedz: ludzie w środku.";
			int heard = mask(player, step);
			return "Zapytaj o ludzi w środku: "
					+ ((heard & 1) == 0 ? "Wszemiła w koszarach po lewej stronie dziedzińca" : "")
					+ (heard == 0 ? " oraz " : "")
					+ ((heard & 2) == 0 ? "Dobrosławę w lazarecie w północno-wschodniej części warowni" : "")
					+ ".";
		case WARSAW_FIRE:
			if ("report".equals(state))
				return "Uspokój Dobrosławę w lazarecie. Powiedz: beczki z olejem.";
			return "orders".equals(state)
					? "Przeczytaj rozkaz na stole w zbrojowni po prawej stronie dziedzińca."
					: "Unieszkodliw olej w południowo-wschodnim magazynie. Beczka stoi w głębi pomieszczenia, przy wschodniej ścianie.";
		case WARSAW_STEWARD:
			if ("documents".equals(state))
				return "Zabezpiecz zapiski Radomira na stole przy północnej ścianie sali dowodzenia.";
			return "report".equals(state)
					? "Zanieś zapiski księciu. Powiedz: zarządca Radomir."
					: "Pokonaj Radomira w sali dowodzenia na północnym końcu dziedzińca warowni.";
		case WARSAW_TRUCE:
			if ("truce".equals(state))
				return "Uzgodnij ze Wszemiłem wyjście rannych. Powiedz: otwarta brama.";
			if ("gate".equals(state))
				return "Zabezpiecz linę przy głównej bramie, na południowym końcu dziedzińca, żeby załoga mogła bezpiecznie wyjść.";
			if ("prince".equals(state))
				return "Poproś księcia o ocalenie załogi. Powiedz: ocalenie załogi.";
			return "Wróć do Marka z odpowiedzią księcia. Zostaw miejsce w plecaku. Powiedz: ocalenie załogi.";
		default:
			throw new IllegalArgumentException("Not a Warsaw quest");
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
		case WARSAW_GATE:
			if (!"fight".equals(state))
				rows.add(
						"Wartownik został pokonany. Droga do lazaretu jest wolna.");
			if ("report".equals(state) || done)
				rows.add(
						"Dobrosława opiekuje się rannymi po obu stronach sporu. Załoga zbuntowała się po kolejnych miesiącach bez żołdu.");
			break;
		case WARSAW_PAY:
			if ((mask(player, step) & 1) != 0)
				rows.add(
						"Księga zawiera podpisy odbiorców żołdu, których Dobrosława uważa za zmarłych.");
			if ((mask(player, step) & 2) != 0)
				rows.add(
						"Spis zapasów wymienia Radana i znak trzech kółek. Dostawy opłacano z żołdu załogi.");
			break;
		case WARSAW_WITNESSES:
			if ((mask(player, step) & 1) != 0)
				rows.add(
						"Wszemił twierdzi, że Radomir zabierał pieniądze i groził żołnierzom szubienicą.");
			if ((mask(player, step) & 2) != 0)
				rows.add(
						"Dobrosława potwierdza, że zarządca kazał składać cudze podpisy. Nie wszyscy mieszkańcy warowni popierają bunt.");
			break;
		case WARSAW_FIRE:
			if (!"orders".equals(state))
				rows.add(
						"Rozkaz nakazuje podpalić magazyn i kancelarię po rozpoczęciu szturmu.");
			if ("report".equals(state) || done)
				rows.add(
						"Olej został wylany w bezpiecznym miejscu, a naczynia do jego przenoszenia zniszczone.");
			break;
		case WARSAW_STEWARD:
			if (!"fight".equals(state))
				rows.add("Radomir został pokonany. Jego ludzie złożyli broń.");
			if ("report".equals(state) || done)
				rows.add(
						"Zapiski potwierdzają przywłaszczenie żołdu i kupowanie skradzionych dostaw od ludzi Radana.");
			break;
		case WARSAW_TRUCE:
			if (!"truce".equals(state))
				rows.add(
						"Wszemił zgodził się wyprowadzić rannych i oddać broń.");
			if ("prince".equals(state) || "ready".equals(state) || done)
				rows.add(
						"Brama została zabezpieczona. Ranni mogą bezpiecznie przejść do obozu.");
			if ("ready".equals(state) || done)
				rows.add(
						"Książę odwołał szturm i obiecał uczciwy osąd załogi. Za kradzieże i rozkaz podpalenia odpowie Radomir.");
			if (done)
				rows.add(
						"Mark podziękował za ocalenie ludzi i przekazał mi zbroję ze składu księcia.");
			break;
		default:
			throw new IllegalArgumentException("Not a Warsaw quest");
		}
		return rows;
	}
}
