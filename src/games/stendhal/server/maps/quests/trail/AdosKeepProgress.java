/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Personal evidence and combat checkpoints for the upper abandoned keep. */
final class AdosKeepProgress {
    static final String OUTSIDE="0_ados_mountain_n2_w", KEEP="-1_ados_abandoned_keep";
    static final String GUARDIAN="strażnik warowni";
    static final int GUARDIAN_X=45, GUARDIAN_Y=108;
    private AdosKeepProgress() { }
    static boolean active(Player player, Step step) {
        return player!=null && player.hasQuest(step.slot) && !player.isQuestCompleted(step.slot)
                && player.isQuestCompleted(step.previousSlot());
    }
    static int mask(Player player, Step step) {
        if (player.isQuestCompleted(step.slot) || player.isQuestInState(step.slot,"report")) return 3;
        String value=player.getQuest(step.slot,1);
        return "1".equals(value)?1:"2".equals(value)?2:0;
    }
    static boolean mark(Player player, Step step, int bit, String state) {
        if (!active(player,step) || !player.isQuestInState(step.slot,0,state)) return false;
        int previous=mask(player,step), progress=previous|bit;
        if (previous==progress) return false;
        player.setQuest(step.slot,progress==3?"report":state+";"+progress);
        return true;
    }
    static boolean needsGuardian(Player player) {
        return active(player,Step.KEEP_GUARDIAN) && player.isQuestInState(Step.KEEP_GUARDIAN.slot,"fight");
    }
    static void recordGuardianKill(Player player, RPEntity victim) {
        if (!(victim instanceof AdosKeepGuardian) || !needsGuardian(player) || victim.getZone()==null
                || player.getZone()!=victim.getZone() || !KEEP.equals(victim.getZone().getName()) || !player.nextTo(victim,16)) return;
        CreatureRespawnPoint source=((AdosKeepGuardian)victim).getRespawnPoint();
        if (source==null || source.getZone()!=victim.getZone() || source.getX()!=GUARDIAN_X || source.getY()!=GUARDIAN_Y) return;
        player.setQuest(Step.KEEP_GUARDIAN.slot,"report");
        player.sendPrivateText("Kamienny strażnik upadł i znieruchomiał. Wróć do Zogfanga i opowiedz, co stało się w południowej sali.");
    }
    static String objective(Player player, Step step) {
        String state=player.getQuest(step.slot,0); int seen=mask(player,step);
        switch(step) {
        case KEEP_TRAIL:
            if ("report".equals(state)) return "Wróć do Juliusa przy głównym wejściu do Ados. Powiedz: stara warownia.";
            return "Sprawdź "+((seen&1)==0?"ciężkie ślady przy wejściu do warowni (13,122)":"")
                    +(seen==0?" oraz ":"")+((seen&2)==0?"odłamek kamiennej płyty obok (20,119)":"")
                    +" w górach na północny zachód od Ados. Wejście do warowni jest na południowym skraju tej mapy.";
        case KEEP_WITNESSES:
            if ("report".equals(state)) return "Przekaż Juliusowi relacje mieszkańców. Powiedz: uciekinierzy.";
            return "Porozmawiaj o hałasach w warowni "+((seen&1)==0?"z Zogfangiem w południowo-zachodniej części":"")
                    +(seen==0?" oraz ":"")+((seen&2)==0?"z Hagnurkiem w północno-wschodniej części":"")+" pierwszego piętra warowni. Nie schodź na niższe piętra.";
        case KEEP_INSCRIPTIONS:
            if ("report".equals(state)) return "Wróć do Wikipediana w bibliotece Ados. Powiedz: stare napisy.";
            return "Odczytaj "+((seen&1)==0?"stary napis w południowo-zachodniej sali (22,105)":"")
                    +(seen==0?" oraz ":"")+((seen&2)==0?"napis w sali dalej na wschód (65,105)":"")+" na pierwszym piętrze warowni.";
        case KEEP_GUARDIAN: return "report".equals(state)
                ? "Wróć do Zogfanga i opowiedz o pokonaniu strażnika. Powiedz: kamienny strażnik."
                : "Pokonaj strażnika warowni w środkowej sali na południu pierwszego piętra (45,108). Pozostań w pobliżu podczas walki.";
        case KEEP_RETURN: return "ready".equals(state)
                ? "Odbierz podziękowanie od Juliusa przy wejściu do Ados. Zostaw miejsce w plecaku. Powiedz: droga do Ados."
                : "Zapytaj Hagnurka w północno-wschodniej części pierwszego piętra warowni, czy droga jest bezpieczna. Powiedz: bezpieczna droga.";
        default: throw new IllegalArgumentException("Not an Ados keep quest");
        }
    }
    static List<String> history(Player player, Step step) {
        List<String> result=new ArrayList<>();
        if (!player.hasQuest(step.slot)) return result;
        result.add(step.description); int seen=mask(player,step); boolean done=player.isQuestCompleted(step.slot);
        switch(step) {
        case KEEP_TRAIL:
            if ((seen&1)!=0) result.add("Ciężkie ślady prowadzą od schodów na drogę. Bagaże porzucono pośpiesznie, bez śladów walki.");
            if ((seen&2)!=0) result.add("Odłamek jest ciepły i pokryty znakami. Wokół są kamienne odpryski, ale nie ma śladów ognia.");
            if (done) result.add("Julius chce wysłuchać mieszkańców warowni, zanim uzna ich za napastników."); break;
        case KEEP_WITNESSES:
            if ((seen&1)!=0) result.add("Zogfang słyszał ciężkie kroki. Kamienna postać rozbiła beczkę i ruszyła na orków.");
            if ((seen&2)!=0) result.add("Hagnurk stracił część towaru. Na kamiennych płytach są takie same znaki jak na odłamku spod wejścia.");
            if (done) result.add("Julius skierował mnie do Wikipediana. Podejrzewa dawny mechanizm obronny, a nie przygotowania do najazdu."); break;
        case KEEP_INSCRIPTIONS:
            if ((seen&1)!=0) result.add("Na pierwszej płycie jest rozkaz ochrony załogi warowni i zakaz wypuszczania napastników.");
            if ((seen&2)!=0) result.add("Druga płyta wymienia znaki dawnej załogi. Część napisu odpadła. Dzisiejszych mieszkańców strażnik bierze za intruzów.");
            if (done) result.add("Wikipedian nie potrafi naprawić uszkodzonego strażnika. Zogfang może wskazać miejsce, w którym stoi kamienna postać."); break;
        case KEEP_GUARDIAN:
            if ("report".equals(player.getQuest(step.slot,0)) || done) result.add("Strażnik warowni został pokonany w południowej sali pierwszego piętra.");
            if (done) result.add("Zogfang potwierdził, że mieszkańcy nie planowali najazdu. Trzeba przekazać Juliusowi wynik wyprawy."); break;
        case KEEP_RETURN:
            if ("ready".equals(player.getQuest(step.slot,0)) || done) result.add("Hagnurk potwierdził, że ciężkie kroki ucichły i można wrócić do przewożenia towaru.");
            if (done) result.add("Julius dostał pełną relację. Podarował mi rzadki hełm chaosu. Sprawa warowni jest zakończona."); break;
        default: throw new IllegalArgumentException("Not an Ados keep quest");
        }
        return result;
    }
}
