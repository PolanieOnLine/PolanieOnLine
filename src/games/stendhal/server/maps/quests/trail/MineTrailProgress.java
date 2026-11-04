/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.DeathMatchCreature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Personal evidence and kill credit for the first Semos mine investigation. */
final class MineTrailProgress {
    static final String ZONE = "-1_semos_mine_n2";
    static final String COURIER = "łącznik duergarów";
    static final int COURIER_X = 112, COURIER_Y = 32;
    private MineTrailProgress() { }
    static boolean active(Player player, Step step) {
        return player != null && player.isQuestCompleted(step.previousSlot())
                && player.hasQuest(step.slot) && !player.isQuestCompleted(step.slot);
    }
    static int clues(Player player) {
        if (player.isQuestInState(Step.MINE_TRACKS.slot, "report") || player.isQuestCompleted(Step.MINE_TRACKS.slot)) return 3;
        String mask = player.getQuest(Step.MINE_TRACKS.slot, 1);
        return "1".equals(mask) ? 1 : "2".equals(mask) ? 2 : 0;
    }
    static boolean addClue(Player player, int clue) {
        if (!active(player, Step.MINE_TRACKS) || !player.isQuestInState(Step.MINE_TRACKS.slot, 0, "clues")) return false;
        int previous = clues(player), next = previous | clue;
        if (previous == next) return false;
        player.setQuest(Step.MINE_TRACKS.slot, next == 3 ? "report" : "clues;" + next); return true;
    }
    static void recordCourierKill(Player player, RPEntity victim) {
        if (!active(player, Step.MINE_DISPATCH) || !player.isQuestInState(Step.MINE_DISPATCH.slot, "guard")
                || !(victim instanceof Creature) || victim instanceof ArenaCreature || victim instanceof DeathMatchCreature
                || !COURIER.equals(victim.getName()) || victim.getZone() == null || player.getZone() != victim.getZone()
                || !ZONE.equals(victim.getZone().getName()) || !player.nextTo(victim, 16)) return;
        CreatureRespawnPoint origin = ((Creature) victim).getRespawnPoint();
        if (origin == null || origin.getZone() != victim.getZone() || origin.getX() != COURIER_X || origin.getY() != COURIER_Y) return;
        player.setQuest(Step.MINE_DISPATCH.slot, "orders");
        player.sendPrivateText("Łącznik nie przeniesie już tego rozkazu. Sprawdź pakiet przy zakręcie, na południowy zachód od miejsca, którego pilnował.");
    }
    static String objective(Player player, Step step) {
        String state = player.getQuest(step.slot, 0);
        if (player.isQuestCompleted(step.slot)) return "Ten etap sprawy nocnej zmiany jest zakończony.";
        switch (step) {
        case MINE_LEDGER:
            if ("barbarus".equals(state)) return "Odszukaj Barbarusa w kopalni Semos, w górach na północ od miasta. Wejdź przez główne wejście i idź zachodnią odnogą. Zapytaj: nocna zmiana.";
            if ("register".equals(state)) return "Sprawdź rejestr wydobycia obok miejsca pracy Barbarusa w kopalni Semos. Księga leży po zachodniej stronie jego stanowiska.";
            return "Pokaż Barbarusowi brak wpisu w rejestrze. Powiedz: nocna zmiana.";
        case MINE_TRACKS:
            if ("report".equals(state)) return "Wróć do Barbarusa i opowiedz o racjach oraz śladach butów. Powiedz: ślady nocnej zmiany.";
            int mask = clues(player);
            return "Sprawdź " + ((mask&1)==0 ? "skrzynkę po racjach na zakręcie na północny wschód od Barbarusa" : "")
                    + (mask==0 ? " oraz " : "") + ((mask&2)==0 ? "ślady butów dalej na wschód, w odnodze prowadzącej ku Phalkowi" : "") + ".";
        case MINE_VOICES:
            return "listen".equals(state)
                    ? "Sprawdź starą obudowę szybu na północnym wschodzie kopalni Semos, na południowy zachód od Phalka. Posłuchaj głosów zza skały."
                    : "Porozmawiaj z Zynnem Iwuhosem w bibliotece Semos o usłyszanej komendzie. Powiedz: rozkazy spod ziemi.";
        case MINE_DISPATCH:
            if ("guard".equals(state)) return "Pokonaj łącznika duergarów przy zakręcie na południe od Phalka w kopalni Semos. Pozostań w pobliżu. Jeśli go nie ma, poczekaj na jego powrót.";
            if ("orders".equals(state)) return "Sprawdź pakiet z rozkazem na południowy zachód od miejsca, którego pilnował łącznik duergarów.";
            if ("identified".equals(state)) return "Pokaż Phalkowi przechwycony rozkaz. Powiedz: rozkaz dla straży.";
            return "Zanieś meldunek Drilenunowi do ratusza Semos. Powiedz: druga zmiana.";
        default: throw new IllegalArgumentException("Not a mine quest: " + step);
        }
    }
    static List<String> history(Player player, Step step) {
        List<String> entries = new ArrayList<>();
        if (!player.hasQuest(step.slot)) return entries;
        String state = player.getQuest(step.slot, 0); boolean done = player.isQuestCompleted(step.slot);
        switch (step) {
        case MINE_LEDGER:
            entries.add("W księdze Radana są zapasy dla nocnej zmiany w kopalni Semos. Nie ma nazwiska odbiorcy, tylko dopisek: dostarczyć po ostatnim wózku.");
            if (!"barbarus".equals(state)) entries.add("Barbarus kończy pracę przed nocą. Kazał sprawdzić rejestr, który prowadzi przy swoim stanowisku.");
            if ("report".equals(state) || done) entries.add("W rejestrze nie ma nocnego wydobycia. Ilość węgla zgadza się z pracą dziennej zmiany.");
            if (done) entries.add("Zapasy nie trafiały do ludzi Barbarusa. Ktoś korzystał z kopalni, ale nie po to, żeby wydobywać węgiel.");
            break;
        case MINE_TRACKS:
            entries.add("Barbarus wskazał zakręt, przy którym znajdował resztki jedzenia. Trzeba sprawdzić, kto tam przechodził.");
            int mask = clues(player);
            if ((mask&1)!=0) entries.add("Skrzynka po racjach ma taki sam znak przewozowy jak dostawy Radana. Porcje odliczano dla grupy ludzi.");
            if ((mask&2)!=0) entries.add("Ślady ciężkich podkutych butów idą parami, w równych odstępach. Prowadzą ku północno-wschodniej części kopalni.");
            if (done) entries.add("Barbarus nie poznaje tych śladów. Przypomniał sobie głosy dochodzące zza starej obudowy szybu, niedaleko miejsca pilnowanego przez Phalka.");
            break;
        case MINE_VOICES:
            entries.add("Barbarus słyszał krzyki i rozkazy w północno-wschodniej części kopalni.");
            if (!"listen".equals(state)) entries.add("Za skałą słychać równe kroki, a potem komendę: druga zmiana do bramy, zapasy na dół. Głosy cichną po krótkim sygnale.");
            if (done) entries.add("Zynn rozpoznał dawny sposób zmieniania warty. Wskazał Phalka, który pilnuje wejścia do głębszych korytarzy. To on może znać ludzi noszących rozkazy.");
            break;
        case MINE_DISPATCH:
            entries.add("Phalk widział uzbrojonego duergara przenoszącego pakiety. Wskazał zakręt na południe od swojego stanowiska. Nie trzeba przechodzić przez zablokowane wejście.");
            if (!"guard".equals(state)) entries.add("Łącznik został pokonany. Można sprawdzić pakiet z rozkazem.");
            if ("identified".equals(state) || "return".equals(state) || done) entries.add("Rozkaz każe odbierać zapasy Radana i utrzymywać wartę przy starej bramie. Jest na nim znak duergarów.");
            if ("return".equals(state) || done) entries.add("Phalk potwierdził pochodzenie rozkazu. Duergarowie są blisko zwykłych wyrobisk. Trzeba ostrzec straż w Semos.");
            if (done) entries.add("Drilenun dostał meldunek. Kupcy poznają fałszywe zamówienia, a Barbarus zostanie ostrzeżony. Nadal nie wiadomo, ilu duergarów jest za starą bramą.");
            break;
        default: throw new IllegalArgumentException("Not a mine quest: " + step);
        }
        return entries;
    }
}
