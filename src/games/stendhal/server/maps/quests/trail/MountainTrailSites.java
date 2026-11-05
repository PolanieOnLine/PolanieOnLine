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
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.MountainTrailProgress.Encounter;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Actors on existing maps; no map geometry or entrances are changed. */
public final class MountainTrailSites implements ZoneConfigurator {
    @Override public void configureZone(StendhalRPZone zone, Map<String,String> attributes) {
        if (MountainTrailProgress.MINE.equals(zone.getName())) {
            npc(zone,MountainTrailProgress.MINER,"minernpc",76,65,
                    "Uff... Daj mi chwilę odetchnąć. Nie mogę wrócić po innych #górników w takim stanie.",
                    "Oto Borzym. Roboczy kaftan ma podarty, a twarz ubrudzoną pyłem. Trzyma się za obolałą nogę.");
        } else if (TrailQuestLine.FOREST.equals(zone.getName())) {
            npc(zone,MountainTrailProgress.DRIVER,"seller3npc",89,65,
                    "Nie podchodź, bo jeszcze ciebie złapią! Te #więzy zaraz odetną mi ręce.",
                    "Oto woźnica Miłosz. Siedzi przy skrzyni z rękami skrępowanymi grubym sznurem.");
            zone.add(new Cargo());
            final AreaEntity supplies=new AreaEntity(1,1);
            supplies.setRPClass("block"); supplies.put("type","block"); supplies.put("class","questprop");
            supplies.put("name","zapasy oddziału"); supplies.put("tileset","item/pot/crate_small");
            supplies.put("tile_index",1); supplies.put("tileset_columns",2); supplies.put("z",8000);
            supplies.setDescription("Oto skrzynia z zapasami oddziału. Na płótnie pozostał znak trzech kółek.");
            supplies.setPosition(86,53); zone.add(supplies);
            for (Encounter role:Encounter.values()) {
                Creature template=SingletonRepository.getEntityManager().getCreature(role.creature);
                if (template==null) throw new IllegalStateException("Missing encounter creature: "+role.creature);
                zone.add(new CreatureRespawnPoint(zone,role.x,role.y,new MountainTrailGuard(template,role),1));
            }
        } else throw new IllegalArgumentException("Unknown mountain story zone: "+zone.getName());
    }
    private void npc(StendhalRPZone zone, String name, String sprite, int x, int y, String greeting, String description) {
        SpeakerNPC npc=new SpeakerNPC(name) {
            @Override protected void createDialog() {
                addGreeting(greeting);
                addGoodbye("Uważaj na siebie.");
            }
        };
        npc.setEntityClass(sprite); npc.setGender("M"); npc.setDescription(description);
        npc.setPosition(x,y); npc.setDirection(Direction.DOWN); zone.add(npc);
    }
    static final class Cargo extends AreaEntity implements UseListener {
        Cargo() {
            super(1,1); setRPClass("block"); put("type","block"); put("class","questuseable");
            put("name","skrzynia z ukrytą bronią"); put("tileset","item/pot/crate_small");
            put("tile_index",0); put("tileset_columns",2); put("z",8000); setPosition(90,65);
            setMenu("Sprawdź|Użyj"); setCursor("ACTIVITY");
            setDescription("Oto ciężka skrzynia. Spod płótna wystaje metalowe ostrze.");
        }
        @Override public boolean onUsed(RPEntity user) {
            if (!(user instanceof Player) || getZone()==null || user.getZone()!=getZone()
                    || !TrailQuestLine.FOREST.equals(getZone().getName())) return false;
            Player player=(Player)user;
            if (!player.nextTo(this)) { player.sendPrivateText("Musisz podejść bliżej."); return false; }
            if (!MountainTrailProgress.active(player,Step.MOUNTAIN_CARGO) || !player.isQuestInState(Step.MOUNTAIN_CARGO.slot,"cargo")) {
                player.sendPrivateText("Oglądasz skrzynię, ale nie znajdujesz niczego nowego."); return false;
            }
            player.setQuest(Step.MOUNTAIN_CARGO.slot,"stasek");
            player.sendPrivateText("Pod płótnem leżą groty i ostrza, nie zużyte narzędzia. Na głowniach jest charakterystyczne nacięcie. "
                    + "Rysujesz znak, żeby pokazać go Staskowi. Miłosz miał zawieźć tę skrzynię do północnej części polany.");
            return true;
        }
    }
}
