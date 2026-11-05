/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Persisted progress; dialogue and shared scenery never consume another player's clues. */
final class MountainTrailProgress {
    static final String MINER = "Górnik Borzym", DRIVER = "Woźnica Miłosz";
    static final String MINE = MineTrailProgress.ZONE;
    enum Encounter {
        MEETING_HUMAN("wspólnik Radana", 91, 65, Step.MOUNTAIN_MEETING, 1),
        MEETING_DUERGAR("wysłannik duergarów", 93, 65, Step.MOUNTAIN_MEETING, 2),
        BATTLE_HUMAN("wspólnik Radana", 84, 54, Step.MOUNTAIN_BATTLE, 1),
        BATTLE_DUERGAR("wysłannik duergarów", 88, 54, Step.MOUNTAIN_BATTLE, 2),
        COMMANDER("dowódca duergarów", 80, 48, Step.MOUNTAIN_BATTLE, 4);
        final String creature;
        final int x, y, bit;
        final Step step;
        Encounter(String creature, int x, int y, Step step, int bit) {
            this.creature=creature; this.x=x; this.y=y; this.step=step; this.bit=bit;
        }
    }
    private MountainTrailProgress() { }
    static boolean active(Player player, Step step) {
        return player != null && player.hasQuest(step.slot) && !player.isQuestCompleted(step.slot)
                && player.isQuestCompleted(step.previousSlot());
    }
    static int mask(Player player, Step step) {
        String value=player.getQuest(step.slot, 1);
        try { return value == null ? 0 : Integer.parseInt(value) & 7; }
        catch (NumberFormatException invalid) { return 0; }
    }
    static boolean needs(Player player, Encounter role) {
        return active(player,role.step) && player.isQuestInState(role.step.slot,0,
                role.step==Step.MOUNTAIN_MEETING ? "guards" : "battle") && (mask(player,role.step)&role.bit)==0;
    }
    static void recordKill(Player player, RPEntity victim) {
        if (!(victim instanceof MountainTrailGuard)) return;
        MountainTrailGuard guard=(MountainTrailGuard)victim;
        Encounter role=guard.role;
        if (!needs(player,role) || victim.getZone()==null || player.getZone()!=victim.getZone()
                || !TrailQuestLine.FOREST.equals(victim.getZone().getName()) || !player.nextTo(victim,16)) return;
        CreatureRespawnPoint source=guard.getRespawnPoint();
        if (source==null || source.getZone()!=victim.getZone() || source.getX()!=role.x || source.getY()!=role.y) return;
        int progress=mask(player,role.step)|role.bit;
        boolean meeting=role.step==Step.MOUNTAIN_MEETING;
        player.setQuest(role.step.slot, progress==(meeting?3:7) ? (meeting?"rescue":"report")
                : (meeting?"guards;":"battle;")+progress);
        if (progress==(meeting?3:7)) player.sendPrivateText(meeting
                ? "Obaj strażnicy pokonani. Podejdź do woźnicy Miłosza i pomóż mu rozwiązać więzy."
                : "Dowódca i jego ludzie zostali pokonani. Wróć do Brzezdoma i opowiedz o walce.");
    }
    static int mark(Player player, Step step, int bit, String prefix, String finished, int all) {
        int progress=mask(player,step)|bit;
        player.setQuest(step.slot,progress==all ? finished : prefix+";"+progress);
        return progress;
    }
    static String objective(Player player, Step step) {
        String state=player.getQuest(step.slot,0);
        switch(step) {
        case MOUNTAIN_MINERS: return "search".equals(state)
                ? "Odszukaj górnika Borzyma w bocznym tunelu kopalni Semos, na północny wschód od Barbarusa (76,65). Zapytaj o górników."
                : "Wróć do Barbarusa i opowiedz o odnalezionym górniku. Powiedz: zaginieni górnicy.";
        case MOUNTAIN_THREATS:
            if ("report".equals(state)) return "Wróć do Gazdy Wojtka w ratuszu Zakopanego C. Powiedz: pogróżki.";
            return "Porozmawiaj o pogróżkach " + ((mask(player,step)&1)==0?"ze Staskiem w sklepie w Zakopanem C":"")
                    + (mask(player,step)==0?" oraz ":"") + ((mask(player,step)&2)==0?"z Jagną w karczmie w Zakopanem NE":"") + ".";
        case MOUNTAIN_MEETING:
            if ("rescue".equals(state)) return "Uwolnij woźnicę Miłosza na wschodzie polany w Kościelisku (89,65). Powiedz: więzy.";
            if ("report".equals(state)) return "Wróć do Brzezdoma z wieściami o woźnicy. Powiedz: obcy na polanie.";
            return "Pokonaj " + remaining(player,step) + " przy woźnicy, na wschodzie polany w Kościelisku (91–93,65).";
        case MOUNTAIN_CARGO:
            if ("cargo".equals(state)) return "Sprawdź skrzynię z ukrytą bronią obok woźnicy we wschodnim Kościelisku (90,65).";
            if ("stasek".equals(state)) return "Pokaż Staskowi znak z głowni znalezionej broni. Powiedz: ukryta broń.";
            return "Zanieś wieści o broni Drilenunowi w ratuszu Semos. Powiedz: transport broni.";
        case MOUNTAIN_BATTLE: return "report".equals(state)
                ? "Wróć do Brzezdoma i opowiedz o rozbiciu oddziału. Powiedz: zbiórka oddziału."
                : "Pokonaj " + remaining(player,step) + " w północnej części polany we wschodnim Kościelisku (80–88,48–54).";
        case MOUNTAIN_RETURN:
            if ("ready".equals(state)) return "Odbierz podziękowanie od Gazdy Wojtka w ratuszu Zakopanego C. Zostaw miejsce w plecaku. Powiedz: wieści z lasu.";
            List<String> recipients=new ArrayList<>(); int known=mask(player,step);
            if ((known&1)==0) recipients.add("Gazdą Wojtkiem w ratuszu Zakopanego C");
            if ((known&2)==0) recipients.add("Drilenunem w ratuszu Semos");
            if ((known&4)==0) recipients.add("Barbarusem w kopalni Semos");
            return "Porozmawiaj o wieściach z lasu z: " + String.join(", ",recipients) + ".";
        default: throw new IllegalArgumentException("Not a mountain quest");
        }
    }
    private static String remaining(Player player, Step step) {
        int defeated=mask(player,step);
        List<String> names=new ArrayList<>();
        if ((defeated&1)==0) names.add("wspólnika Radana");
        if ((defeated&2)==0) names.add("wysłannika duergarów");
        if (step==Step.MOUNTAIN_BATTLE && (defeated&4)==0) names.add("dowódcę duergarów");
        return String.join(" oraz ",names);
    }
    static List<String> history(Player player, Step step) {
        List<String> entries=new ArrayList<>();
        if (!player.hasQuest(step.slot)) return entries;
        entries.add(step.description);
        boolean done=player.isQuestCompleted(step.slot);
        switch(step) {
        case MOUNTAIN_MINERS:
            if (!player.isQuestInState(step.slot,"search")) entries.add("Borzym uciekł duergarom. Drugi górnik, Oskar, wydostał się wcześniej. Napastnicy rozmawiali o wspólnikach w Kościelisku.");
            if (done) entries.add("Barbarus posłał ludzi po Borzyma. Radzi ostrzec Gazdę Wojtka."); break;
        case MOUNTAIN_THREATS:
            int statements=done||player.isQuestInState(step.slot,"report")?3:mask(player,step);
            if ((statements&1)!=0) entries.add("Stasek rozpoznał człowieka, który groził mu za rozmowę o węglu. To dawny wspólnik Radana.");
            if ((statements&2)!=0) entries.add("Jagna widziała go z uzbrojonym towarzyszem. Wypytywali o Brzezdoma i drogę przez polanę.");
            if (done) entries.add("Wojtek przypilnuje mieszkańców. Brzezdom może wiedzieć, gdzie zatrzymali się przybysze."); break;
        case MOUNTAIN_MEETING:
            if ("rescue".equals(player.getQuest(step.slot,0)) || "report".equals(player.getQuest(step.slot,0)) || done) entries.add("Obaj ludzie pilnujący woźnicy zostali pokonani.");
            if ("report".equals(player.getQuest(step.slot,0)) || done) entries.add("Miłosz jest wolny. Miał przewieźć skrzynię jako zwykłe żelastwo, ale zobaczył w środku ostrza i groty."); break;
        case MOUNTAIN_CARGO:
            if (!player.isQuestInState(step.slot,"cargo")) entries.add("Pod płótnem były groty i ostrza. W drewnie skrzyni wypalono znak, a na głowniach jest nacięcie dawnego dostawcy Staska.");
            if ("briefing".equals(player.getQuest(step.slot,0)) || done) entries.add("Stasek rozpoznał broń skupowaną przez Radana. Miłosz miał ruszyć nią do północnej części polany.");
            if (done) entries.add("Drilenun ostrzegł Wojtka. Grupa szykuje napady na transporty do Zakopanego i Semos."); break;
        case MOUNTAIN_BATTLE:
            if ("report".equals(player.getQuest(step.slot,0)) || done) entries.add("Dowódca duergarów i obaj jego ludzie zostali pokonani. W skrzyni oddziału były zapasy na wyprawę.");
            if (done) entries.add("Brzezdom zostaje przy polanie do przybycia straży. Trzeba powiadomić oba miasta i Barbarusa."); break;
        case MOUNTAIN_RETURN:
            int reported=done||player.isQuestInState(step.slot,"ready")?7:mask(player,step);
            if ((reported&1)!=0) entries.add("Gazda Wojtek wie o rozbiciu oddziału. Kupcy będą mogli znów wozić zapasy.");
            if ((reported&2)!=0) entries.add("Drilenun dostał wieści z lasu i przejmie sprawę ocalałych wspólników Radana.");
            if ((reported&4)!=0) entries.add("Barbarus wie, kto napadł na górników. Borzym zaczeka na pomoc, Oskar jest już bezpieczny.");
            if (done) entries.add("Wojtek podarował mi rzadką cuhę góralską. Sprawa ludzi Radana jest zakończona."); break;
        default: throw new IllegalArgumentException("Not a mountain quest");
        }
        return entries;
    }
}
