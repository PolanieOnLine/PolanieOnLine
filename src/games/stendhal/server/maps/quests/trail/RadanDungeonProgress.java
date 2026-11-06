/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Persistent checkpoints, independent of the lifetime of a private map. */
final class RadanDungeonProgress {
	private RadanDungeonProgress() {
	}

	static boolean mayEnter(Player player) {
		return player.isQuestCompleted(Step.SEMOS_PICKUP.slot)
				&& (player.isQuestInState(Step.RADAN_PASSAGE.slot, "brace")
						|| player.isQuestCompleted(Step.RADAN_PASSAGE.slot))
				&& !player.isQuestCompleted(Step.RADAN_FINALE.slot);
	}

	static void entered(Player player) {
		if (player.isQuestInState(Step.RADAN_PASSAGE.slot, "brace")) {
			finish(player, Step.RADAN_PASSAGE);
			player.setQuest(Step.RADAN_RESCUE.slot, "guards;0");
		}
	}

	static int guards(Player player) {
		if (player.isQuestInState(Step.RADAN_RESCUE.slot, "rescue")
				|| player.isQuestCompleted(Step.RADAN_RESCUE.slot))
			return 3;
		String mask = player.getQuest(Step.RADAN_RESCUE.slot, 1);
		return "1".equals(mask) ? 1 : "2".equals(mask) ? 2 : 0;
	}

	static void defeated(Player player, int role) {
		if (role == 4) {
			if (player.isQuestInState(Step.RADAN_FINALE.slot, "guard")) {
				player.setQuest(Step.RADAN_FINALE.slot, "radan");
				player.sendPrivateText(
						"Zbrojny padł. Porozmawiaj z Radanem w ostatniej izbie.");
			}
		} else if (player.isQuestInState(Step.RADAN_RESCUE.slot, 0, "guards")) {
			int mask = guards(player) | role;
			player.setQuest(Step.RADAN_RESCUE.slot,
					mask == 3 ? "rescue" : "guards;" + mask);
			if (mask == 3)
				player.sendPrivateText(
						"Obaj strażnicy pokonani. Podejdź do Bartka i zapytaj o więzy.");
		}
	}

	static boolean free(Player player) {
		if (!player.isQuestInState(Step.RADAN_RESCUE.slot, "rescue"))
			return false;
		finish(player, Step.RADAN_RESCUE);
		player.setQuest(Step.RADAN_TRANSPORT.slot, "brake");
		return true;
	}

	static boolean brake(Player player) {
		if (!player.isQuestInState(Step.RADAN_TRANSPORT.slot, "brake"))
			return false;
		player.setQuest(Step.RADAN_TRANSPORT.slot, "chain");
		return true;
	}

	static boolean chain(Player player) {
		if (!player.isQuestInState(Step.RADAN_TRANSPORT.slot, "chain"))
			return false;
		finish(player, Step.RADAN_TRANSPORT);
		player.setQuest(Step.RADAN_FINALE.slot, "guard");
		return true;
	}

	static boolean confront(Player player) {
		if (!player.isQuestInState(Step.RADAN_FINALE.slot, "radan"))
			return false;
		player.setQuest(Step.RADAN_FINALE.slot, "return");
		return true;
	}

	private static void finish(Player player, Step step) {
		if (player.isQuestCompleted(step.slot))
			return;
		player.setQuest(step.slot, "done");
		player.addXP(step.xp);
	}

	static String objective(Player player, Step step) {
		String stage = player.getQuest(step.slot, 0);
		if (player.isQuestCompleted(step.slot))
			return "Ten etap wyprawy jest zakończony.";
		switch (step) {
		case RADAN_PASSAGE:
			return "search".equals(stage)
					? "Zejdź do pierwszych podziemi Semos. Od schodów idź na zachód. Sprawdź porzucone narzędzia przy bocznym chodniku."
					: "Sprawdź boczny chodnik obok narzędzi w pierwszych podziemiach Semos. Belka jest już zabezpieczona.";
		case RADAN_RESCUE:
			return "rescue".equals(stage)
					? "Porozmawiaj z Bartkiem w północno-zachodniej izbie kryjówki. Powiedz: więzy."
					: "Pokonaj dwóch strażników w północno-zachodniej izbie kryjówki Radana. Możesz wrócić przez boczny chodnik pod Semos.";
		case RADAN_TRANSPORT:
			return "chain".equals(stage)
					? "Odepnij łańcuch po prawej stronie hamulca przy wózku w kryjówce Radana."
					: "Zaciągnij hamulec przy północnej ścianie izby z wózkiem, na wschód od miejsca uwięzienia Bartka.";
		case RADAN_FINALE:
			if ("guard".equals(stage))
				return "Pokonaj zbrojnego Radana w ostatniej izbie, na południowym wschodzie kryjówki.";
			if ("radan".equals(stage))
				return "Porozmawiaj z Radanem w ostatniej izbie. Zapytaj o transport.";
			return "Wróć do porucznika Drilenuna w ratuszu Semos. Powiedz: Radan. Zrób miejsce w plecaku na nagrodę.";
		default:
			throw new IllegalArgumentException("Not a dungeon step: " + step);
		}
	}

	static List<String> history(Player player, Step step) {
		List<String> entries = new ArrayList<>();
		if (!player.hasQuest(step.slot))
			return entries;
		switch (step) {
		case RADAN_PASSAGE:
			entries.add(
					"Drilenun ustalił, że Radan trzyma zapasy w bocznym chodniku pod Semos. Nie chce wypłoszyć ludzi pilnujących wejścia.");
			if (!player.isQuestInState(step.slot, "search"))
				entries.add(
						"Przy narzędziach jest ślad po świeżym cięciu. Drewniany klin podpiera obluzowaną belkę. Wejście jest bezpieczne.");
			if (player.isQuestCompleted(step.slot))
				entries.add(
						"W bocznym chodniku słychać głosy. W głębi ktoś pilnuje związanego robotnika.");
			break;
		case RADAN_RESCUE:
			entries.add(
					"W izbie jest Bartek. Dwaj ludzie Radana pilnują go, żeby nie uciekł.");
			if (guards(player) == 3)
				entries.add("Obaj strażnicy zostali pokonani.");
			if (player.isQuestCompleted(step.slot))
				entries.add(
						"Bartek jest wolny. Radan zmuszał go do naprawiania toru. Trzeba zaciągnąć hamulec wózka, a potem odpiąć łańcuch. Bartek poczeka na straż w uwolnionej izbie.");
			break;
		case RADAN_TRANSPORT:
			entries.add(
					"Wózek jest połączony łańcuchem z wyciągiem. Najpierw trzeba zabezpieczyć koła hamulcem.");
			if (!player.isQuestInState(step.slot, "brake"))
				entries.add("Hamulec trzyma koła. Można odpiąć łańcuch.");
			if (player.isQuestCompleted(step.slot))
				entries.add(
						"Łańcuch jest odpięty. Skradziony towar zostanie w podziemiach. Przejście do Radana jest wolne.");
			break;
		case RADAN_FINALE:
			entries.add(
					"Radan ukrywa się w ostatniej izbie. Drogę zastawia jego zbrojny.");
			if (!player.isQuestInState(step.slot, "guard"))
				entries.add(
						"Zbrojny został pokonany. Radan nie ma już komu wydać rozkazów.");
			if (player.isQuestInState(step.slot, "return")
					|| player.isQuestCompleted(step.slot))
				entries.add(
						"Radan oddał księgę przewozów i poddał się. Straż może odzyskać zapasy i oddać je kupcom.");
			if (player.isQuestCompleted(step.slot))
				entries.add(
						"Drilenun wysłał strażników po Radana i towar. Bartek wróci do domu. Za pomoc dostaję rzadkie buty żelazne, 150 monet i doświadczenie.");
			break;
		default:
			throw new IllegalArgumentException("Not a dungeon step: " + step);
		}
		return entries;
	}
}
