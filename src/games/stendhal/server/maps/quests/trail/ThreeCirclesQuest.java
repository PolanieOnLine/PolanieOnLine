/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/** Reading an old carrier book supplies the phrase needed for the later pickup. */
public final class ThreeCirclesQuest extends TrailQuest {
    private static final String OFFER = "Te trzy kółka wyglądają znajomo, ale nie przypomnę sobie skąd. "
            + "Ceryl ma w bibliotece stare księgi kupieckie. Może trafi na taki znak. Pójdziesz go zapytać?";
    public ThreeCirclesQuest() { super(TrailQuestLine.Step.SEMOS_SIGN); }
    @Override protected void addDialogs() {
        offer("Zajrzyj do Ceryla w bibliotece Semos i zapytaj o #'trzy kółka'. "
                + "Powiedz mu też, że na kwicie numer 17 są litery S. Ł.");
        reply("Dagobert", (player, sentence, speaker) -> request(player, speaker, OFFER), "trzy kółka", "trzy kolka");
        reply("Ceryl", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "library")) {
                player.setQuest(step.slot, "shelf");
                speaker.say("Trzy kółka? Gdzieś już to widziałem. Chyba w starej księdze przewoźników. "
                        + "Leży na stole przy południowo-zachodnim rogu biblioteki. Przejrzyj ją i zobacz, "
                        + "czy dopisek przy tym znaku pasuje do liter na kwicie.");
            } else remind(player, speaker);
        }, "trzy kółka", "trzy kolka");
        reply("Ceryl", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "phrase")) complete(player, speaker, "phrase");
            else remind(player, speaker);
        }, "suchy ładunek", "suchy ladunek");
    }
    @Override protected String completedText() {
        return "Suchy ładunek... Tak, to pasuje do S. Ł. na kwicie. Tymi słowami przewoźnicy potwierdzali odbiór. "
                + "O, tutaj jest też miejsce: gospoda w Semos. Margaret pewnie wie, kto umawiał tam #'odbiór'. "
                + "Zajrzyj do niej, może ten człowiek jeszcze czeka na towar.";
    }
}
