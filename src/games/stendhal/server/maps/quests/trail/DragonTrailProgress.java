/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Personal evidence, freed guide and restored clutch on the shared Dragon Lands. */
final class DragonTrailProgress {
    static final String SOUTH="0_dragon_land_s", NORTH="0_dragon_land_n", TOWER="int_dragon_wizard_tower_4";
    static final String SURVIVOR="Radowit", GUIDE="Ostrobor";
    enum Encounter {
        CAPTOR("strażnik łowców jaj",99,46,Step.EMBER_GUIDE,"guide"),
        CHIEF("herszt łowców jaj",104,41,Step.EMBER_EGGS,"cache");
        final String name,next;final int x,y;final Step step;
        Encounter(String name,int x,int y,Step step,String next) {this.name=name;this.x=x;this.y=y;this.step=step;this.next=next;}
    }
    private DragonTrailProgress() { }
    static boolean active(Player player,Step step) {
        return player!=null&&player.hasQuest(step.slot)&&!player.isQuestCompleted(step.slot)&&player.isQuestCompleted(step.previousSlot());
    }
    static boolean advance(Player player,Step step,String before,String after) {
        if(!active(player,step)||!player.isQuestInState(step.slot,before))return false;
        player.setQuest(step.slot,after);return true;
    }
    static boolean needs(Player player,Encounter role) {return active(player,role.step)&&player.isQuestInState(role.step.slot,"fight");}
    static int traces(Player player) {
        String state=player.getQuest(Step.EMBER_CAMP.slot,0);
        if("witness".equals(state)||"report".equals(state)||"done".equals(state))return 3;
        String bits=player.getQuest(Step.EMBER_CAMP.slot,1);return "1".equals(bits)?1:"2".equals(bits)?2:0;
    }
    static boolean trace(Player player,int bit) {
        if(!active(player,Step.EMBER_CAMP)||!player.isQuestInState(Step.EMBER_CAMP.slot,0,"traces"))return false;
        int before=traces(player),after=before|bit;if(before==after)return false;
        player.setQuest(Step.EMBER_CAMP.slot,after==3?"witness":"traces;"+after);return true;
    }
    static void recordKill(Player player,RPEntity victim) {
        if(!(victim instanceof DragonTrailGuard))return;
        DragonTrailGuard guard=(DragonTrailGuard)victim;Encounter role=guard.role;
        if(!needs(player,role)||victim.getZone()==null||player.getZone()!=victim.getZone()
                ||!NORTH.equals(victim.getZone().getName())||!player.nextTo(victim,16))return;
        CreatureRespawnPoint source=guard.getRespawnPoint();
        if(source==null||source.getZone()!=victim.getZone()||source.getX()!=role.x||source.getY()!=role.y)return;
        player.setQuest(role.step.slot,role.next);
        player.sendPrivateText(role==Encounter.CAPTOR?"Strażnik nie zagrodzi już drogi. Ostrobor czeka przy obozowisku."
                :"Herszt nie obroni już ładunku. W skrzyni obok jego stanowiska zostały jaja i zapiski handlarzy.");
    }
    static String objective(Player p,Step step) {
        String state=p.getQuest(step.slot,0);
        switch(step) {
        case EMBER_CAMP:
            if("witness".equals(state))return "Porozmawiaj z Radowitem przy spalonym obozie na Smoczych Ziemiach S (31,118). Powiedz: spalony obóz.";
            if("report".equals(state))return "Wróć do Racirada przy południowym wejściu na Smocze Ziemie. Powiedz: spalony obóz.";
            int bits=traces(p);return "Obejrzyj "+((bits&1)==0?"nadpaloną skrzynię (28,119)":"")+(bits==0?" i ":"")
                    +((bits&2)==0?"wygasłe palenisko (34,119)":"")+" przy południowym wejściu na Smocze Ziemie.";
        case EMBER_TRANSPORT:return "cargo".equals(state)?"Sprawdź porzucony ładunek na Smoczych Ziemiach N (60,40), na południe od Robercika."
                :"Porozmawiaj z Robercikiem na Smoczych Ziemiach N (63,31). Powiedz: porzucony transport.";
        case EMBER_SCENT:return "mixture".equals(state)?"Sprawdź naczynie z płótnem obok Wizariusza na szczycie jego wieży (6,42)."
                :"Pokaż Wizariuszowi, co stało się z płótnem. Powiedz: zapach jaj.";
        case EMBER_GUIDE:return "fight".equals(state)?"Pokonaj strażnika łowców jaj przy obozowisku na Smoczych Ziemiach N (99,46)."
                :"Porozmawiaj z uwolnionym Ostroborem przy obozowisku na Smoczych Ziemiach N (99,43). Powiedz: pojmany przewodnik.";
        case EMBER_EGGS:
            if("fight".equals(state))return "Pokonaj herszta łowców jaj na Smoczych Ziemiach N (104,41).";
            return "cache".equals(state)?"Odzyskaj jaja ze skrzyni obok stanowiska herszta (107,41)."
                    :"Powiedz Ostroborowi, że jaja są odzyskane. Temat: skradziony lęg.";
        case EMBER_RETURN:
            if("wash".equals(state))return "Obmyj jaja przy naczyniu w obozie handlarzy (97,43), używając środka od Wizariusza.";
            if("traps".equals(state))return "Usuń pułapki przy dawnym lęgowisku w południowo-zachodniej części Smoczych Ziem N (25,110).";
            return "nest".equals(state)?"Odłóż jaja na wyściółkę gniazda na Smoczych Ziemiach N (27,107)."
                    :"Wróć do Racirada przy południowym wejściu na Smocze Ziemie. Powiedz: powrót do gniazda.";
        default:throw new IllegalArgumentException("Not a dragon story quest");
        }
    }
    static List<String> history(Player p,Step step) {
        List<String> rows=new ArrayList<>();if(!p.hasQuest(step.slot))return rows;
        rows.add(step.description);String state=p.getQuest(step.slot,0);boolean done=p.isQuestCompleted(step.slot);
        switch(step) {
        case EMBER_CAMP:
            int bits=traces(p);
            if((bits&1)!=0)rows.add("W nadpalonej skrzyni został fragment smoczej skorupy, owinięty zabrudzonym płótnem.");
            if((bits&2)!=0)rows.add("Ogień zaczął się przy rozładowywanym wozie. Ludzie byli tu przed atakiem smoków.");
            if("report".equals(state)||done)rows.add("Radowit widział, jak woźnica porzucił skrzynię i uciekł, zanim nadleciały smoki.");break;
        case EMBER_TRANSPORT:
            if(!"cargo".equals(state))rows.add("Porzucony ładunek zawierał słomę, skorupy i płótno nasączone ciemną mazią.");
            if(done)rows.add("Robercik widział człowieka nacierającego płótno. Wizariusz może rozpoznać tę substancję.");break;
        case EMBER_SCENT:
            if(!"mixture".equals(state))rows.add("Ciepła woda wypłukała z płótna żywicę zmieszaną ze smoczą krwią.");
            if(done)rows.add("Wizariusz przygotował środek do obmycia jaj. Zapach przynęty prowadził smoki prosto do ludzkiego obozu.");break;
        case EMBER_GUIDE:
            if(!"fight".equals(state))rows.add("Strażnik łowców jaj został pokonany.");
            if(done)rows.add("Ostrobor został zmuszony do wskazania lęgowiska. Odmówił dalszej pomocy i handlarze go uwięzili.");break;
        case EMBER_EGGS:
            if(!"fight".equals(state))rows.add("Herszt łowców jaj został pokonany.");
            if("report".equals(state)||done)rows.add("Jaja i zapiski handlarzy są odzyskane. Podrzucali lęg przy osadach, by smoki zajęły się ludźmi i opuściły gniazda.");break;
        case EMBER_RETURN:
            if(!"wash".equals(state))rows.add("Przynęta została zmyta ze skorup i opakowania.");
            if("nest".equals(state)||"report".equals(state)||done)rows.add("Sidła przy lęgowisku zostały rozbrojone.");
            if("report".equals(state)||done)rows.add("Jaja wróciły do gniazda. Można wrócić do Racirada i powstrzymać odwet.");
            if(done)rows.add("Racirad odwołał wyprawę z pochodniami. Będzie strzegł drogi, lecz nie ruszy na smocze gniazda.");break;
        default:throw new IllegalArgumentException("Not a dragon story quest");
        }
        return rows;
    }
}
