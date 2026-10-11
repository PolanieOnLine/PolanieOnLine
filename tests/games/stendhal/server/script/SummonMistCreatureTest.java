package games.stendhal.server.script;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.creature.EliteCreatureService;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class SummonMistCreatureTest {
	@BeforeClass public static void setup() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}
	private Player admin(final StendhalRPZone zone, final int level) {
		final Player admin = PlayerTestHelper.createPlayer("mist-admin");
		admin.setAdminLevel(level);
		zone.add(admin);
		admin.setPosition(10, 10);
		return admin;
	}
	@Test public void summonsMistAndEliteTogetherUsingMapAndEliteBonuses() {
		final StendhalRPZone zone = new StendhalRPZone("mist-script-test", 30, 30);
		final Player admin = admin(zone, 5000);
		final Creature base = SingletonRepository.getEntityManager().getCreature("kamienny golem");
		new SummonMistCreature().execute(admin, Arrays.asList("kamienny", "golem", "elite=true"));
		assertEquals(1, zone.getEntitiesOfClass(Creature.class).size());
		final Creature summoned = (Creature) zone.getEntitiesOfClass(Creature.class).iterator().next();
		assertEquals("mgielny kamienny golem", summoned.getName());
		assertTrue(EliteCreatureService.isElite(summoned));
		assertEquals((int) Math.round(((int) (base.getBaseHP() * 1.1)) * 1.4), summoned.getBaseHP());
		assertEquals(base.getBaseHP(), SingletonRepository.getEntityManager().getCreature("kamienny golem").getBaseHP());
	}
	@Test public void invalidFactorAndMissingAuthorityNeverSpawn() {
		final StendhalRPZone zone = new StendhalRPZone("mist-script-invalid", 30, 30);
		final Player admin = admin(zone, 5000);
		new SummonMistCreature().execute(admin, Arrays.asList("kamienny golem", "factor=NaN"));
		new SummonMistCreature().execute(admin, Arrays.asList("kamienny golem", "elite=maybe"));
		admin.setAdminLevel(0);
		new SummonMistCreature().execute(admin, Collections.singletonList("kamienny golem"));
		assertTrue(zone.getEntitiesOfClass(Creature.class).isEmpty());
	}
}
