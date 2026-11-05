/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.maps.Region;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.DeathMatchCreature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

/** Persistent, personal objectives of the Zakopane delivery investigation. */
public final class TrailQuestLine {
    public static final String CHAPTER = "Niepokój na trakcie";
    public static final String MINE_CHAPTER = "Druga zmiana";
    public static final String DUNGEON_CHAPTER = "Pod miastem";
    public static final String SEMOS_CHAPTER = "Ślad trzech kółek";
    public static final String FOREST = "0_koscielisko_e";
    public static final String GUARD = "strażnik składu";
    public static final int STOCK_X = 62;
    public static final int STOCK_Y = 61;

    public enum Step {
        DELIVERY("trail_delivery", "Dostawa, której nikt nie zamawiał", "Stasek", "inspect", 1200, 25,
                "W skrzyni Staska znalazł się kwit z pieczęcią ratusza. Trzeba sprawdzić, kto zamówił tę dostawę.",
                "Odwiedź Staska w sklepie w Zakopanem C i zapytaj o dostawę.", CHAPTER, 1, Region.ZAKOPANE_CITY),
        GUEST("trail_guest", "Ostatni gość Jagny", "Gościrad", "jagna", 1800, 40,
                "Zamówienie okazało się fałszywe. Jagna może pamiętać woźnicę, który odebrał zapasy.",
                "Zapytaj Gościrada w ratuszu Zakopanego C o woźnicę.", CHAPTER, 2, Region.ZAKOPANE_CITY),
        SEAL("trail_seal", "Za cudzą pieczęcią", "Brzezdom", "guard", 3000, 60,
                "Ślady wozu prowadzą do ukrytego składu. Pilnuje go strażnik. Warto sprawdzić leżące tam papiery.",
                "Odnajdź Brzezdoma na polanie we wschodnim Kościelisku i zapytaj o skład.", CHAPTER, 3, Region.ZAKOPANE_CITY),
        DEBT("trail_debt", "Dług wobec Zakopanego", "Gazda Wojtek", "witnesses;0", 2000, 100,
                "Wszystko wskazuje na to, że zapasy pochodzą z Zakopanego. Wojtek chce jeszcze usłyszeć, co powiedzą Stasek i Jagna.",
                "Przedstaw dowody Gazdzie Wojtkowi w ratuszu Zakopanego C. Powiedz: dowody.", CHAPTER, 4, Region.ZAKOPANE_CITY),
        SEMOS_LETTER("trail_semos_letter", "List z Zakopanego", "Lieutenant Drilenun", "bank", 1800, 40,
                "List znaleziony w składzie prowadzi do Semos. Porucznik Drilenun powinien wiedzieć, gdzie szukać odbiorcy.",
                "Idź do ratusza w Semos i pokaż list porucznikowi Drilenunowi. Powiedz: list.", SEMOS_CHAPTER, 1, Region.SEMOS_CITY),
        SEMOS_SIGN("trail_semos_sign", "Skąd ten znak", "Dagobert", "library", 2200, 50,
                "W księdze wpłat jest taki sam znak jak na zamówieniu Staska. Ceryl może znaleźć coś o nim w starych księgach.",
                "Zapytaj Dagoberta w banku Semos o trzy kółka.", SEMOS_CHAPTER, 2, Region.SEMOS_CITY),
        SEMOS_PICKUP("trail_semos_pickup", "Odbiór przy gospodzie", "Margaret", "wood", 3200, 80,
                "Odbiorca ma przyjść pod gospodę. Margaret chce podstawić mu skrzynię z polanami i sprawdzić, dla kogo pracuje.",
                "Porozmawiaj z Margaret w gospodzie w Semos. Zapytaj o odbiór.", SEMOS_CHAPTER, 3, Region.SEMOS_CITY),
        RADAN_PASSAGE("radan_passage", "Zamknięty chodnik", "Lieutenant Drilenun", "search", 1200, 0,
                "Radan ukrywa skradzione zapasy pod Semos. Drilenun wskazał boczny chodnik w podziemiach.",
                "Zapytaj porucznika Drilenuna w ratuszu Semos o chodnik.", DUNGEON_CHAPTER, 1, Region.SEMOS_CITY),
        RADAN_RESCUE("radan_rescue", "Nie wszyscy z własnej woli", "Bartek", "guards;0", 1800, 0,
                "W kryjówce jest związany robotnik. Najpierw trzeba rozprawić się z pilnującymi go ludźmi.",
                "Wejdź do bocznego chodnika pod Semos i odszukaj więźnia.", DUNGEON_CHAPTER, 2, Region.SEMOS_CITY),
        RADAN_TRANSPORT("radan_transport", "Ostatni transport", "Bartek", "brake", 1800, 0,
                "Radan przygotował wózek ze skradzionym towarem. Bartek wie, jak go zatrzymać.",
                "Porozmawiaj z uwolnionym Bartkiem o wózku.", DUNGEON_CHAPTER, 3, Region.SEMOS_CITY),
        RADAN_FINALE("radan_finale", "Rachunek Radana", "Lieutenant Drilenun", "guard", 5000, 150,
                "Transport jest zatrzymany. Pozostało pokonać zbrojnego i porozmawiać z Radanem.",
                "Odszukaj Radana w ostatnim pomieszczeniu kryjówki.", DUNGEON_CHAPTER, 4, Region.SEMOS_CITY),
        MINE_LEDGER("trail_mine_ledger", "Dopisek w księdze", "Lieutenant Drilenun", "barbarus", 3500, 60,
                "W księdze Radana jest zamówienie na zapasy dla nocnej zmiany w kopalni Semos. Drilenun chce sprawdzić, kto je odbierał.",
                "Zapytaj porucznika Drilenuna w ratuszu Semos o nocną zmianę.", MINE_CHAPTER, 1, Region.SEMOS_MINES, 30),
        MINE_TRACKS("trail_mine_tracks", "Kto schodzi po zmroku", "Barbarus", "clues;0", 4500, 80,
                "Barbarus nie zatrudnia nikogo na noc. W tunelach zostały jednak opakowania po racjach i ślady ciężkich butów.",
                "Zapytaj Barbarusa w kopalni Semos o ślady nocnej zmiany.", MINE_CHAPTER, 2, Region.SEMOS_MINES, 30),
        MINE_VOICES("trail_mine_voices", "Głosy spod skały", "Barbarus", "listen", 5500, 100,
                "Ślady prowadzą w głąb kopalni. Barbarus słyszał tam rozkazy. Trzeba sprawdzić, skąd dochodzą.",
                "Porozmawiaj z Barbarusem o głosach z głębi kopalni. Powiedz: głosy spod skały.", MINE_CHAPTER, 3, Region.SEMOS_MINES, 30),
        MINE_DISPATCH("trail_mine_dispatch", "Rozkaz na pergaminie", "Phalk", "guard", 10000, 200,
                "Zynn rozpoznał dawne komendy wojskowe. Phalk może wiedzieć, kto przenosi rozkazy między tunelami.",
                "Odszukaj Phalka przy zablokowanym przejściu w kopalni Semos. Zapytaj o łącznika.", MINE_CHAPTER, 4, Region.SEMOS_MINES, 30);

        public final String slot, title, npc, initial, description, hint, chapter, region;
        public final int xp, coins, order, minimumLevel;
        Step(String slot, String title, String npc, String initial, int xp, int coins, String description, String hint, String chapter, int order, String region) {
            this(slot, title, npc, initial, xp, coins, description, hint, chapter, order, region, 0);
        }
        Step(String slot, String title, String npc, String initial, int xp, int coins, String description, String hint, String chapter, int order, String region, int minimumLevel) {
            this.minimumLevel = minimumLevel;
            this.slot = slot; this.title = title; this.npc = npc; this.initial = initial;
            this.xp = xp; this.coins = coins; this.description = description; this.hint = hint;
            this.chapter = chapter; this.order = order; this.region = region;
        }
        public boolean available(final Player player) {
            return player.isQuestCompleted(previousSlot()) && player.getLevel() >= minimumLevel;
        }
        String previousSlot() {
            return ordinal() == 0 ? "wegiel_na_opal" : values()[ordinal() - 1].slot;
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
            if (!player.isQuestCompleted(step.previousSlot())) {
                return step.ordinal() == 0 ? "Najpierw pomóż Staskowi z węglem."
                        : objective(player, Step.values()[step.ordinal() - 1].slot);
            }
            if (player.getLevel() < step.minimumLevel && stage == null) {
                return "Zdobądź " + step.minimumLevel + ". poziom. Potem " + step.hint.substring(0, 1).toLowerCase() + step.hint.substring(1);
            }
            if (stage == null || "rejected".equals(stage)) return step.hint;
            switch (step) {
            case DELIVERY:
                if ("inspect".equals(stage)) return "Sprawdź skrzynię z dostawą w sklepie Staska w Zakopanem C.";
                if ("archive".equals(stage)) return "Przejrzyj rejestr zamówień na małym stoliku obok Gościrada w ratuszu w centrum Zakopanego (Zakopane C).";
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
                if ("report".equals(stage)) return "Wróć do Gazdy Wojtka w ratuszu Zakopanego C i przedstaw dowody. Powiedz: dowody.";
                final int witnesses = witnesses(player);
                return "Pokaż zapiski "
                        + ((witnesses & 1) == 0 ? "Staskowi w sklepie w Zakopanem C" : "")
                        + (witnesses == 0 ? " oraz " : "")
                        + ((witnesses & 2) == 0 ? "Jagnie w karczmie w Zakopanem NE" : "") + ". Zapytaj o dowody.";
            case SEMOS_LETTER:
                if ("bank".equals(stage)) return "Pokaż kwit Dagobertowi w banku Semos i sprawdź księgę wpłat przy jego ladzie. Zapytaj: kwit.";
                return "Wróć do porucznika Drilenuna w ratuszu Semos i porozmawiaj o liście.";
            case SEMOS_SIGN:
                if ("library".equals(stage)) return "Zapytaj Ceryla w bibliotece Semos o trzy kółka.";
                if ("shelf".equals(stage)) return "Przejrzyj starą księgę przewoźników na stole przy południowo-zachodnim rogu biblioteki Semos.";
                return "Porozmawiaj z Cerylem o słowach znalezionych w księdze. Powiedz: suchy ładunek.";
            case SEMOS_PICKUP:
                if ("wood".equals(stage)) return "Przynieś Margaret w gospodzie Semos 2 polana. Powiedz: przygotuj skrzynię.";
                if ("crate".equals(stage)) return "Sprawdź skrzynię obok gospody w Semos. Stoi na zewnątrz, po prawej stronie wejścia.";
                if ("contact".equals(stage)) return "Porozmawiaj z tragarzem Olkiem obok skrzyni pod gospodą. Użyj hasła z księgi: suchy ładunek.";
                if ("question".equals(stage)) return "Poproś tragarza Olka o kwit na odbiór. Powiedz: kwit.";
                return "Zanieś wiadomość porucznikowi Drilenunowi w ratuszu Semos. Zapytaj o tragarza.";
            case RADAN_PASSAGE: case RADAN_RESCUE: case RADAN_TRANSPORT: case RADAN_FINALE:
                return RadanDungeonProgress.objective(player, step);
            case MINE_LEDGER: case MINE_TRACKS: case MINE_VOICES: case MINE_DISPATCH:
                return MineTrailProgress.objective(player, step);
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
            if (done) entries.add("Brzezdom radzi pokazać znalezisko Gazdzie Wojtkowi w ratuszu w Zakopanem C.");
            break;
        case DEBT:
            entries.add("Wojtek chce wiedzieć, czy Stasek i Jagna poznają swoje towary. Trzeba pokazać im zapiski.");
            final int confirmed = done || "report".equals(stage) ? 3 : witnesses(player);
            if ((confirmed & 1) != 0) entries.add("Stasek poznaje swoje worki z węglem. Nie wysyłał ich do Kościeliska.");
            if ((confirmed & 2) != 0) entries.add("Jagna poznaje zamówioną mąkę. To ten sam woźnica miał ją przywieźć do karczmy.");
            if (done) entries.add("Wojtek pośle ludzi po zapasy i uprzedzi kupców o fałszywych zamówieniach. Został jeszcze list ze znakiem trzech kółek. Następny transport miał trafić do Semos.");
            break;
        case SEMOS_LETTER:
            entries.add("Drilenun chce sprawdzić, kto zapłacił za transport z Zakopanego. Dagobert prowadzi księgę wpłat w banku Semos.");
            if (!"bank".equals(stage)) entries.add("W księdze jest zaliczka na dostawę, ale zamiast nazwiska ktoś narysował trzy kółka. Kwit ma numer 17. Odbiór ma się odbyć przy gospodzie w Semos.");
            if (done) entries.add("Drilenun poprosił Dagoberta, żeby wstrzymał wypłatę. Trzeba jeszcze ustalić, kto posługuje się tym znakiem.");
            break;
        case SEMOS_SIGN:
            entries.add("Dagobert radzi zapytać Ceryla o znak trzech kółek.");
            if (!"library".equals(stage)) entries.add("Ceryl pamięta taki znak ze starej księgi przewoźników. Wskazał ją na stole przy wejściu do biblioteki.");
            if ("phrase".equals(stage) || done) entries.add("W księdze zapisano znak przewoźnika i dopisek: suchy ładunek. Ten sam zwrot jest na kwicie numer 17.");
            if (done) entries.add("Ceryl odczytał miejsce odbioru: obok gospody w Semos. Margaret powinna wiedzieć, kto tam przychodzi.");
            break;
        case SEMOS_PICKUP:
            entries.add("Margaret zgodziła się przygotować skrzynię z polanami zamiast prawdziwego towaru.");
            if (!"wood".equals(stage)) entries.add("Margaret dostała dwa polana. Skrzynia czeka obok wejścia do gospody.");
            if ("contact".equals(stage) || "question".equals(stage) || "return".equals(stage) || done) entries.add("Skrzynia jest gotowa do odbioru. Olek czeka obok niej na umówione hasło.");
            if ("question".equals(stage) || "return".equals(stage) || done) entries.add("Olek rozpoznał hasło: suchy ładunek. Można poprosić go o kwit.");
            if ("return".equals(stage) || done) entries.add("Na kwicie jest nazwisko Radan. Olek twierdzi, że dostał pieniądze i instrukcję odbioru, ale nie znał zawartości skrzyni.");
            if (done) entries.add("Drilenun dostał odpis kwitu. Bank wstrzyma wypłatę, a straż sprawdzi człowieka podpisanego jako Radan. Olek nie będzie już odbierał tych przesyłek.");
            break;
        case RADAN_PASSAGE: case RADAN_RESCUE: case RADAN_TRANSPORT: case RADAN_FINALE:
            return RadanDungeonProgress.history(player, step);
        case MINE_LEDGER: case MINE_TRACKS: case MINE_VOICES: case MINE_DISPATCH:
            return MineTrailProgress.history(player, step);
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

    /** Record nearby story kills only from the expected world respawn points. */
    public static void recordGuardKill(final Player player, final RPEntity victim) {
        MineTrailProgress.recordCourierKill(player, victim);
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
