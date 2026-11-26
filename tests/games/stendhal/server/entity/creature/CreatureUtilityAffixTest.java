package games.stendhal.server.entity.creature;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.damage.HuntingAffixService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.BleedingStatus;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;

public class CreatureUtilityAffixTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test
	public void onlyNewlyGeneratedCreatureMoneyGetsTheCappedEquippedBonus() {
		final Player player = player("utility-new-money");
		player.getSlot("cloak").add(affix("cloak", 0.06));
		player.getSlot("pas").add(affix("belts", 0.06));
		player.getSlot("finger").add(affix("ring", 0.06));
		final DropCreature victim = creature(player);
		final StackableItem carried = money(1000);
		victim.addDropItem(carried);
		final StackableItem generated = money(1000);
		victim.generated.add(generated);
		final StackableItem arrows = (StackableItem) SingletonRepository.getEntityManager().getItem("strzała");
		arrows.setQuantity(1000);
		victim.generated.add(arrows);
		HuntingAffixService.onKilled(player, victim);
		victim.dropItemsOn(new Corpse("test", 0, 0));
		assertEquals(1000, carried.getQuantity());
		assertEquals(1150, generated.getQuantity());
		assertEquals(1000, arrows.getQuantity());
	}

	@Test
	public void theActualCorpseBeneficiarySuppliesTheBonusInsteadOfTheFinalBlowPlayer() {
		final Player killer = player("utility-final-hit");
		killer.getSlot("cloak").add(affix("cloak", 0.06));
		final Player beneficiary = PlayerTestHelper.createPlayer("utility-loot-owner");
		beneficiary.initHP(10000);
		killer.getZone().add(beneficiary);
		final DropCreature first = creature(killer);
		first.beneficiary = beneficiary.getName();
		final StackableItem unboosted = money(1000);
		first.generated.add(unboosted);
		HuntingAffixService.onKilled(killer, first);
		first.dropItemsOn(new Corpse("test", 0, 0));
		assertEquals(1000, unboosted.getQuantity());
		beneficiary.getSlot("neck").add(affix("necklace", 0.03));
		final DropCreature second = creature(killer);
		second.beneficiary = beneficiary.getName();
		final StackableItem boosted = money(1000);
		second.generated.add(boosted);
		HuntingAffixService.onKilled(killer, second);
		second.dropItemsOn(new Corpse("test", 0, 0));
		assertEquals(1030, boosted.getQuantity());
	}

	@Test
	public void poisonAndBleedFallbackCreditTheOriginalPlayerAndNeverCreateMoneyFromNothing() {
		final Player player = player("utility-money-status");
		player.getSlot("cloak").add(affix("cloak", 0.06));
		for (int index = 0; index < 2; index++) {
			final DropCreature victim = creature(player);
			final StackableItem generated = money(index == 0 ? 1000 : 0);
			victim.generated.add(generated);
			HuntingAffixService.onKilled(index == 0 ? new PoisonStatus(-10, 1, -10, player)
					: new BleedingStatus(10, 1, 1, player), victim);
			victim.dropItemsOn(new Corpse("test", 0, 0));
			assertEquals(index == 0 ? 1060 : 0, generated.getQuantity());
		}
	}

	@Test
	public void moneyBonusIsSnapshottedAtDeathAndCannotOverflowAStack() {
		final Player player = player("utility-money-snapshot");
		player.getSlot("cloak").add(affix("cloak", 0.06));
		final DropCreature victim = creature(player);
		final StackableItem generated = money(Integer.MAX_VALUE);
		victim.generated.add(generated);
		final StackableItem ordinary = money(1000);
		victim.generated.add(ordinary);
		HuntingAffixService.onKilled(player, victim);
		player.getSlot("cloak").clear();
		victim.dropItemsOn(new Corpse("test", 0, 0));
		assertEquals(Integer.MAX_VALUE, generated.getQuantity());
		assertEquals(1060, ordinary.getQuantity());
	}

	private Player player(final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.initHP(10000);
		final StendhalRPZone zone = new StendhalRPZone(name, 40, 40);
		zone.add(player);
		player.setPosition(10, 10);
		return player;
	}

	private DropCreature creature(final Player player) {
		final DropCreature creature = new DropCreature();
		creature.setGender("M");
		creature.put("name", "utility loot target");
		creature.initHP(100);
		creature.setHP(0);
		player.getZone().add(creature);
		creature.setPosition(11, 10);
		return creature;
	}

	private StackableItem money(final int quantity) {
		final StackableItem money = new StackableItem("utility money", "money", "test", new HashMap<String, String>());
		if (quantity == 0) {
			money.sub(money.getQuantity());
		} else {
			money.setQuantity(quantity);
		}
		return money;
	}

	private Item affix(final String type, final double value) {
		final Item item = new Item("utility coins", type, "test", new HashMap<String, String>());
		item.put(ItemAffixState.ATTRIBUTE, "coin_bonus", Double.toString(value));
		return item;
	}

	private static final class DropCreature extends Creature {
		private final List<Item> generated = new ArrayList<>();
		private String beneficiary;

		@Override
		public String getCorpseDeserver() {
			return beneficiary;
		}

		@Override
		List<Item> createDroppedItems(final EntityManager manager) {
			return generated;
		}
	}
}
