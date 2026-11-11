/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Nature;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.core.rule.damage.TacticalAffixCombatService;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.spell.AttackingSpell;
import games.stendhal.server.entity.status.BleedingStatus;
import games.stendhal.server.entity.status.BleedingStatusTurnListener;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.entity.status.PoisonStatusTurnListener;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.quests.trail.TrailEvidence;
import marauroa.common.game.DetailLevel;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;

public class PlayerPrivateSceneTest {

	private Player owner;
	private Player outsider;
	private Creature enemy;
	private StendhalRPZone zone;

	@BeforeClass
	public static void world() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Before
	public void scene() {
		zone = new StendhalRPZone("private-combat", 20, 20);
		zone.protectionMap.init(20, 20);
		owner = PlayerTestHelper.createPlayer("scene owner");
		outsider = PlayerTestHelper.createPlayer("scene outsider");
		owner.setBaseHP(100);
		owner.setHP(100);
		outsider.setBaseHP(100);
		outsider.setHP(100);
		owner.setPosition(2, 2);
		outsider.setPosition(3, 2);
		zone.add(owner);
		zone.add(outsider);
		enemy = creature(4, 2);
		PlayerPrivateScene.assign(enemy, owner);
	}

	@Test
	public void ownershipUsesExistingPerceptionAndKeepsPublicInteractions() {
		assertEquals("name", enemy.get("#perception_key"));
		assertEquals(owner.getName(), PlayerPrivateScene.ownerName(enemy));
		assertTrue(enemy.has("owner_collision_only"));
		assertTrue(PlayerPrivateScene.isOwnedBy(enemy, owner));
		assertFalse(PlayerPrivateScene.isOwnedBy(enemy, outsider));
		assertTrue(PlayerPrivateScene.canInteract(owner, outsider));
		assertFalse(PlayerPrivateScene.canInteract(enemy, outsider));
		assertFalse(PlayerPrivateScene.canInteract(outsider, enemy));
		Creature sibling = creature(8, 8);
		PlayerPrivateScene.assign(sibling, owner);
		assertTrue(PlayerPrivateScene.canInteract(enemy, sibling));
		PlayerPrivateScene.assign(sibling, outsider);
		assertFalse(PlayerPrivateScene.canInteract(enemy, sibling));
	}

	@Test
	public void privateCluesCanBeSerializedForClientPerceptions() throws IOException {
		for (TrailEvidence.Kind kind : TrailEvidence.Kind.values()) {
			TrailEvidence clue = new TrailEvidence(kind);
			PlayerPrivateScene.assign(clue, owner);
			assertSerializable(clue);
		}
	}

	@Test
	public void privateCreatureAndItsCorpseCanBeSerializedForClientPerceptions() throws IOException {
		assertSerializable(enemy);
		assertSerializable(new Corpse(enemy, owner.getName()));
	}

	private void assertSerializable(Entity entity) throws IOException {
		for (DetailLevel detail : new DetailLevel[] { DetailLevel.FULL, DetailLevel.PRIVATE }) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			entity.writeObject(new OutputSerializer(bytes), detail);
			assertTrue(entity.getRPClass().getName(), bytes.size() > 0);
		}
	}

	@Test
	public void outsidersCannotTargetOrPerformForgedAttacks() {
		StendhalRPAction.startAttack(outsider, enemy);
		assertNull(outsider.getAttackTarget());
		outsider.setTarget(enemy);
		assertNull(outsider.getAttackTarget());
		enemy.setTarget(outsider);
		assertNull(enemy.getAttackTarget());
		assertFalse(StendhalRPAction.playerAttack(outsider, enemy));
		assertEquals(100, enemy.getHP());
	}

	@Test
	public void staleTargetsAreStoppedBeforeAttacking() {
		enemy.setTarget(owner);
		PlayerPrivateScene.assign(enemy, outsider);
		assertFalse(enemy.attack());
		assertNull(enemy.getAttackTarget());
	}

	@Test
	public void damageSinkBlocksMeleeAreaHitsDotsAndDirectDamageFromOutsiders() {
		enemy.initArmorHP(80);
		assertEquals(0, enemy.receiveCombatDamage(outsider, 10000).getTotalDamage());
		enemy.onHealthDamaged(outsider, 10000);
		enemy.onDamaged(outsider, 10000);
		assertEquals(0, enemy.damage(10000, outsider));
		assertEquals(100, enemy.getHP());
		assertEquals(80, enemy.getArmorHP());
		assertEquals(0, outsider.receiveCombatDamage(enemy, 10000).getTotalDamage());
		assertEquals(0, outsider.damage(10000, enemy));
		assertEquals(100, outsider.getHP());
	}

	@Test
	public void ownerAndOwnedPetCanTargetAndDamagePrivateEnemy() {
		Sheep pet = new Sheep();
		pet.setOwner(owner);
		zone.add(pet);
		assertTrue(PlayerPrivateScene.canInteract(enemy, pet));
		enemy.setTarget(pet);
		assertSame(pet, enemy.getAttackTarget());
		pet.setTarget(enemy);
		assertSame(enemy, pet.getAttackTarget());
		assertEquals(5, enemy.damage(5, pet));
		assertEquals(5, enemy.damage(5, owner));
		pet.setOwner(outsider);
		assertFalse(PlayerPrivateScene.canInteract(enemy, pet));
		assertEquals(0, enemy.damage(5, pet));
	}

	@Test
	public void sourceLessEnvironmentDamageStillWorks() {
		assertEquals(5, enemy.damage(5, null));
		assertEquals(95, enemy.getHP());
	}

	@Test
	public void sourcedPoisonAndWoundCannotBypassOwnershipThroughKillerWrappers() {
		assertEquals(0, enemy.damage(20, new PoisonStatus(-20, 1, -20, outsider)));
		assertEquals(0, enemy.damage(20, new BleedingStatus(20, 1, 1, outsider)));
		assertEquals(100, enemy.getHP());
		assertEquals(5, enemy.damage(5, new PoisonStatus(-5, 1, -5)));
	}

	@Test
	public void outsiderStatusesAreRejectedAndAlreadyScheduledTicksCannotDamagePrivateActor() {
		PoisonStatus poison = new PoisonStatus(-20, 1, -20, outsider);
		BleedingStatus wound = new BleedingStatus(20, 1, 1, outsider);
		assertFalse(enemy.getStatusList().inflictStatus(poison, outsider));
		assertFalse(enemy.getStatusList().inflictStatus(wound, outsider));
		// Simulate a status queued before this entity entered the private scene.
		enemy.getStatusList().getStatuses().add(poison);
		enemy.getStatusList().getStatuses().add(wound);
		new PoisonStatusTurnListener(enemy.getStatusList()).onTurnReached(1);
		new BleedingStatusTurnListener(enemy.getStatusList(), wound).onTurnReached(1);
		assertEquals(100, enemy.getHP());
	}

	@Test
	public void cleaveAreaDamageSkipsOtherPlayersPrivateEnemies() {
		Creature primary = creature(4, 3);
		Creature privateSecondary = creature(3, 3);
		privateSecondary.setTarget(outsider);
		PlayerPrivateScene.assign(privateSecondary, owner);
		Creature publicSecondary = creature(3, 1);
		publicSecondary.setTarget(outsider);
		Weapon weapon = new Weapon("cleave test", "sword", "test", new HashMap<String, String>());
		weapon.put("atk", 30);
		weapon.put("rate", 5);
		weapon.put(ItemTooltip.LEGENDARY_CLEAVE, 1);
		outsider.getSlot("rhand").add(weapon);
		for (int i = 0; i < 4; i++) {
			TacticalAffixCombatService.onHit(outsider, primary, 0,
					primary.receiveCombatDamage(outsider, 10), true);
		}
		assertEquals(100, privateSecondary.getHP());
		assertTrue(publicSecondary.getHP() < 100);
	}

	@Test
	public void collisionIsSymmetricForOwnersOutsidersAndDifferentPrivateScenes() {
		assertTrue(enemy.isObstacle(owner));
		assertTrue(owner.isObstacle(enemy));
		assertFalse(enemy.isObstacle(outsider));
		assertFalse(outsider.isObstacle(enemy));
		assertTrue(zone.collidesObjects(owner, owner.getArea(4, 2)));
		assertFalse(zone.collidesObjects(outsider, outsider.getArea(4, 2)));
		assertFalse(zone.collidesObjects(enemy, enemy.getArea(3, 2)));
		assertTrue(zone.collidesObjects(enemy, enemy.getArea(2, 2)));
		Creature otherScene = creature(6, 2);
		PlayerPrivateScene.assign(otherScene, outsider);
		assertFalse(zone.collidesObjects(enemy, enemy.getArea(6, 2)));
	}

	@Test
	public void aggressiveEnemyListContainsOnlyOwnerAndOwnersCompanions() {
		HashMap<String, String> profiles = new HashMap<String, String>();
		profiles.put("offensive", "");
		enemy.setAIProfiles(profiles);
		assertTrue(enemy.getEnemyList().contains(owner));
		assertFalse(enemy.getEnemyList().contains(outsider));
	}

	@Test
	public void privateCorpseKeepsPerceptionAndLootRestrictedAfterOrdinaryProtectionExpires() {
		Corpse corpse = new Corpse(enemy, owner.getName());
		assertEquals(owner.getName(), PlayerPrivateScene.ownerName(corpse));
		assertTrue(corpse.has("owner_collision_only"));
		assertTrue(corpse.mayUse(owner));
		assertFalse(corpse.mayUse(outsider));
	}

	@Test
	public void zeroExperienceSceneEnemyDoesNotAwardMinimumCombatExperience() {
		enemy.put("name", "private test enemy");
		enemy.receiveCombatDamage(owner, 10);
		int before = owner.getXP();
		enemy.rewardKillers(0);
		assertEquals(before, owner.getXP());
	}

	@Test
	public void attackingSpellRejectsPrivateTargetsOfAnotherPlayer() {
		class TestSpell extends AttackingSpell {
			TestSpell() { super("private spell", Nature.FIRE, 1, 100, 1, 0, 0, 0, 0, 10, 1, 0, 1); }
			boolean valid(Player caster, Entity target) { return isTargetValid(caster, target); }
		}
		TestSpell spell = new TestSpell();
		assertFalse(spell.valid(outsider, enemy));
		assertTrue(spell.valid(owner, enemy));
	}

	private Creature creature(int x, int y) {
		Creature result = new Creature();
		result.setBaseHP(100);
		result.setHP(100);
		result.setResistance(100);
		result.setPosition(x, y);
		zone.add(result);
		return result;
	}
}
