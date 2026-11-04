/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/** Recover documentary proof from the guarded stockpile rather than collecting random loot. */
public final class ForgedSealQuest extends TrailQuest {
    private static final String OFFER = "Za drzewami, w północnej części tej polany, widziałem worki i skrzynie. "
            + "Pilnuje ich uzbrojony człowiek. Nie wygląda na kogoś, kto czeka na uczciwego kupca. "
            + "Jeżeli to zapasy Zakopanego, potrzebujemy dowodu. Pokonasz strażnika i sprawdzisz dokumenty przy workach?";

    public ForgedSealQuest() { super(TrailQuestLine.Step.SEAL); }
    @Override protected void addDialogs() {
        offer("Idź do północnej części polany we wschodnim Kościelisku. Pokonaj strażnika składu, "
                + "a potem sprawdź dokumenty przy workach. Zachowaj spis i znaki opakowań. "
                + "Nie zabieraj samych zapasów, bo najpierw trzeba ustalić, do kogo należą. "
                + "Wróć do mnie i powiedz #'skład'.");
        reply("Brzezdom", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "recovered")) {
                complete(player, speaker, "recovered");
            } else { request(player, speaker, OFFER); }
        }, "skład", "sklad", "ukryty skład");
    }
    @Override protected String completedText() {
        return "Nazwiska mieszkańców, ilości towarów i list do Semos. Teraz Wojtek nie będzie musiał "
                + "wierzyć nam na słowo. Zanieś tę wiadomość do Gazdy Wojtka w ratuszu Zakopanego S "
                + "i powiedz #'dowody'. Ja przypilnuję drogi do składu, żeby można było odzyskać zapasy.";
    }
}
