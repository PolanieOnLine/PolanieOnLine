package games.stendhal.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class MasteryLevelConfigTest {

	@Test
	public void testLoadsValidConfigFile() throws IOException {
		Path config = Files.createTempFile("mastery", ".properties");
		Files.write(config, (
				"mastery.maxLevel=1500\n"
				+ "mastery.early.maxLevel=100\n"
				+ "mastery.early.baseIncrement=90\n"
				+ "mastery.early.levelIncrement=12\n"
				+ "mastery.mid.maxLevel=700\n"
				+ "mastery.mid.baseIncrement=1500\n"
				+ "mastery.mid.levelIncrement=30\n"
				+ "mastery.late.baseIncrement=21000\n"
				+ "mastery.late.levelIncrement=55\n"
				+ "mastery.milestone.interval=50\n"
				+ "mastery.milestone.multiplier=300\n"
				+ "mastery.hardCapXP=999999999\n").getBytes(StandardCharsets.UTF_8));

		System.setProperty(MasteryLevelConfig.CONFIG_PATH_PROPERTY, config.toString());
		MasteryLevelConfig.LoadedConfig loaded = MasteryLevelConfig.load();
		System.clearProperty(MasteryLevelConfig.CONFIG_PATH_PROPERTY);

		assertTrue(loaded.isValid());
		assertEquals(1500, loaded.getConfig().getMaxLevel());
		assertEquals(50, loaded.getConfig().getMilestoneInterval());
		assertEquals(999999999L, loaded.getConfig().getHardCapXP());
	}

	@Test
	public void testFallbackOnInvalidConfigFile() throws IOException {
		Path config = Files.createTempFile("mastery-invalid", ".properties");
		Files.write(config, (
				"mastery.maxLevel=2000\n"
				+ "mastery.early.maxLevel=1200\n"
				+ "mastery.mid.maxLevel=800\n").getBytes(StandardCharsets.UTF_8));

		System.setProperty(MasteryLevelConfig.CONFIG_PATH_PROPERTY, config.toString());
		MasteryLevelConfig.LoadedConfig loaded = MasteryLevelConfig.load();
		System.clearProperty(MasteryLevelConfig.CONFIG_PATH_PROPERTY);

		assertFalse(loaded.isValid());
		assertEquals(MasteryLevelConfig.defaults().getMaxLevel(), loaded.getConfig().getMaxLevel());
		assertEquals(MasteryLevelConfig.defaults().getMilestoneInterval(), loaded.getConfig().getMilestoneInterval());
	}
}
