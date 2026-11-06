/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * The level-thirty continuation of the delivery investigation, in existing
 * mines.
 */
public final class MineTrailQuest extends TrailQuest {
	public MineTrailQuest(Step step) {
		super(step);
		if (!TrailQuestLine.MINE_CHAPTER.equals(step.chapter))
			throw new IllegalArgumentException("Not a mine quest");
	}

	@Override
	protected void addDialogs() {
		switch (step) {
		case MINE_LEDGER:
			offer("Barbarus pracuje w zachodniej odnodze kopalni, w górach na północ od Semos. "
					+ "Pokaż mu ten wpis o #'nocnej zmianie'. Jeśli ktoś pracował po zmroku, on powinien o tym wiedzieć.");
			reply(step.npc, (player, sentence, speaker) -> request(player,
					speaker,
					"Od rana przeglądam księgę Radana. Znalazłem dostawy jedzenia i drewna dla nocnej zmiany w kopalni. "
							+ "Tylko żadnego górnika nie wymieniono z nazwiska. Barbarus mógłby mi to wyjaśnić. Pójdziesz z nim porozmawiać?"),
					"nocna zmiana", "nocną zmianę", "nocnej zmianie");
			reply("Barbarus", (player, sentence, speaker) -> {
				if (!MineTrailProgress.active(player, step)) {
					remind(player, speaker);
					return;
				}
				if (player.isQuestInState(step.slot, "barbarus")) {
					player.setQuest(step.slot, "register");
					speaker.say(
							"Nocna zmiana? U mnie? Jak robi się późno, odkładamy kilofy i wracamy do domów. "
									+ "Zresztą zajrzyj do rejestru obok, po zachodniej stronie. Zapisuję każdy wózek. Widzisz tam coś z nocy?");
				} else if (player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					remind(player, speaker);
			}, "nocna zmiana", "nocną zmianę", "nocnej zmianie");
			break;
		case MINE_TRACKS:
			offer("Na północny wschód stąd jest duży zakręt. Leży przy nim skrzynka po racjach, a dalej na wschód "
					+ "widać odciski butów. Przyjrzyj się obu miejscom. Ja muszę zostać przy robocie.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (MineTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Jest coś jeszcze. Przy zakręcie leżą resztki racji, choć moi ludzie jedzą tutaj, przy narzędziach. "
									+ "Dalej ktoś zostawił ślady ciężkich butów. Obejrzysz to? Wolę wiedzieć, kto mi się kręci po kopalni.");
			}, "ślady nocnej zmiany", "slady nocnej zmiany");
			break;
		case MINE_VOICES:
			offer("Najwyraźniej słychać je przy starej obudowie szybu, na północnym wschodzie, niedaleko Phalka. "
					+ "Przyłóż ucho do belek, ale nie idź za blokadę. Jeśli rozróżnisz jakieś słowa, powtórz je Zynnowi w bibliotece. "
					+ "On zna stare dzieje Semos, może coś mu się przypomni.");
			reply(step.npc, (player, sentence, speaker) -> request(player,
					speaker,
					"Myślałem, że mi się przesłyszało. A tu najpierw krzyk, potem kroki, równo jak na komendę. "
							+ "I nagle cisza. Nie podoba mi się to. Posłuchasz przy starej obudowie szybu? Może tobie uda się coś zrozumieć."),
					"głosy spod skały", "glosy spod skaly",
					"głosach spod skały", "glosach spod skaly");
			reply("Zynn Iwuhos", (player, sentence, speaker) -> {
				if (MineTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, "history"))
					complete(player, speaker, "history");
				else
					remind(player, speaker);
			}, "rozkazy spod ziemi");
			break;
		case MINE_DISPATCH:
			offer("Przechodzi przy zakręcie na południe stąd. Ma przy sobie pakiety. Jeśli go pokonasz, przeszukaj je. "
					+ "Może znajdziesz #'rozkaz dla straży'. Przynieś mi go, obejrzę pieczęć. "
					+ "I trzymaj się tej strony blokady. Dalej sam nie pójdziesz bezpiecznie.");
			reply(step.npc, (player, sentence, speaker) -> request(player,
					speaker,
					"Zynn cię przysyła? Więc też usłyszał o tych komendach. Widziałem duergara z pakietami, chodzi między korytarzami. "
							+ "Sam nie mogę ruszyć się z przejścia. Zdobędziesz to, co niesie? Muszę wiedzieć, co się tu szykuje."),
					"łącznik", "lacznik", "łącznika", "lacznika");
			reply(step.npc, (player, sentence, speaker) -> {
				if (!MineTrailProgress.active(player, step)
						|| !player.isQuestInState(step.slot, "identified")) {
					remind(player, speaker);
					return;
				}
				player.setQuest(step.slot, "return");
				speaker.say(
						"Ta pieczęć... Tak, duergarowie. Zapasy, zmiana warty przy starej bramie. "
								+ "A więc to ich #'druga zmiana'. Zanieś rozkaz Drilenunowi do ratusza. "
								+ "Musi uprzedzić miasto. Ja zostanę przy przejściu.");
			}, "rozkaz dla straży", "rozkaz dla strazy");
			reply("Lieutenant Drilenun", (player, sentence, speaker) -> {
				if (!MineTrailProgress.active(player, step)
						|| !player.isQuestInState(step.slot, "return")) {
					remind(player, speaker);
					return;
				}
				complete(player, speaker, "return");
				new EquipItemAction("duży eliksir", 5).fire(player, sentence,
						speaker);
			}, "druga zmiana");
			break;
		default:
			throw new IllegalStateException("Not a mine quest");
		}
	}

	@Override
	protected String completedText() {
		switch (step) {
		case MINE_LEDGER:
			return "No widzisz. Żadnych nocnych wózków, ani jednego. Ktoś zapłacił za zapasy dla ludzi, których tu nie zatrudniam. "
					+ "Chyba jednak wiem, gdzie znaleźć #'ślady nocnej zmiany'.";
		case MINE_TRACKS:
			return "Takich butów nie nosi żaden z moich ludzi. A te racje... ktoś szykował prowiant na dłuższy pobyt. "
					+ "Powinienem był od razu powiedzieć o #'głosach spod skały'. Brałem je za echo, ale teraz sam już nie wiem.";
		case MINE_VOICES:
			return "Druga zmiana do bramy... Tak woła się przy zmianie warty. "
					+ "Kroki i sygnał na koniec też by pasowały. Obawiam się, że za skałą stoją żołnierze. "
					+ "Phalk strzeże przejścia w północno-wschodniej części kopalni. Może zauważył #łącznika, "
					+ "który przenosi te rozkazy. Porozmawiaj z nim, zanim pójdziesz dalej.";
		case MINE_DISPATCH:
			return "Więc Radan dostarczał zapasy duergarom... A my mieliśmy to za zwykłą kradzież. "
					+ "Dobrze, że Phalk został przy przejściu. Wyślę ostrzeżenie do Barbarusa i kupców. "
					+ "Najpierw musimy ustalić, ilu ich jest. Na dziś wystarczy. Weź 200 monet i te eliksiry, "
					+ "należy ci się porządny odpoczynek. Potem zajrzyj do Barbarusa. Niepokoją go #'zaginieni górnicy'.";
		default:
			throw new IllegalStateException("Not a mine quest");
		}
	}
}
