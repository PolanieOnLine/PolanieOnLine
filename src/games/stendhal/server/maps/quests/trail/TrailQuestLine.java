/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.DeathMatchCreature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

/** Persistent, personal objectives of the Zakopane delivery investigation. */
public final class TrailQuestLine {
    public static final String CHAPTER = "Niepokój na trakcie";
    public static final String FOREST = "0_koscielisko_e";
    public static final String GUARD = "strażnik składu";
    public static final int STOCK_X = 62;
    public static final int STOCK_Y = 61;

    public enum Step {
        DELIVERY("trail_delivery", "Dostawa, której nikt nie zamawiał", "Stasek", "inspect", 1200, 25,
                "W skrzyni Staska znalazł się kwit z pieczęcią ratusza. Trzeba sprawdzić, kto zamówił tę dostawę.",
                "Odwiedź Staska w sklepie w Zakopanem C i zapytaj o dostawę."),
        GUEST("trail_guest", "Ostatni gość Jagny", "Gościrad", "jagna", 1800, 40,
                "Zamówienie okazało się fałszywe. Jagna może pamiętać woźnicę, który odebrał zapasy.",
                "Zapytaj Gościrada w ratuszu Zakopanego S o woźnicę."),
        SEAL("trail_seal", "Za cudzą pieczęcią", "Brzezdom", "guard", 3000, 60,
                "Ślady wozu prowadzą do ukrytego składu. Pilnuje go strażnik. Warto sprawdzić leżące tam papiery.",
                "Odnajdź Brzezdoma na polanie we wschodnim Kościelisku i zapytaj o skład."),
        DEBT("trail_debt", "Dług wobec Zakopanego", "Gazda Wojtek", "witnesses;0", 2000, 100,
                "Wszystko wskazuje na to, że zapasy pochodzą z Zakopanego. Wojtek chce jeszcze usłyszeć, co powiedzą Stasek i Jagna.",
                "Przedstaw dowody Gazdzie Wojtkowi w ratuszu Zakopanego S. Powiedz: dowody.");

        public final String slot, title, npc, initial, description, hint;
        public final int xp, coins;
        Step(String slot, String title, String npc, String initial, int xp, int coins, String description, String hint) {
            this.slot = slot; this.title = title; this.npc = npc; this.initial = initial;
            this.xp = xp; this.coins = coins; this.description = description; this.hint = hint;
        }
        public boolean available(final Player player) {
            final String previous = ordinal() == 0 ? "wegiel_na_opal" : values()[ordinal() - 1].slot;
            return player.isQuestCompleted(previous);
        }
    }

    private TrailQuestLine() { }

    public static String nextObjective(final Player player) {
        for (final Step step : Step.values()) {
            if (!player.isQuestCompleted(step.slot)) return objective(player, step.slot);
        }
        return null;
    }

    public static String objective(final Player player, final String slot) {
        final String stage = player.getQuest(slot, 0);
        for (final Step step : Step.values()) {
            if (!step.slot.equals(slot)) continue;
            if (!step.available(player)) {
                return step.ordinal() == 0 ? "Najpierw pomóż Staskowi z węglem."
                        : objective(player, Step.values()[step.ordinal() - 1].slot);
            }
            if (stage == null || "rejected".equals(stage)) return step.hint;
            switch (step) {
            case DELIVERY:
                if ("inspect".equals(stage)) return "Sprawdź skrzynię z dostawą w sklepie Staska w Zakopanem C.";
                if ("archive".equals(stage)) return "Przejrzyj rejestr zamówień na stole we wschodniej części sali Gościrada w ratuszu Zakopanego S.";
                return "Porozmawiaj z Gościradem o zamówieniu. Powiedz: zamówienie.";
            case GUEST:
                if ("jagna".equals(stage)) return "Zapytaj Jagnę w karczmie w Zakopanem NE o woźnicę.";
                if ("brzezdom".equals(stage)) return "Zapytaj Brzezdoma na polanie we wschodnim Kościelisku o woźnicę.";
                if ("tracks".equals(stage)) return "Sprawdź rozbitą skrzynkę na północno-wschodnim skraju polany we wschodnim Kościelisku, na północ od Brzezdoma.";
                return "Wróć do Brzezdoma i opowiedz o znalezionej skrzynce. Powiedz: woźnica.";
            case SEAL:
                if ("guard".equals(stage)) return "Pokonaj strażnika składu w północnej części polany we wschodnim Kościelisku. Jeśli go nie ma, poczekaj na jego powrót.";
                if ("documents".equals(stage)) return "Przeszukaj dokumenty przy workach w ukrytym składzie we wschodnim Kościelisku.";
                return "Wróć do Brzezdoma z dokumentami ze składu. Powiedz: skład.";
            case DEBT:
                if ("report".equals(stage)) return "Wróć do Gazdy Wojtka w ratuszu Zakopanego S i przedstaw dowody. Powiedz: dowody.";
                final int witnesses = witnesses(player);
                return "Pokaż zapiski "
                        + ((witnesses & 1) == 0 ? "Staskowi w sklepie w Zakopanem C" : "")
                        + (witnesses == 0 ? " oraz " : "")
                        + ((witnesses & 2) == 0 ? "Jagnie w karczmie w Zakopanem NE" : "") + ". Zapytaj o dowody.";
            default: throw new IllegalStateException("Unknown chapter step");
            }
        }
        return null;
    }

    public static List<String> history(final Player player, final Step step) {
        final List<String> entries = new ArrayList<>();
        final String stage = player.getQuest(step.slot, 0);
        if (stage == null) return entries;
        final boolean done = player.isQuestCompleted(step.slot);
        switch (step) {
        case DELIVERY:
            entries.add("Stasek znalazł dziwne zamówienie w skrzyni z opałem. Chce wiedzieć, czy naprawdę pochodzi z ratusza.");
            if (!"inspect".equals(stage)) entries.add("Na dokumencie są pieczęć ratusza i trzy kółka w miejscu podpisu.");
            if ("discrepancy".equals(stage) || done) entries.add("W księdze nie ma tego zamówienia. Nie brakuje żadnych stron, a na pozostałych dokumentach są inny podpis i pieczęć.");
            if (done) entries.add("Gościrad nie wystawił tego zamówienia. Trzeba znaleźć woźnicę, który odebrał zapasy.");
            break;
        case GUEST:
            entries.add("Gościrad radzi zapytać Jagnę o woźnicę. Mógł zatrzymać się w jej karczmie.");
            if (!"jagna".equals(stage)) entries.add("Jagna zapamiętała, że woźnica mówił o kopalni, ale pytał o boczną drogę i Brzezdoma.");
            if (!"jagna".equals(stage) && !"brzezdom".equals(stage)) entries.add("Brzezdom widział wóz skręcający przy lesie. Wskazał rozbitą skrzynkę na północno-wschodnim skraju polany.");
            if ("found".equals(stage) || done) entries.add("Skrzynka ma taki sam znak jak zamówienie. Między deskami został sznurek ubrudzony woskiem. Ślady wozu prowadzą w głąb polany.");
            if (done) entries.add("Brzezdom wskazał ukryty skład w północnej części polany. Zapasy są pilnowane.");
            break;
        case SEAL:
            entries.add("Brzezdom prosi o sprawdzenie składu. Najpierw trzeba pokonać strażnika. Samych zapasów nie należy zabierać.");
            if (!"guard".equals(stage)) entries.add("Walka ze strażnikiem za mną. Można sprawdzić papiery przy workach.");
            if ("recovered".equals(stage) || done) entries.add("W spisie są węgiel Staska i mąka dla karczmy. Jest też list o następnym transporcie do Semos.");
            if (done) entries.add("Brzezdom radzi pokazać znalezisko Gazdzie Wojtkowi w ratuszu w Zakopanem S.");
            break;
        case DEBT:
            entries.add("Wojtek chce wiedzieć, czy Stasek i Jagna poznają swoje towary. Trzeba pokazać im zapiski.");
            final int confirmed = done || "report".equals(stage) ? 3 : witnesses(player);
            if ((confirmed & 1) != 0) entries.add("Stasek poznaje swoje worki z węglem. Nie wysyłał ich do Kościeliska.");
            if ((confirmed & 2) != 0) entries.add("Jagna poznaje zamówioną mąkę. To ten sam woźnica miał ją przywieźć do karczmy.");
            if (done) entries.add("Wojtek pośle ludzi po zapasy i uprzedzi kupców o fałszywych zamówieniach. Został jeszcze list ze znakiem trzech kółek. Następny transport miał trafić do Semos.");
            break;
        default: throw new IllegalStateException("Unknown chapter step");
        }
        return entries;
    }

    static int witnesses(final Player player) {
        final String value = player.getQuest(Step.DEBT.slot, 1);
        if ("1".equals(value)) return 1;
        if ("2".equals(value)) return 2;
        return "3".equals(value) ? 3 : 0;
    }

    /** Count a real nearby kill only while this player's stockpile investigation is active. */
    public static void recordGuardKill(final Player player, final RPEntity victim) {
        if (player == null || !player.isQuestInState(Step.SEAL.slot, "guard")
                || !(victim instanceof Creature) || victim instanceof ArenaCreature || victim instanceof DeathMatchCreature
                || !GUARD.equals(victim.getName()) || victim.getZone() == null
                || player.getZone() != victim.getZone() || !FOREST.equals(victim.getZone().getName())
                || !player.nextTo(victim, 16)) return;
        final CreatureRespawnPoint origin = ((Creature) victim).getRespawnPoint();
        if (origin == null || origin.getZone() != victim.getZone()
                || origin.getX() != STOCK_X || origin.getY() != STOCK_Y) return;
        player.setQuest(Step.SEAL.slot, "documents");
        player.sendPrivateText("Strażnik już ci nie przeszkodzi. Sprawdź papiery przy workach.");
    }
}
