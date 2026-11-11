/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.entity.npc.action.ExamineChatAction;

/**
 * A personal decoy and spoken pickup phrase expose the carrier's signed
 * receipt.
 */
public final class FalsePickupQuest extends TrailQuest {
	private static final String OFFER = "Widziałam, jak tragarz czekał przy skrzyniach obok gospody. "
			+ "Możemy podstawić mu skrzynię z polanami zamiast prawdziwego towaru. "
			+ "Skoro znasz hasło, może pokaże ci, kto go przysłał. Spróbujemy?";

	public FalsePickupQuest() {
		super(TrailQuestLine.Step.SEMOS_PICKUP);
	}

	@Override
	protected void addDialogs() {
		final ExamineChatAction showReceipt = new ExamineChatAction(
				"kwit-radana.png", "Kwit odbioru", "");
		offer("Przynieś mi dwa #'polana'. Włożę je do skrzyni i przykryję płótnem. "
				+ "Powinny starczyć, żeby tragarz poczuł ciężar. Olek czeka na zewnątrz, po prawej stronie wejścia.");
		reply("Margaret",
				(player, sentence, speaker) -> request(player, speaker, OFFER),
				"odbiór", "odbior");
		reply("Margaret", (player, sentence, speaker) -> {
			if (!player.isQuestInState(step.slot, "wood")) {
				remind(player, speaker);
				return;
			}
			if (!player.dropSubmittable("polano", 2)) {
				speaker.say(
						"Do pustej skrzyni zajrzy od razu. Przynieś dwa #polana, żeby było co dźwigać. "
								+ "Poszukaj na polanach za miastem, tam powinno być drewno.");
				return;
			}
			player.setQuest(step.slot, "crate");
			speaker.say(
					"Gotowe. Skrzynia stoi na zewnątrz, po prawej stronie wejścia do gospody. "
							+ "Zajrzyj pod płótno, nim podejdziesz do Olka. Dla niego to po prostu kolejny #'suchy ładunek'.");
		}, "przygotuj skrzynię", "przygotuj skrzynie", "polano", "polana");
		reply("Tragarz Olek", (player, sentence, speaker) -> {
			if (player.isQuestInState(step.slot, "contact")) {
				player.setQuest(step.slot, "question");
				speaker.say(
						"No, nareszcie ktoś z towarem. Wszystko suche? Dobrze. "
								+ "Mam odebrać jedną skrzynię. Jeśli chcesz, mogę pokazać #'kwit', zanim ją zabiorę.");
			} else if (player.isQuestInState(step.slot, "question")) {
				speaker.say("Tak, hasło się zgadza. Chcesz obejrzeć #'kwit'?");
			} else if (player.isQuestInState(step.slot, "return")) {
				speaker.say(
						"Porucznik powinien zobaczyć ten podpis. Tylko powiedz mu, że ja nie wiedziałem o żadnej kradzieży.");
			} else if (player.isQuestCompleted(step.slot)) {
				speaker.say(
						"Nie odbieram już tych przesyłek. Wystarczy mi kłopotów.");
			} else {
				speaker.say(
						"Najpierw musi być gotowa skrzynia. Na razie nie mam czego odbierać.");
			}
		}, "suchy ładunek", "suchy ladunek");
		reply("Tragarz Olek", (player, sentence, speaker) -> {
			if (player.isQuestInState(step.slot, "return")
					|| player.isQuestCompleted(step.slot)) {
				speaker.say(
						"Radan, tak jest podpisane. Nic więcej o nim nie wiem. Miałem tylko odebrać skrzynię.");
				showReceipt.fire(player, sentence, speaker);
				return;
			}
			if (!player.isQuestInState(step.slot, "question")) {
				speaker.say(
						"Najpierw muszę wiedzieć, czy to ty masz mi wydać przesyłkę.");
				return;
			}
			player.setQuest(step.slot, "return");
			speaker.say(
					"Proszę. Podpisano: Radan. Dał mi pieniądze i kazał odebrać skrzynię po umówionym haśle. "
							+ "Nie wiem, co miało być w środku. Jeśli coś się nie zgadza, pokaż ten kwit porucznikowi Drilenunowi w koszarach w Ados. "
							+ "Zaczekam, aż to wyjaśnicie.");
			showReceipt.fire(player, sentence, speaker);
		}, "kwit", "dla kogo");
		reply("Lieutenant Drilenun", (player, sentence, speaker) -> {
			if (player.isQuestInState(step.slot, "return"))
				complete(player, speaker, "return");
			else
				remind(player, speaker);
		}, "tragarz", "Olek");
	}

	@Override
	protected String completedText() {
		return "Radan. Wreszcie mamy nazwisko i podpis. Dagobert zatrzyma pieniądze, "
				+ "a ja poślę ludzi, żeby sprawdzili zleceniodawcę Olka. "
				+ "Dobrze, że udało się to załatwić bez kolejnej kradzieży. "
				+ "Napiszę do Wojtka, żeby nie martwił się o tę dostawę. "
				+ "A co do Radana, chyba wiem, gdzie go szukać. Pod Semos jest pewien stary #chodnik...";
	}
}
