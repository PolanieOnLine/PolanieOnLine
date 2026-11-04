/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/** Witnesses and a physical trail reveal where the false delivery was taken. */
public final class LastGuestQuest extends TrailQuest {
    private static final String OFFER = "Woźnica mógł zatrzymać się u Jagny, w karczmie "
            + "na północnym wschodzie Zakopanego. Zagląda tam wielu podróżnych. "
            + "Może go pamięta. Popytasz ją o niego?";

    public LastGuestQuest() { super(TrailQuestLine.Step.GUEST); }
    @Override protected void addDialogs() {
        offer("Idź do Jagny i zapytaj o #'woźnicę'. Karczma jest na północnym wschodzie Zakopanego. "
                + "Dowiedz się, dokąd się wybierał. Może ktoś widział go po drodze.");
        reply("Gościrad", (player, sentence, speaker) -> request(player, speaker, OFFER), "woźnica", "woznica", "woźnicę");
        reply("Jagna", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "jagna")) {
                player.setQuest(step.slot, "brzezdom");
                speaker.say("Pamiętam go. Ledwo usiadł, a już zerkał przez okno na swój wóz. "
                        + "Podobno jechał do kopalni, ale pytał mnie o polanę we wschodnim Kościelisku. "
                        + "Kiedy wspomniałam o Brzezdomie, od razu chciał wiedzieć, "
                        + "o której tamten wraca do domu. Brzezdom chodzi po tej polanie. Zapytaj go o #'woźnicę'.");
            } else { remind(player, speaker); }
        }, "woźnica", "woznica", "woźnicę");
        reply("Brzezdom", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "brzezdom")) {
                player.setQuest(step.slot, "tracks");
                speaker.say("Do kopalni? Nie, skręcił przy lesie i wjechał między drzewa. "
                        + "Potem usłyszałem trzask desek i ktoś zaczął strasznie kląć. "
                        + "Na północ od nas, przy wschodnim skraju polany, leży rozbita skrzynka. "
                        + "Sprawdź, czy ma ten sam znak co zamówienie. Potem wróć, pogadamy jeszcze o #'woźnicy'.");
            } else if (player.isQuestInState(step.slot, "found")) {
                complete(player, speaker, "found");
            } else { remind(player, speaker); }
        }, "woźnica", "woznica", "woźnicy");
    }
    @Override protected String completedText() {
        return "Ten sam znak? Czyli to jego skrzynka. "
                + "Wygląda na to, że specjalnie zjechał z drogi. "
                + "Na północy polany widziałem też jakiś #'skład'. Zapytaj mnie o niego, zanim tam pójdziesz.";
    }
}
