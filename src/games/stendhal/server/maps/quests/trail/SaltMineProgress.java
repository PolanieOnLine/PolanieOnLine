/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Personal rescue and drainage checkpoints; never reset another visitor's mine. */
final class SaltMineProgress {
    static final String MINE="-1_wieliczka_salt_mines_c", FOREMAN="Radost", MAYOR="Zbigniew", SCHOLAR="Agrypin";
    static final String BOGDAN="górnik Bogdan", WOJMIR="górnik Wojmir";
    enum Encounter {
        SEEP("solny szlam",50,82,Step.SALT_RESCUE,"miners;0"),
        SOURCE("żywiołak solanki",65,80,Step.SALT_CREATURE,"seal");
        final String name,next;final int x,y;final Step step;
        Encounter(String name,int x,int y,Step step,String next) {
            this.name=name;this.x=x;this.y=y;this.step=step;this.next=next;
        }
    }
    private SaltMineProgress() { }
    static boolean active(Player player,Step step) {
        return player!=null&&player.hasQuest(step.slot)&&!player.isQuestCompleted(step.slot)
                &&player.isQuestCompleted(step.previousSlot());
    }
    static boolean advance(Player player,Step step,String before,String after) {
        if(!active(player,step)||!player.isQuestInState(step.slot,before))return false;
        player.setQuest(step.slot,after);return true;
    }
    static boolean needs(Player player,Encounter role) {
        return active(player,role.step)&&player.isQuestInState(role.step.slot,"fight");
    }
    static int mask(Player player,Step step,String terminal) {
        String state=player.getQuest(step.slot,0);
        if("done".equals(state)||terminal.equals(state))return 3;
        String bits=player.getQuest(step.slot,1);
        return "1".equals(bits)?1:"2".equals(bits)?2:0;
    }
    static boolean mark(Player player,Step step,String before,int bit,String terminal) {
        if(!active(player,step)||!player.isQuestInState(step.slot,0,before))return false;
        int old=mask(player,step,terminal),next=old|bit;
        if(old==next)return false;
        player.setQuest(step.slot,next==3?terminal:before+";"+next);return true;
    }
    static void recordKill(Player player,RPEntity victim) {
        if(!(victim instanceof SaltMineGuard))return;
        SaltMineGuard guard=(SaltMineGuard)victim;Encounter role=guard.role;
        if(!needs(player,role)||victim.getZone()==null||player.getZone()!=victim.getZone()
                ||!MINE.equals(victim.getZone().getName())||!player.nextTo(victim,16))return;
        CreatureRespawnPoint source=guard.getRespawnPoint();
        if(source==null||source.getZone()!=victim.getZone()||source.getX()!=role.x||source.getY()!=role.y)return;
        player.setQuest(role.step.slot,role.next);
        player.sendPrivateText(role==Encounter.SEEP
                ?"Szlam rozpływa się między kamieniami. Możesz podejść do Bogdana i Wojmira, żeby wskazać im bezpieczną drogę."
                :"Żywiołak traci kształt i opada w solankę. Możesz teraz zamknąć szczelinę, z której wypływa woda.");
    }
    static String objective(Player player,Step step) {
        String state=player.getQuest(step.slot,0);
        switch(step) {
        case SALT_SHIFT:
            if("foreman".equals(state))return "Porozmawiaj ze sztygarem Radostem przy wejściu do kopalni Wieliczki (34,80). Powiedz: zaginiona zmiana.";
            if("report".equals(state))return "Pokaż Radostowi ślady wstrząsu i wypływu. Powiedz: zaginiona zmiana.";
            int marks=mask(player,step,"report");
            return "Obejrzyj "+((marks&1)==0?"skrzynkę przy naruszonej podporze (38,89)":"")+(marks==0?" i ":"")
                    +((marks&2)==0?"mokre liny przy chodniku (42,81)":"")+" w centralnej części kopalni.";
        case SALT_RESCUE:
            if("brace".equals(state))return "Zabezpiecz strop przy skrzynce z rozpórkami (38,89).";
            if("fight".equals(state))return "Pokonaj solny szlam zastępujący drogę górnikom (50,82).";
            if("report".equals(state))return "Powiadom Radosta, że obaj górnicy dotrą do szybu. Powiedz: odcięci górnicy.";
            int miners=mask(player,step,"report");
            return "Wskaż drogę: "+((miners&1)==0?"Bogdanowi (43,79)":"")+(miners==0?" i ":"")
                    +((miners&2)==0?"Wojmirowi (40,54)":"")+". Powiedz: odcięci górnicy.";
        case SALT_SAMPLE:return "agrypin".equals(state)
                ?"Zanieś próbkę Agrypinowi w jego domu w Wieliczce. Powiedz: próbka solanki."
                :"Pobierz próbkę z wypływu w dalszej części chodnika (55,92).";
        case SALT_SOURCE:
            if("report".equals(state))return "Wyjaśnij Radostowi, którędy płynie woda. Powiedz: stary zbiornik.";
            int channels=mask(player,step,"report");
            return "Zbadaj "+((channels&1)==0?"zatkany odpływ (54,69)":"")+(channels==0?" i ":"")
                    +((channels&2)==0?"wykruszony próg kanału (67,53)":"")+". Nie otwieraj jeszcze spustu.";
        case SALT_CREATURE:
            if("seal".equals(state))return "Zamknij szczelinę starego zbiornika (63,82).";
            return "report".equals(state)?"Powiedz Radostowi, że dopływ jest zamknięty. Temat: żywiołak solanki."
                    :"Pokonaj żywiołaka solanki przy starym zbiorniku (65,80).";
        case SALT_RETURN:
            if("drain".equals(state))return "Otwórz spust awaryjny liną przy wypływie (54,92).";
            if("mayor".equals(state))return "Wróć do Zbigniewa w ratuszu Wieliczki. Powiedz: bezpieczny szyb.";
            int checked=mask(player,step,"mayor");
            return "Sprawdź "+((checked&1)==0?"podpartą belkę (38,89)":"")+(checked==0?" oraz ":"")
                    +((checked&2)==0?"drogę przy mokrych linach (42,81)":"")+", zanim wrócisz na powierzchnię.";
        default:throw new IllegalArgumentException("Not a salt mine quest");
        }
    }
    static List<String> history(Player player,Step step) {
        List<String> rows=new ArrayList<>();if(!player.hasQuest(step.slot))return rows;
        rows.add(step.description);String state=player.getQuest(step.slot,0);boolean done=player.isQuestCompleted(step.slot);
        switch(step) {
        case SALT_SHIFT:
            if(!"foreman".equals(state))rows.add("Radost szuka Bogdana i Wojmira. Kazał sprawdzić ślady wypływu i stan podpór.");
            int marks=mask(player,step,"report");
            if((marks&1)!=0)rows.add("Podpora odsunęła się od stropu. Wstrząs uszkodził przejście do górników.");
            if((marks&2)!=0)rows.add("Liny są nasiąknięte solanką. Woda napływa z dalszych chodników.");break;
        case SALT_RESCUE:
            if(!"brace".equals(state))rows.add("Strop został podparty rozpórkami ze skrzynki.");
            if("miners".equals(state)||"report".equals(state)||done)rows.add("Solny szlam nie zastępuje już drogi.");
            int miners=mask(player,step,"report");
            if((miners&1)!=0)rows.add("Bogdan poznał bezpieczną drogę do szybu.");
            if((miners&2)!=0)rows.add("Wojmir poznał bezpieczną drogę do szybu.");break;
        case SALT_SAMPLE:
            if("agrypin".equals(state)||done)rows.add("Próbka została pobrana z wypływu w dalszym chodniku.");
            if(done)rows.add("Agrypin dostrzegł w próbce poruszające się drobiny. Wstrząs otworzył coś więcej niż zwykłą żyłę wody.");break;
        case SALT_SOURCE:
            int channels=mask(player,step,"report");
            if((channels&1)!=0)rows.add("Odpływ jest zatkany odłamkami soli. Woda nie ma dokąd odpłynąć.");
            if((channels&2)!=0)rows.add("Woda przelewa się przez pęknięty próg dawnego zbiornika.");break;
        case SALT_CREATURE:
            if(!"fight".equals(state))rows.add("Żywiołak został pokonany. Można podejść do szczeliny.");
            if("report".equals(state)||done)rows.add("Szczelina zbiornika została zamknięta. Nowa solanka nie zalewa już chodników.");break;
        case SALT_RETURN:
            if(!"drain".equals(state))rows.add("Spust odprowadzający nagromadzoną wodę został otwarty.");
            int checked=mask(player,step,"mayor");
            if((checked&1)!=0)rows.add("Rozpórki trzymają strop. Przejście nie wymaga dalszego podparcia.");
            if((checked&2)!=0)rows.add("Woda przy drodze do szybu opadła. Górnicy mogą wracać na powierzchnię.");
            if(done)rows.add("Zbigniew dostał wieści o uratowanych górnikach i zabezpieczonych chodnikach.");break;
        default:throw new IllegalArgumentException("Not a salt mine quest");
        }
        return rows;
    }
}
