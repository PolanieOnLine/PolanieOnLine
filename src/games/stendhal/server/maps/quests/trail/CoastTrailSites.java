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
import games.stendhal.server.maps.quests.trail.CoastTrailProgress.Encounter;

/** Usable scenery on the existing beaches and cellar; no ordinary spawn markers change. */
public final class CoastTrailSites implements ZoneConfigurator {
    enum Kind {
        CRATE("skrzynia z Mewy",CoastTrailProgress.WEST,97,57,"item/pot/crate_small",1,2,Step.COAST_WRECK,"traces",null,1,
                "Na zgniecionej skrzyni widać nazwę Mewa. Sól wżarła się w deski, ale ładunek nie zdążył zgnić. Statek musiał rozbić się niedawno, blisko portu."),
        ROPE("strzępy olinowania",CoastTrailProgress.WEST,105,58,"item/ropes",3,2,Step.COAST_WRECK,"traces",null,2,
                "Lina jest postrzępiona od strony morza, lecz drugi koniec ucięto równo nożem. Ktoś odcinał ładunek, gdy wrak leżał już na mieliźnie. Dalej na plaży siedzi człowiek w mokrym ubraniu."),
        FALSE_LIGHT("palenisko na mieliźnie",CoastTrailProgress.EAST,105,62,"item/campfires",1,2,Step.COAST_LIGHTS,"lookout","fight",0,
                "Wokół paleniska leżą blaszane osłony. Kierowały światło w stronę morza, dokładnie ku mieliźnie. To nie ognisko rybaków. Człowiek pilnujący obozowiska sięga po broń."),
        CARGO("skrzynia z rozbitego statku",CoastTrailProgress.CELLAR,27,12,"item/pot/crate_small",1,2,Step.COAST_CARGO,"cargo","rosa",0,
                "Skrzynia jest mokra od słonej wody. Pod świeżym oznaczeniem składu widnieje nazwa Mewa i znak kupca znany z zapisków Radomira. Ktoś usiłuje sprzedać zabrany ze statku ładunek."),
        PAPERS("plan rabusiów wybrzeża",CoastTrailProgress.CELLAR,30,26,"item/documents/notes",1,2,Step.COAST_WRECKERS,"papers","report",0,
                "Plan wymienia Jaskółkę jako następny cel. Obok leżą skradzione osłony lamp i suche liny. Zabierasz je wraz z zapiskami, żeby przywrócić prawdziwy sygnał do portu."),
        RIGGING("linki lamp sygnałowych",CoastTrailProgress.EAST,56,60,"item/ropes",0,2,Step.COAST_LAMPS,"rigging","lamps;0",0,
                "Zastępujesz przecięte linki linami odzyskanymi w piwnicy. Mocowania trzymają. Możesz ustawić lampy: najpierw czerwoną po zachodniej stronie, potem zieloną po wschodniej."),
        WEST_LAMP("czerwona lampa sygnałowa",CoastTrailProgress.EAST,40,57,"furniture/light/ship_lamps",0,2,Step.COAST_LAMPS,"lamps;0","lamps;1",0,
                "Ustawiasz czerwoną lampę po zachodniej stronie brzegu. Osłona kieruje jej światło ku wejściu do portu. Teraz pora na zieloną lampę po wschodniej stronie."),
        EAST_LAMP("zielona lampa sygnałowa",CoastTrailProgress.EAST,75,57,"furniture/light/ship_lamps",1,2,Step.COAST_LAMPS,"lamps;1","report",0,
                "Ustawiasz zieloną lampę po wschodniej stronie. Obie wskazują bezpieczne przejście obok mielizny. Lubor powinien wiedzieć, że sygnał jest przygotowany."),
        SIGNAL("latarnia pilota",CoastTrailProgress.EAST,56,57,"furniture/light/small_lamp",0,4,Step.COAST_RETURN,"signal","lookout",0,
                "Odsłaniasz latarnię i dajesz umówiony błysk. Z morza nadchodzi odpowiedź pilota. Lubor patrzy na statek z plaży przy przystani promu. Zapytaj go o bezpieczny kurs.");
        final String name,zone,tileset,state,next,text;final int x,y,tile,columns,bit;final Step step;
        Kind(String name,String zone,int x,int y,String tileset,int tile,int columns,Step step,String state,String next,int bit,String text) {
            this.name=name;this.zone=zone;this.x=x;this.y=y;this.tileset=tileset;this.tile=tile;this.columns=columns;
            this.step=step;this.state=state;this.next=next;this.bit=bit;this.text=text;
        }
    }
    @Override public void configureZone(StendhalRPZone zone,Map<String,String> attributes) {
        if (!CoastTrailProgress.WEST.equals(zone.getName())&&!CoastTrailProgress.EAST.equals(zone.getName())
                &&!CoastTrailProgress.CELLAR.equals(zone.getName())) throw new IllegalArgumentException("Unknown coast story zone");
        for(Kind kind:Kind.values())if(kind.zone.equals(zone.getName()))zone.add(new Evidence(kind));
        for(Encounter role:Encounter.values())if(role.zone.equals(zone.getName())) {
            Creature template=SingletonRepository.getEntityManager().getCreature(role.name);
            if(template==null)throw new IllegalStateException("Missing coast opponent: "+role.name);
            zone.add(new CreatureRespawnPoint(zone,role.x,role.y,new CoastTrailGuard(template,role),1));
        }
        if(CoastTrailProgress.WEST.equals(zone.getName())) {
            SpeakerNPC sailor=new SpeakerNPC(CoastTrailProgress.SAILOR) {
                @Override protected void createDialog() {
                    add(ConversationStates.IDLE,ConversationPhrases.GREETING_MESSAGES,null,ConversationStates.ATTENDING,null,
                            (p,s,n)->n.say(p.isQuestCompleted(Step.COAST_RETURN.slot)
                                    ?"Jaskółka jest w porcie. Za każdym razem, gdy o tym myślę, łatwiej mi wrócić myślami do Mewy. Dziękuję za pomoc."
                                    :p.isQuestCompleted(Step.COAST_WRECK.slot)
                                    ?"Dobrze cię widzieć. Staszek przysłał już ludzi na brzeg. Byle nie trzeba było szukać kolejnej załogi."
                                    :"Żyję, choć jeszcze w to nie wierzę. Płynąłem Mewą. Jeśli przysyła cię Staszek, powiedz, czego udało się dowiedzieć."));
                    addJob("Jestem marynarzem. Znam ten brzeg od dziecka, a mimo to dałem się zwieść jednemu światłu.");
                    addHelp("Jeśli szukasz załogi, zapytaj o #'zaginioną Mewę'. Potem trzeba będzie wyjaśnić #'fałszywe światło'.");
                    addGoodbye("Patrz pod nogi. Morze wyrzuciło sporo ostrych desek.");
                }
            };
            sailor.setEntityClass("sailor1npc");sailor.setGender("M");sailor.setPosition(110,60);sailor.setDirection(Direction.DOWN);
            sailor.setDescription("Oto Lubor. Rozbitek odpoczywa blisko brzegu i nie spuszcza wzroku z morza.");zone.add(sailor);
        }
    }
    static final class Evidence extends AreaEntity implements UseListener {
        final Kind kind;
        Evidence(Kind kind) {
            super(1,1);this.kind=kind;setRPClass("block");put("type","block");put("class","questuseable");
            put("name",kind.name);put("tileset",kind.tileset);put("tile_index",kind.tile);put("tileset_columns",kind.columns);put("z",8000);
            setResistance(0);setPosition(kind.x,kind.y);setCursor("ACTIVITY");setMenu("Sprawdź|Użyj");setDescription("Oto "+kind.name+".");
        }
        @Override public boolean onUsed(RPEntity user) {
            if(!(user instanceof Player)||getZone()==null||user.getZone()!=getZone()||!kind.zone.equals(getZone().getName()))return false;
            Player player=(Player)user;
            if(!player.nextTo(this)){player.sendPrivateText("Musisz podejść bliżej.");return false;}
            boolean changed=kind.bit!=0?CoastTrailProgress.trace(player,kind.bit):CoastTrailProgress.advance(player,kind.step,kind.state,kind.next);
            String reminder="Nie znajdujesz tu niczego nowego. Dziennik przypomni ci, co zostało do zrobienia.";
            if(!changed&&CoastTrailProgress.active(player,Step.COAST_LAMPS)) {
                if(player.isQuestInState(Step.COAST_LAMPS.slot,"rigging"))reminder="Najpierw napraw linki pośrodku brzegu. Bez nich lampy nie utrzymają właściwego położenia.";
                else if(kind==Kind.EAST_LAMP&&player.isQuestInState(Step.COAST_LAMPS.slot,"lamps;0"))reminder="Lubor prosił, żeby zacząć od czerwonej lampy po zachodniej stronie. Niczego nie musisz ustawiać od nowa.";
            }
            player.sendPrivateText(changed?kind.text:reminder);return changed;
        }
    }
}
