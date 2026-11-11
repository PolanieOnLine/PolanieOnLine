/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/**
 * Close the Zakopane investigation with witnesses and leave an explicit lead to
 * Semos.
 */
public final class DebtToZakopaneQuest extends TrailQuest {
	private static final String OFFER = "Więc w tym składzie są nasze zapasy? Dobrze, że masz te papiery. "
			+ "Zanim poślę tam ludzi, chcę jeszcze mieć pewność, czyje to towary. Stasek powinien rozpoznać swoje worki, "
			+ "a Jagna pewnie pamięta, co zamawiała do karczmy. Popytasz ich o to?";

	public DebtToZakopaneQuest() {
		super(TrailQuestLine.Step.DEBT);
	}

	@Override
	protected void addDialogs() {
		offer("Pokaż #'dowody' Staskowi w sklepie w centrum Zakopanego i Jagnie w karczmie na północnym wschodzie miasta. "
				+ "Niech obejrzą zapiski i powiedzą, co poznają. Potem wróć do mnie.");
		reply("Gazda Wojtek", (player, sentence, speaker) -> {
			if (player.isQuestInState(step.slot, "report"))
				complete(player, speaker, "report");
			else
				request(player, speaker, OFFER);
		}, "dowody", "zeznania");
		addWitness("Stasek", 1,
				"Tak, to moje worki z węglem. Tylko co one robią w Kościelisku? "
						+ "Woźnica pokazał pieczęć ratusza i mówił, że wszystko jest uzgodnione. Kazał nawet szykować następną dostawę.",
				"Pokaż jeszcze te #'dowody' Jagnie w karczmie na północnym wschodzie Zakopanego. "
						+ "Może rozpozna coś swojego.");
		addWitness("Jagna", 2,
				"Tak, tę mąkę zamawiałam do karczmy. Woźnica mówił, że musi ją najpierw zawieźć "
						+ "do miejskiego magazynu, a potem przywiezie do mnie. No to już wiem, dlaczego tak wypytywał o Brzezdoma.",
				"Zajrzyj jeszcze do Staska w sklepie w centrum Zakopanego i pokaż mu #'dowody'. "
						+ "Może pozna swoje worki.");
		reply("Gazda Wojtek", (player, sentence, speaker) -> {
			if (player.isQuestCompleted(step.slot))
				speaker.say(
						"W liście piszą o następnym odbiorze towaru w Semos. "
								+ "Podpisu nie ma, są tylko te trzy kółka. "
								+ "Pokaż ten list porucznikowi Drilenunowi w ratuszu Semos. Przejście do Semos znajdziesz na północnym wschodzie centrum Zakopanego.");
			else
				remind(player, speaker);
		}, "list", "Semos", "trzy kółka");
	}

	private void addWitness(final String name, final int bit,
			final String statement, final String nextWitness) {
		reply(name, (player, sentence, speaker) -> {
			if (!"witnesses".equals(player.getQuest(step.slot, 0))) {
				remind(player, speaker);
				return;
			}
			final int witnesses = TrailQuestLine.witnesses(player) | bit;
			player.setQuest(step.slot,
					witnesses == 3 ? "report" : "witnesses;" + witnesses);
			speaker.say(statement + (witnesses == 3
					? " Wróć z tymi papierami do Gazdy Wojtka w ratuszu w centrum Zakopanego "
							+ "i przedstaw mu #'dowody'. Niech pośle ludzi po nasze zapasy."
					: " " + nextWitness));
		}, "dowody", "zeznania");
	}

	@Override
	protected String completedText() {
		return "Dobrze, że udało się to wyjaśnić. Poślę ludzi po zapasy i uprzedzę kupców, "
				+ "żeby uważali na takie zamówienia. Dziękuję ci. Najpierw pomoc sąsiadom, a teraz to. "
				+ "Gdyby nie ty, szukalibyśmy tych zapasów do wiosny. "
				+ "Następny transport miał trafić do #'Semos'. Pokaż #'list' porucznikowi Drilenunowi w tamtejszym ratuszu. Może pomoże znaleźć odbiorcę.";
	}
}
