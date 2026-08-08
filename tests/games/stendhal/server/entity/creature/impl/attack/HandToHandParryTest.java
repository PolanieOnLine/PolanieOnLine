/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.creature.impl.attack;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AttackEvent;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.RPClass.CreatureTestHelper;

public class HandToHandParryTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		CreatureTestHelper.generateRPClasses();
		MockStendhalRPRuleProcessor.get();
	}

	@Test
	public void successfulParryCancelsNormalCreatureAttack() {
		final HandToHand hth = new HandToHand() {
			@Override
			boolean rollParry(final Player player) {
				return true;
			}
		};
		final Creature creature = createMock(Creature.class);
		final Player player = createMock(Player.class);

		expect(creature.isAttackTurn(SingletonRepository.getRuleProcessor().getTurn()))
				.andReturn(true);
		expect(creature.getAttackTarget()).andReturn(player);

		player.rememberAttacker(creature);
		expectLastCall();
		expect(creature.getWeapon()).andReturn(null);
		creature.addEvent((AttackEvent) anyObject());
		expectLastCall();
		creature.notifyWorldAboutChanges();
		expectLastCall();

		replay(creature, player);
		hth.attack(creature);
		verify(creature, player);
	}
}
