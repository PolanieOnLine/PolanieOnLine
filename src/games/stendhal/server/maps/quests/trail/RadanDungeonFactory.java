/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.server.entity.mapstuff.sound.LoopedSoundSource;
import org.apache.log4j.Logger;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.creature.DomesticAnimal;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.ZoneEnterExitListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.mapstuff.portal.Teleporter;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.server.game.rp.InstanceZoneDescriptor;
import marauroa.server.game.rp.InstanceZoneFactory;

/** A private copy of the authored map; all mutable scenery lives in entities. */
final class RadanDungeonFactory implements InstanceZoneFactory {
    private static final Logger LOG = Logger.getLogger(RadanDungeonFactory.class);
    static final String TEMPLATE = "int_semos_radan_hideout";
    private final Player owner;
    RadanDungeonFactory(Player owner) { this.owner = owner; }
    @Override public IRPZone create(InstanceZoneDescriptor descriptor) {
        StendhalRPZone template = SingletonRepository.getRPWorld().getZone(TEMPLATE);
        if (template == null) throw new IllegalStateException("Missing Radan map template");
        StendhalRPZone zone = new StendhalRPZone(descriptor.getRuntimeZoneIdString(), template);
        zone.setPublicAccessible(false); zone.setMoveToAllowed(false); zone.disallowIn();
        zone.getAttributes().putAll(template.getAttributes().toMap());
        for (Entity entity : template.getEntitiesOfClass(LoopedSoundSource.class)) {
            LoopedSoundSource source = (LoopedSoundSource) entity;
            LoopedSoundSource copy = new LoopedSoundSource(source.get("sound"), source.getInt("radius"),
                    source.getInt("volume"), SoundLayer.values()[source.getInt("layer")]);
            copy.setPosition(source.getX(), source.getY()); zone.add(copy);
        }
        zone.getAttributes().put("readable_name", "Kryjówka Radana");
        zone.getAttributes().put("runtime_zone", zone.getName());
        AreaEntity admission = new AreaEntity(40, 28) {
            @Override public boolean isObstacle(Entity entity) {
                return entity instanceof Player && !owner.getName().equals(entity.getName());
            }
        };
        admission.hide(); zone.add(admission);
        StendhalRPZone outside = SingletonRepository.getRPWorld().getZone(RadanDungeonSite.PUBLIC_ZONE);
        if (outside == null) throw new IllegalStateException("Missing return zone");
        Teleporter exit = new Teleporter(new Spot(outside, RadanDungeonSite.RETURN_X, RadanDungeonSite.RETURN_Y));
        exit.setPosition(6, 24); zone.add(exit);
        new RadanDungeonScene(owner, zone).populate();
        // Zone exit also fires for logout and death. Save public coordinates before serialization.
        zone.addZoneEnterExitListener(new ZoneEnterExitListener() {
            @Override public void onEntered(RPObject object, StendhalRPZone current) { }
            @Override public void onExited(RPObject object, StendhalRPZone current) {
                if (object != owner) return;
                evacuate(current, outside);
                owner.put("zoneid", RadanDungeonSite.PUBLIC_ZONE);
                owner.put("x", RadanDungeonSite.RETURN_X); owner.put("y", RadanDungeonSite.RETURN_Y);
                try { SingletonRepository.getRPWorld().getInstanceZoneManager().release(current.getID(), owner.getName()); }
                catch (Exception exception) { LOG.error("Cannot release Radan instance " + current.getName(), exception); }
            }
        });
        return zone;
    }
    private void evacuate(StendhalRPZone zone, StendhalRPZone outside) {
        List<Entity> belongings = new ArrayList<>();
        for (RPObject object : zone) {
            if (object instanceof Item || object instanceof Corpse || object instanceof DomesticAnimal) belongings.add((Entity) object);
        }
        for (Entity entity : belongings) {
            // Death has already abandoned pets. Preserve that state, but leave them reachable.
            boolean moved = entity instanceof Corpse
                    ? ((Corpse) entity).relocateTo(outside, RadanDungeonSite.RETURN_X, RadanDungeonSite.RETURN_Y)
                    : StendhalRPAction.placeat(outside, entity, RadanDungeonSite.RETURN_X, RadanDungeonSite.RETURN_Y);
            if (!moved) {
                // The public entry tile is statically walkable. In a crowded map
                // temporary overlap is safer than discarding a pet or an item.
                if (entity instanceof DomesticAnimal) {
                    DomesticAnimal animal = (DomesticAnimal) entity;
                    animal.stopAttack(); animal.clearPath(); animal.stop();
                }
                zone.remove(entity); entity.setPosition(RadanDungeonSite.RETURN_X, RadanDungeonSite.RETURN_Y);
                outside.add(entity);
            }
        }
    }
    @Override public void destroy(InstanceZoneDescriptor descriptor, IRPZone instance) {
        StendhalRPZone zone = (StendhalRPZone) instance;
        List<RPObject> remove = new ArrayList<>();
        for (RPObject object : zone) {
            if (object instanceof RadanDungeonScene.Actor || object instanceof RadanDungeonScene.Fighter
                    || object instanceof Corpse) remove.add(object);
        }
        // Remove through the zone to cancel corpse timers and unregister private NPCs.
        for (RPObject object : remove) zone.remove(object);
    }
}
