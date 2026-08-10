package games.stendhal.server.entity.status;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

/** Prevents creature data from falling back to the removed pre-2.0 bleeding profile. */
public class BleedingCreatureProfileMigrationTest {
	private static final String LEGACY_PROFILE = "profile name=\"perilous\"";

	@Test
	public void allCreatureXmlUsesBleedingTwoPointZeroProfile() throws IOException {
		final Path creatureDirectory = Paths.get("data", "conf", "creatures");
		assertTrue("Creature configuration directory is missing", Files.isDirectory(creatureDirectory));

		final List<Path> legacyFiles = new ArrayList<Path>();
		try (Stream<Path> paths = Files.walk(creatureDirectory)) {
			final Iterator<Path> iterator = paths.iterator();
			while (iterator.hasNext()) {
				final Path path = iterator.next();
				if (!Files.isRegularFile(path) || !path.toString().endsWith(".xml")) {
					continue;
				}
				final String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
				if (content.contains(LEGACY_PROFILE)) {
					legacyFiles.add(path);
				}
			}
		}

		assertTrue("Legacy perilous bleeding profiles found: " + legacyFiles,
				legacyFiles.isEmpty());
	}
}
