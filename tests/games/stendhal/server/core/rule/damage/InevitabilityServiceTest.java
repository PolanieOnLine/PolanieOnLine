package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;

public class InevitabilityServiceTest {
	@BeforeClass public static void setup() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test public void twoMissesGuaranteeTheNextValidAttackAndIncreaseItsDamage() {
		final Player player = player("inevitable-attack");
		final Creature target = target(player);
		assertFalse(StendhalRPAction.playerAttack(player, target));
		assertFalse(StendhalRPAction.playerAttack(player, target));
		assertTrue(StendhalRPAction.playerAttack(player, target));
		assertEquals(10000 - 120 * (target.has("crit") ? 2 : 1), target.getHP());
	}

	@Test public void damageBonusLastsFourSecondsAndDoesNotStackWithAnotherCopy() {
		final Player player = player("inevitable-time");
		final Weapon duplicate = new Weapon("second inevitability weapon", "sword", "test", null);
		duplicate.put("damage_min", "100");
		duplicate.put("damage_max", "100");
		duplicate.put(ItemAffixState.ATTRIBUTE, "legendary_inevitability", "1.0");
		player.getSlot("lhand").add(duplicate);
		final Creature target = target(player);
		assertFalse(InevitabilityService.resolveHit(player, target, false, 1000));
		assertFalse(InevitabilityService.resolveHit(player, target, false, 2000));
		assertEquals(120, InevitabilityService.boostDamage(player, target, 100, 5999));
		assertEquals(100, InevitabilityService.boostDamage(player, target, 100, 6000));
		assertTrue(InevitabilityService.resolveHit(player, target, false, 6000));
		assertFalse(InevitabilityService.resolveHit(player, target, false, 7000));
		assertFalse(InevitabilityService.resolveHit(player, target, false, 8000));
		assertEquals(120, InevitabilityService.boostDamage(player, target, 100, 8000));
		assertEquals(Integer.MAX_VALUE, InevitabilityService.boostDamage(player, target, Integer.MAX_VALUE, 8000));
	}

	@Test public void successfulHitsAndChangesOfTargetOrWeaponClearTheMissStreak() {
		final Player player = player("inevitable-reset");
		final Creature first = target(player);
		final Creature second = target(player);
		assertFalse(InevitabilityService.resolveHit(player, first, false, 1000));
		assertTrue(InevitabilityService.resolveHit(player, first, true, 2000));
		assertFalse(InevitabilityService.resolveHit(player, first, false, 3000));
		assertEquals(100, InevitabilityService.boostDamage(player, first, 100, 3000));
		assertFalse(InevitabilityService.resolveHit(player, second, false, 4000));
		assertFalse(InevitabilityService.resolveHit(player, first, false, 5000));
		assertFalse(InevitabilityService.resolveHit(player, first, false, 6000));
		player.getWeapon().onUnequipped();
		assertEquals(100, InevitabilityService.boostDamage(player, first, 100, 6001));
		assertFalse(InevitabilityService.resolveHit(player, first, false, 6001));
	}

	private Player player(final String name) {
		final Player player = new Player(PlayerTestHelper.createPlayer(name)) {
			@Override public boolean canHit(final RPEntity defender) { return false; }
			@Override public int damageDone(final RPEntity defender, final double attack, final Nature nature) { return 100; }
		};
		player.initHP(1000);
		new StendhalRPZone(name, 20, 20).add(player);
		player.setPosition(5, 5);
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("damage_min", "100");
		attributes.put("damage_max", "100");
		final Weapon weapon = new Weapon("inevitability weapon", "sword", "test", attributes);
		weapon.put(ItemAffixState.ATTRIBUTE, "legendary_inevitability", "1.0");
		player.getSlot("rhand").add(weapon);
		return player;
	}

	private Creature target(final Player player) {
		final Creature target = new Creature();
		target.put("name", "inevitability target");
		target.setGender("M");
		target.initHP(10000);
		player.getZone().add(target);
		target.setPosition(6, 5);
		return target;
	}
}
