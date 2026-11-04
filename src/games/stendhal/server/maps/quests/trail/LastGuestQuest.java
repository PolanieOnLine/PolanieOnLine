/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/** Witnesses and a physical trail reveal where the false delivery was taken. */
public final class LastGuestQuest extends TrailQuest {
    private static final String OFFER = "Woźnica musiał gdzieś odpocząć i napoić konie. Jagna w karczmie "
            + "w Zakopanem NE słyszy więcej niż urzędnicy zza biurek. Nie pytaj jej o złodzieja, "
            + "tylko o człowieka, który wiózł zapasy dla ratusza. Ustalisz jego drogę?";

    public LastGuestQuest() { super(TrailQuestLine.Step.GUEST); }
    @Override protected void addDialogs() {
        offer("Porozmawiaj z Jagną w karczmie w Zakopanem NE. Zapytaj o #'woźnicę'. "
                + "Zapamiętaj, co mówił o trasie, i sprawdź, czy ktoś rzeczywiście widział tam jego wóz.");
        reply("Gościrad", (player, sentence, speaker) -> request(player, speaker, OFFER), "woźnica", "woznica", "woźnicę");
        reply("Jagna", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "jagna")) {
                player.setQuest(step.slot, "brzezdom");
                speaker.say("Pamiętam go. Zapłacił od razu, ale co chwilę wyglądał przez okno. "
                        + "Mówił, że jedzie do kopalni, a pytał o drogę przez polanę we wschodnim Kościelisku. "
                        + "Gdy wspomniałam o Brzezdomie, nagle przestał się spieszyć i zaczął wypytywać, "
                        + "kiedy tamten wraca do domu. Odnajdź Brzezdoma na tamtejszej polanie i zapytaj o #'woźnicę'.");
            } else { remind(player, speaker); }
        }, "woźnica", "woznica", "woźnicę");
        reply("Brzezdom", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "brzezdom")) {
                player.setQuest(step.slot, "tracks");
                speaker.say("Do kopalni? Tamtędy nie jechał. Wóz skręcił przy lesie i zniknął między drzewami. "
                        + "Słyszałem trzask desek, a potem ktoś długo przeklinał. "
                        + "Sprawdź rozbitą skrzynkę na północno-wschodnim skraju tej polany, na północ stąd. "
                        + "Jeśli znajdziesz znak podobny do tego z zamówienia, wróć i opowiedz mi o #'woźnicy'.");
            } else if (player.isQuestInState(step.slot, "found")) {
                complete(player, speaker, "found");
            } else { remind(player, speaker); }
        }, "woźnica", "woznica", "woźnicy");
    }
    @Override protected String completedText() {
        return "Trzy kółka, sznurek i ślady ciężkiego wozu. To już więcej niż plotka. "
                + "Ten człowiek nie zgubił drogi, tylko chciał ukryć ładunek. "
                + "W północnej części polany ktoś urządził #'skład'. Zapytaj mnie o niego, zanim ruszysz dalej.";
    }
}
