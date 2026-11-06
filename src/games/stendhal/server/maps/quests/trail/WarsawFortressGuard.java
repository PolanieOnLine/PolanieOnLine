/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.WarsawFortressProgress.Encounter;

/** Story opponents engage only investigators on their current combat objective. */
final class WarsawFortressGuard extends Creature {
    final Encounter role;
    WarsawFortressGuard(Creature template,Encounter role) {
        super(template);this.role=role;setXP(0);clearDropItemList();
    }
    @Override public Creature getNewInstance() {return new WarsawFortressGuard(this,role);}
    private boolean eligible(RPEntity target) {
        return target instanceof Player && target.getZone()==getZone()
                && WarsawFortressProgress.needs((Player)target,role)
                && Math.abs(target.getX()-role.x)<=8 && Math.abs(target.getY()-role.y)<=8;
    }
    @Override public void logic() {
        if (getAttackTarget()!=null && !eligible(getAttackTarget())) {stopAttack();clearPath();stop();}
        super.logic();
    }
    @Override public void setIdle() {clearPath();stopAttack();stop();}
    @Override public List<RPEntity> getEnemyList() {
        List<RPEntity> result=new ArrayList<>();
        if (getZone()!=null) for (RPEntity entity:super.getEnemyList())
            if (eligible(entity) && (nextTo(entity,4)||getAttackingRPEntities().contains(entity))) result.add(entity);
        return result;
    }
}
