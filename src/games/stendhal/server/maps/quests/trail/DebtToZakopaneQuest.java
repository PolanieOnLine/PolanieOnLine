/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/** Close the Zakopane investigation with witnesses and leave an explicit lead to Semos. */
public final class DebtToZakopaneQuest extends TrailQuest {
    private static final String OFFER = "Brzezdom przekazał mi już wieść o składzie. Opał i mąka wrócą do mieszkańców, "
            + "ale zanim ogłoszę, co się stało, potrzebuję potwierdzenia. Stasek rozpozna dostawę, "
            + "a Jagna może połączyć ją z woźnicą. Zbierzesz ich zeznania?";

    public DebtToZakopaneQuest() { super(TrailQuestLine.Step.DEBT); }
    @Override protected void addDialogs() {
        offer("Zapytaj Staska w Zakopanem C oraz Jagnę w karczmie w Zakopanem NE o #'dowody'. "
                + "Pokaż im zapisane znaki i treść dokumentów. Potem wróć do mnie z potwierdzeniem obojga.");
        reply("Gazda Wojtek", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "report")) complete(player, speaker, "report");
            else request(player, speaker, OFFER);
        }, "dowody", "zeznania");
        addWitness("Stasek", 1, "To mój znak na workach z opałem. Żadnego odbioru w Kościelisku nie zlecałem. "
                + "Człowiek pokazał mi pieczęć ratusza i kazał szykować następny transport. Potwierdzam twoje ustalenia.");
        addWitness("Jagna", 2, "Spis się zgadza. Ta mąka miała trafić do karczmy. Woźnica mówił, że zawiezie ją "
                + "do miejskiego magazynu, a mnie zostawi potwierdzenie później. Teraz wiem, po co pytał o Brzezdoma. Potwierdzam twoje ustalenia.");
        reply("Gazda Wojtek", (player, sentence, speaker) -> {
            if (player.isQuestCompleted(step.slot)) speaker.say("List ze składu mówi o kolejnym odbiorze w Semos. "
                    + "Nie ma nazwiska odbiorcy, tylko znak trzech kółek. To trop do dalszego śledztwa, "
                    + "a nie powód, by oskarżać wszystkich tamtejszych kupców.");
            else remind(player, speaker);
        }, "list", "Semos", "trzy kółka");
    }
    private void addWitness(final String name, final int bit, final String statement) {
        reply(name, (player, sentence, speaker) -> {
            if (!"witnesses".equals(player.getQuest(step.slot, 0))) { remind(player, speaker); return; }
            final int witnesses = TrailQuestLine.witnesses(player) | bit;
            player.setQuest(step.slot, witnesses == 3 ? "report" : "witnesses;" + witnesses);
            speaker.say(statement + (witnesses == 3 ? " Wróć do Wojtka i powiedz: dowody."
                    : " Potrzebujesz jeszcze potwierdzenia drugiego świadka."));
        }, "dowody", "zeznania");
    }
    @Override protected String completedText() {
        return "Mamy dokumenty i potwierdzenie świadków. Poślę ludzi po zapasy, a kupcy dostaną ostrzeżenie "
                + "o fałszywej pieczęci. Dziękuję. Pomoc przy zbieraniu opału pokazała, że można ci zaufać, "
                + "ale tym razem udało ci się ochronić całe miasto przed kolejnymi stratami. "
                + "Został #'list' o następnym odbiorze w #'Semos'. Zachowamy go jako trop do dalszego śledztwa.";
    }
}
