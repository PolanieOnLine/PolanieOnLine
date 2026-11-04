/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/** Follow the false delivery's payment to Semos without requiring Dagobert's side quest. */
public final class LetterFromZakopaneQuest extends TrailQuest {
    private static final String OFFER = "Wojtek przysłał cię z tym listem? Pokaż... "
            + "O dostawie nic nie słyszałem, ale jeśli ktoś za nią zapłacił, Dagobert będzie miał zapis w banku. "
            + "Sprawdzisz to u niego?";
    public LetterFromZakopaneQuest() { super(TrailQuestLine.Step.SEMOS_LETTER); }
    @Override protected void addDialogs() {
        offer("Idź do banku w Semos i pokaż Dagobertowi #'kwit'. Księga wpłat leży przy jego ladzie. "
                + "Sprawdź, czy ktoś wpłacił pieniądze za transport z Zakopanego, a potem wróć do mnie z #'listem'.");
        reply("Lieutenant Drilenun", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "checked")) complete(player, speaker, "checked");
            else request(player, speaker, OFFER);
        }, "list", "list z Zakopanego", "listem");
        reply("Dagobert", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "bank")) {
                speaker.say("Transport z Zakopanego? Ktoś rzeczywiście zostawił na niego zaliczkę. "
                        + "Sprawdź księgę przy mojej ladzie. Nie mam tam nazwiska, tylko jakiś znak. "
                        + "To zwykła księga wpłat, nie musisz wchodzić do prywatnego skarbca.");
            } else remind(player, speaker);
        }, "kwit", "zaliczka");
    }
    @Override protected String completedText() {
        return "Czyli pieniądze już czekają. Poproszę Dagoberta, żeby na razie ich nikomu nie wypłacał. "
                + "Zanim zatrzymamy odbiorcę, trzeba wiedzieć, kogo szukać. "
                + "Zapytaj Dagoberta o #'trzy kółka'. Może już kiedyś widział ten znak.";
    }
}
