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
				+ "mastery.early.baseIncrement=500\n"
				+ "mastery.early.levelIncrement=20\n"
				+ "mastery.mid.maxLevel=700\n"
				+ "mastery.mid.baseIncrement=3000\n"
				+ "mastery.mid.levelIncrement=40\n"
				+ "mastery.late.baseIncrement=28000\n"
				+ "mastery.late.levelIncrement=80\n"
				+ "mastery.globalMultiplier=1.2\n"
				+ "mastery.milestone.interval=50\n"
				+ "mastery.milestone.multiplier=300\n"
				+ "mastery.hardCapXP=999999999\n").getBytes(StandardCharsets.UTF_8));

		System.setProperty(MasteryLevelConfig.CONFIG_PATH_PROPERTY, config.toString());
		MasteryLevelConfig.LoadedConfig loaded = MasteryLevelConfig.load();
		System.clearProperty(MasteryLevelConfig.CONFIG_PATH_PROPERTY);

		assertTrue(loaded.isValid());
		assertEquals(1500, loaded.getConfig().getMaxLevel());
		assertEquals(1.2d, loaded.getConfig().getGlobalMultiplier(), 0.0001d);
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

	@Test
	public void testFallbackWhenMinimumCostIsNotMonotonic() throws IOException {
		Path config = Files.createTempFile("mastery-invalid-monotonic", ".properties");
		Files.write(config, (
				"mastery.maxLevel=2000\n"
				+ "mastery.early.maxLevel=100\n"
				+ "mastery.early.baseIncrement=100\n"
				+ "mastery.early.levelIncrement=1\n"
				+ "mastery.mid.maxLevel=800\n"
				+ "mastery.mid.baseIncrement=50\n"
				+ "mastery.mid.levelIncrement=1\n"
				+ "mastery.late.baseIncrement=5000\n"
				+ "mastery.late.levelIncrement=2\n"
				+ "mastery.globalMultiplier=1.0\n"
				+ "mastery.milestone.interval=100\n"
				+ "mastery.milestone.multiplier=250\n"
				+ "mastery.hardCapXP=-1\n").getBytes(StandardCharsets.UTF_8));

		System.setProperty(MasteryLevelConfig.CONFIG_PATH_PROPERTY, config.toString());
		MasteryLevelConfig.LoadedConfig loaded = MasteryLevelConfig.load();
		System.clearProperty(MasteryLevelConfig.CONFIG_PATH_PROPERTY);

		assertFalse(loaded.isValid());
		assertEquals(MasteryLevelConfig.defaults().getMaxLevel(), loaded.getConfig().getMaxLevel());
	}
}
