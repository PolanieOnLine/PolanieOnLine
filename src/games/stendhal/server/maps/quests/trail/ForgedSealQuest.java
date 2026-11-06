/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/**
 * Recover documentary proof from the guarded stockpile rather than collecting
 * random loot.
 */
public final class ForgedSealQuest extends TrailQuest {
	private static final String OFFER = "Za drzewami, w północnej części polany, stoją worki i skrzynie. "
			+ "Pilnuje ich uzbrojony człowiek. Nie dał mi nawet podejść bliżej. "
			+ "Może tam właśnie trzymają nasze zapasy. Pokonasz strażnika i sprawdzisz, co tam leży?";

	public ForgedSealQuest() {
		super(TrailQuestLine.Step.SEAL);
	}

	@Override
	protected void addDialogs() {
		offer("Idź do północnej części tej polany. Pokonaj strażnika, "
				+ "a potem przejrzyj papiery przy workach. Zobacz, do kogo należą te zapasy. "
				+ "Worki zostaw, ich właściciele pewnie wciąż czekają na dostawę. "
				+ "Przynieś za to papiery. Musimy się dowiedzieć, kto urządził ten #'skład'.");
		reply("Brzezdom", (player, sentence, speaker) -> {
			if (player.isQuestInState(step.slot, "recovered")) {
				complete(player, speaker, "recovered");
			} else {
				request(player, speaker, OFFER);
			}
		}, "skład", "sklad", "ukryty skład");
	}

	@Override
	protected String completedText() {
		return "A więc to rzeczywiście nasze zapasy. Z tym spisem i listem "
				+ "idź do Gazdy Wojtka, do ratusza w centrum Zakopanego. "
				+ "Zanieś mu te #'dowody'. Ja zostanę i popilnuję worków. Dość już nam nakradli.";
	}
}
