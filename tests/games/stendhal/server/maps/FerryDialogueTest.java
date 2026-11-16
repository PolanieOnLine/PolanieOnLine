/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.SpeakerNPCTestHelper;

/** Polish links displayed by the ferry crew must be usable conversation words. */
public class FerryDialogueTest {
	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Test
	public void ferryCrewsUnderstandThePolishWordForFerry() throws Exception {
		for (final String[] entry : new String[][] {
			{"krakow.ship.FerryConveyerNPC", "Janek"}, {"warszawa.ship.FerryConveyerNPC", "Franek"},
			{"warszawa.ship.FerryConveyerGdanskNPC", "Władek"}, {"gdansk.ship.FerryConveyerNPC", "Staszek"},
			{"gdansk.ship.FerryConveyerPirateIslandNPC", "Roko"}, {"pirate_island.ship.FerryConveyerNPC", "Odwagard"},
			{"athor.holiday_area.FerryConveyerNPC", "Jessica"}, {"ados.coast.FerryConveyerNPC", "Eliza"}
		}) {
			final SpeakerNPC npc = create(entry[0], entry[1]);
			final Player player = PlayerTestHelper.createPlayer("ferry_dialogue");
			player.setLanguage("pl");
			npc.getEngine().stepTest(player, "hi");
			SpeakerNPCTestHelper.getReply(npc);
			npc.getEngine().stepTest(player, "prom");
			final String reply = SpeakerNPCTestHelper.getReply(npc);
			assertNotNull(entry[1] + " must recognize prom", reply);
			assertTrue(entry[1] + ": " + reply, reply.startsWith("Prom "));
			SingletonRepository.getNPCList().remove(entry[1]);
		}
	}

	@Test
	public void elizaUnderstandsThePolishIslandKeyword() throws Exception {
		final SpeakerNPC npc = create("ados.coast.FerryConveyerNPC", "Eliza");
		final Player player = PlayerTestHelper.createPlayer("ferry_island");
		player.setLanguage("pl");
		npc.getEngine().stepTest(player, "hi");
		SpeakerNPCTestHelper.getReply(npc);
		npc.getEngine().stepTest(player, "wyspa");
		assertNotNull("The displayed Polish island word must have a reply", SpeakerNPCTestHelper.getReply(npc));
		SingletonRepository.getNPCList().remove("Eliza");
	}

	private SpeakerNPC create(final String className, final String name) throws Exception {
		SingletonRepository.getNPCList().remove(name);
		final StendhalRPZone zone = new StendhalRPZone("ferry_dialogue_test", 128, 128);
		((ZoneConfigurator) Class.forName("games.stendhal.server.maps." + className).getDeclaredConstructor().newInstance())
				.configureZone(zone, Collections.emptyMap());
		return SingletonRepository.getNPCList().get(name);
	}
}
