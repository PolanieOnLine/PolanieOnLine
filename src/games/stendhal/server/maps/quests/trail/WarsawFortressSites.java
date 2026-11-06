/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.quests.trail.WarsawFortressProgress.Encounter;

/** All story positions live here so a revised Tiled layout can be wired in one place. */
public final class WarsawFortressSites implements ZoneConfigurator {
    enum Kind {
        LEDGER("księga wypłat",11,9,"item/documents/book",0,2,Step.WARSAW_PAY,"clues",null,1,
                "W księdze są pokwitowania odbioru żołdu. Kilka podpisów złożono tą samą ręką. Wśród odbiorców wymieniono żołnierzy zmarłych przed datą wypłaty. Przepisujesz podejrzane wpisy."),
        SUPPLIES("spis zapasów",44,36,"item/documents/notes",1,2,Step.WARSAW_PAY,"clues",null,2,
                "Spis wymienia dostawy odebrane od Radana. Obok kwot jest znak trzech kółek. Zapłatę wpisano do wydatków na żołd, choć w skrzyniach były broń i racje dla ludzi zarządcy."),
        ORDER("rozkaz podpalenia",44,27,"item/documents/notes",1,2,Step.WARSAW_FIRE,"orders","supplies",0,
                "Rozkaz nakazuje rozlać olej w kancelarii i magazynie, gdy zacznie się szturm. Rannych nie wolno wypuszczać z lazaretu. Pod spodem jest podpis Radomira. Zabierasz odpis dla Dobrosławy."),
        OIL("beczka z olejem",48,37,"item/pot/barrel",0,2,Step.WARSAW_FIRE,"supplies","report",0,
                "Wynosisz olej do pustego rowu za murem, z dala od ognia. Opróżniasz beczki i łamiesz naczynia przygotowane do rozlewania. Tego zapasu nikt już nie użyje do podpalenia warowni."),
        PAPERS("zapiski Radomira",28,6,"item/documents/book",1,2,Step.WARSAW_STEWARD,"documents","report",0,
                "W zapiskach Radomir wyliczył zatrzymany żołd i zapłatę dla ludzi Radana. Ostatni wpis każe spalić księgi razem z kancelarią. Zabezpieczasz papiery dla księcia."),
        GATE("lina bramy",30,41,"item/ropes",0,2,Step.WARSAW_TRUCE,"gate","prince",0,
                "Mocujesz linę i zabezpieczasz bramę przed zatrzaśnięciem. Wszemił przeprowadza rannych do obozu. Mark czeka po drugiej stronie, ale los załogi nadal zależy od odpowiedzi księcia.");
        final String name,tileset,state,next,text; final int x,y,tile,columns,bit;final Step step;
        Kind(String name,int x,int y,String tileset,int tile,int columns,Step step,String state,String next,int bit,String text) {
            this.name=name;this.x=x;this.y=y;this.tileset=tileset;this.tile=tile;this.columns=columns;
            this.step=step;this.state=state;this.next=next;this.bit=bit;this.text=text;
        }
    }
    @Override public void configureZone(StendhalRPZone zone,Map<String,String> attributes) {
        if (!WarsawFortressProgress.ZONE.equals(zone.getName())) throw new IllegalArgumentException("Unknown Warsaw fortress");
        for (Kind kind:Kind.values()) zone.add(new Evidence(kind));
        for (Encounter role:Encounter.values()) {
            Creature template=SingletonRepository.getEntityManager().getCreature(role.name);
            if (template==null) throw new IllegalStateException("Missing fortress opponent: "+role.name);
            zone.add(new CreatureRespawnPoint(zone,role.x,role.y,new WarsawFortressGuard(template,role),1));
        }
        SpeakerNPC healer=new SpeakerNPC(WarsawFortressProgress.HEALER) {
            @Override protected void createDialog() {
                add(ConversationStates.IDLE,ConversationPhrases.GREETING_MESSAGES,null,ConversationStates.ATTENDING,null,
                        (p,s,n)->n.say(p.isQuestCompleted(Step.WARSAW_TRUCE.slot)
                                ?"Dobrze cię widzieć. Ranni są już bezpieczni. Tutaj opatruję tych, którzy powoli wracają do służby."
                                :"Uważaj, gdzie stawiasz nogi. Nie wszyscy moi podopieczni mogą wstać z posłania. Jeśli przysyła cię Mark, powiedz, z czym przychodzisz."));
                addJob("Opatruję rannych. Rana nie pyta, po której stronie bramy ktoś stał.");
                addHelp("Jeśli przychodzisz od Marka, porozmawiajmy, #'zanim ruszy szturm'.");
                addGoodbye("Uważaj na siebie.");
            }
        };
        healer.setEntityClass("welcomernpc");healer.setGender("F");healer.setPosition(44,10);healer.setDirection(Direction.DOWN);
        healer.setDescription("Oto Dobrosława. Niestrudzenie opiekuje się rannymi mieszkańcami warowni.");zone.add(healer);
        SpeakerNPC soldier=new SpeakerNPC(WarsawFortressProgress.SOLDIER) {
            @Override protected void createDialog() {
                add(ConversationStates.IDLE,ConversationPhrases.GREETING_MESSAGES,null,ConversationStates.ATTENDING,null,
                        (p,s,n)->n.say(p.isQuestCompleted(Step.WARSAW_TRUCE.slot)
                                ?"Nie zapomniałem tego, co dla nas zrobiono. Książę dotrzymał słowa i wysłuchał każdego z osobna."
                                :"Jeśli chcesz walczyć, znajdziesz wielu chętnych. Ja mam już dość. Chcę wiedzieć, czy moi ludzie doczekają jutra."));
                addJob("Dowodziłem jedną z wart. Teraz pilnuję, żeby nie doszło tu do kolejnej bójki.");
                addHelp("O #'ludziach w środku' można powiedzieć wiele. Większość wolałaby dziś siedzieć w domu.");
                addGoodbye("Nie daj się sprowokować.");
            }
        };
        soldier.setEntityClass("youngsoldiernpc");soldier.setGender("M");soldier.setPosition(14,23);soldier.setDirection(Direction.DOWN);
        soldier.setDescription("Oto Wszemił. Zmęczony dowódca warty czuwa przy swoich ludziach.");zone.add(soldier);
    }
    static final class Evidence extends AreaEntity implements UseListener {
        final Kind kind;
        Evidence(Kind kind) {
            super(1,1);this.kind=kind;setRPClass("block");put("type","block");put("class","questuseable");
            put("name",kind.name);put("tileset",kind.tileset);put("tile_index",kind.tile);put("tileset_columns",kind.columns);put("z",8000);
            setPosition(kind.x,kind.y);setCursor("ACTIVITY");setMenu("Sprawdź|Użyj");setDescription("Oto "+kind.name+".");
        }
        @Override public boolean onUsed(RPEntity user) {
            if (!(user instanceof Player) || getZone()==null || user.getZone()!=getZone()
                    || !WarsawFortressProgress.ZONE.equals(getZone().getName())) return false;
            Player player=(Player)user;
            if (!player.nextTo(this)) {player.sendPrivateText("Musisz podejść bliżej.");return false;}
            boolean changed=kind.bit==0?WarsawFortressProgress.advance(player,kind.step,kind.state,kind.next)
                    :WarsawFortressProgress.mark(player,kind.step,kind.state,kind.bit);
            player.sendPrivateText(changed?kind.text:"Nie znajdujesz tu niczego nowego. Zajrzyj do dziennika, jeśli nie wiesz, co pozostało do zrobienia.");
            return changed;
        }
    }
}
