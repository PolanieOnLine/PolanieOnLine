/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Killer;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Private native-art encounters; no global creature or repeatable drops. */
final class CompassGuard extends Creature {
  static final int[] X = {67, 70, 84, 88}, Y = {87, 87, 75, 75};
  private final int role;
  private boolean recorded;

  private CompassGuard(Creature nativeCreature, int role) {
    super(nativeCreature);
    this.role = role;
    setXP(0);
    clearDropItemList();
    setName(role < 2 ? "wilk przy rannym" : "błędne światło");
    setDescription(
        role < 2
            ? "Wilk osaczył rannego wędrowca."
            : "Błędne światło tańczy wokół zagubionej obudowy kompasu.");
    setLevel(role < 2 ? 12 : 18);
    setAtk(role < 2 ? 55 : 70);
    setRatk(0);
    setDef(role < 2 ? 12 : 17);
    initHP(role < 2 ? 180 : 240);
    initArmorHP(0);
    setArmorType("none");
    setResistance(0);
    setVisibility(100);
    setBaseSpeed(0.4);
    LinkedHashMap<String, String> profiles = new LinkedHashMap<>();
    profiles.put("animal", "");
    profiles.put("offensive", "");
    profiles.put("no_elite", "");
    setAIProfiles(profiles);
  }

  static CompassGuard create(int role) {
    Creature source =
        SingletonRepository.getEntityManager().getCreature(role < 2 ? "wilk" : "błędny ognik");
    if (source == null) throw new IllegalStateException("Missing compass encounter creature");
    return new CompassGuard(source, role);
  }

  static boolean needed(Player p, int role) {
    return CompassProgress.stage(p) == (role < 2 ? Stage.WOLVES : Stage.CASE)
        && (CompassProgress.detail(p) & (1 << (role % 2))) == 0;
  }

  private boolean eligible(RPEntity enemy) {
    return enemy instanceof Player
        && PlayerPrivateScene.isOwnedBy(this, (Player) enemy)
        && needed((Player) enemy, role)
        && enemy.getZone() == getZone()
        && Math.abs(enemy.getX() - X[role]) <= 8
        && Math.abs(enemy.getY() - Y[role]) <= 8;
  }

  @Override
  public List<RPEntity> getEnemyList() {
    List<RPEntity> enemies = new ArrayList<>();
    for (RPEntity e : super.getEnemyList()) if (eligible(e)) enemies.add(e);
    return enemies;
  }

  @Override
  public void logic() {
    if (getAttackTarget() != null && !eligible(getAttackTarget())) {
      stopAttack();
      clearPath();
      stop();
    }
    super.logic();
  }

  @Override
  protected void dropItemsOn(Corpse corpse) {}

  @Override
  public Creature getNewInstance() {
    return new CompassGuard(this, role);
  }

  @Override
  public void onDead(Killer killer, boolean remove) {
    if (!recorded
        && getZone() != null
        && killer instanceof RPEntity
        && PlayerPrivateScene.canInteract(this, (RPEntity) killer)) {
      recorded = true;
      for (Player owner : getZone().getPlayers())
        if (PlayerPrivateScene.isOwnedBy(this, owner)
            && needed(owner, role)
            && owner.nextTo(this, 16)) {
          CompassProgress.set(
              owner,
              CompassProgress.stage(owner),
              CompassProgress.detail(owner) | (1 << (role % 2)),
              0);
          owner.sendPrivateText(CompassProgress.objective(owner));
          break;
        }
    }
    super.onDead(killer, remove);
  }
}
