package games.stendhal.server.entity.item;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class FoodMillTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ItemTestHelper.generateRPClasses();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOnUsedSugarMill() throws Exception {
		String name = "sugar mill";
		String clazz = "";
		String subclass = "";
		Map<String, String> attributes = new HashMap<String, String>();
		FoodMill fm = new FoodMill(name, clazz, subclass, attributes);
		Player user  =  PlayerTestHelper.createPlayer("fmbob");
		fm.onUsed(user);
		assertEquals("Powinieneś mieć sugar mill, aby móc go użyć.", PlayerTestHelper.getPrivateReply(user));

		user.equip("bag", fm);
		fm.onUsed(user);
		assertEquals("Powinieneś trzymać sugar mill w drugiej ręce, aby móc go użyć", PlayerTestHelper.getPrivateReply(user));
		user.equip("lhand", fm);
		fm.onUsed(user);

		assertEquals("Your other hand looks empty.", PlayerTestHelper.getPrivateReply(user));
		PlayerTestHelper.equipWithItemToSlot(user, "ser", "rhand");
		fm.onUsed(user);
		assertEquals("You need to have at least a sugar cane in your other hand", PlayerTestHelper.getPrivateReply(user));

		user.drop("ser");

		PlayerTestHelper.equipWithItemToSlot(user, "trzcina cukrowa", "rhand");

		fm.onUsed(user);
		assertEquals("You don't have an empty sack with you", PlayerTestHelper.getPrivateReply(user));
		PlayerTestHelper.equipWithItem(user, "pusty worek");
		fm.onUsed(user);
		assertTrue(user.isEquipped("cukier"));
	}
	@Test
	public void testOnUsedScrollEraser() throws Exception {
		String name = "scroll eraser";
		String clazz = "";
		String subclass = "";
		Map<String, String> attributes = new HashMap<String, String>();
		FoodMill fm = new FoodMill(name, clazz, subclass, attributes);
		Player user  =  PlayerTestHelper.createPlayer("fmbob");
		fm.onUsed(user);
		assertEquals("Powinieneś mieć scroll eraser, aby móc go użyć.", PlayerTestHelper.getPrivateReply(user));
		assertFalse(user.isEquipped("niezapisany zwój"));

		user.equip("bag", fm);
		fm.onUsed(user);
		assertEquals("Powinieneś trzymać scroll eraser w drugiej ręce, aby móc go użyć.", PlayerTestHelper.getPrivateReply(user));
		assertFalse(user.isEquipped("niezapisany zwój"));

		user.equip("lhand", fm);
		fm.onUsed(user);
		assertEquals("Your other hand looks empty.", PlayerTestHelper.getPrivateReply(user));
		assertFalse(user.isEquipped("niezapisany zwój"));

		PlayerTestHelper.equipWithItemToSlot(user, "ser", "rhand");
		PlayerTestHelper.equipWithItemToSlot(user, "zwój zapisany","bag");
		fm.onUsed(user);
		assertEquals("You need to have at least a marked scroll in your other hand", PlayerTestHelper.getPrivateReply(user));
		assertFalse(user.isEquipped("niezapisany zwój"));

		user.drop("ser");
		user.drop("zwój zapisany");
		PlayerTestHelper.equipWithItemToSlot(user, "zwój zapisany","rhand");
		fm.onUsed(user);
		assertTrue(user.isEquipped("niezapisany zwój"));
		assertFalse(user.isEquipped("zwój zapisany"));
	}

	@Test
	public void testFoodMill()
	 throws Exception {
		String name = "sugar mill";
		String clazz = "";
		String subclass = "";
		Map<String, String> attributes = new HashMap<String, String>();
		new FoodMill(name, clazz, subclass, attributes);

	}

}
