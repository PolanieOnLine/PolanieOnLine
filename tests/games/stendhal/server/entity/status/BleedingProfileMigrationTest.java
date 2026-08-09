package games.stendhal.server.entity.status;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Test;

/** Guards the creature-data migration to the explicit bleeding profile. */
public class BleedingProfileMigrationTest {
	private static final Pattern LEGACY_PROFILE = Pattern.compile(
			"name\\s*=\\s*[\\\"']perilous[\\\"']");
	private static final Pattern BLEEDING_PROFILE = Pattern.compile(
			"<profile\\b(?=[^>]*\\bname\\s*=\\s*[\\\"']bleeding_attack[\\\"'])[^>]*>");
	private static final Pattern PARAMS_ATTRIBUTE = Pattern.compile(
			"\\bparams\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']");

	@Test
	public void creatureDataUsesBleedingAttackProfileName() throws IOException {
		final Path root = Paths.get("data", "conf", "creatures");
		final List<String> legacyFiles = new ArrayList<String>();

		try (Stream<Path> paths = Files.walk(root)) {
			paths.filter(path -> path.toString().endsWith(".xml"))
					.forEach(path -> collectLegacyProfile(path, legacyFiles));
		}

		assertTrue("Legacy perilous bleeding profiles remain in: " + legacyFiles,
				legacyFiles.isEmpty());
	}

	@Test
	public void migratedBleedingProfilesUseSupportedParameters() throws IOException {
		final Path root = Paths.get("data", "conf", "creatures");
		final List<String> invalidProfiles = new ArrayList<String>();

		try (Stream<Path> paths = Files.walk(root)) {
			paths.filter(path -> path.toString().endsWith(".xml"))
					.forEach(path -> validateBleedingProfiles(path, invalidProfiles));
		}

		assertTrue("Invalid bleeding_attack profiles: " + invalidProfiles,
				invalidProfiles.isEmpty());
	}

	private void collectLegacyProfile(final Path path,
			final List<String> legacyFiles) {
		try {
			final String content = read(path);
			if (LEGACY_PROFILE.matcher(content).find()) {
				legacyFiles.add(path.toString());
			}
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot inspect " + path, e);
		}
	}

	private void validateBleedingProfiles(final Path path,
			final List<String> invalidProfiles) {
		try {
			final Matcher profiles = BLEEDING_PROFILE.matcher(read(path));
			while (profiles.find()) {
				final String profileTag = profiles.group();
				final Matcher paramsMatcher = PARAMS_ATTRIBUTE.matcher(profileTag);
				if (!paramsMatcher.find()) {
					invalidProfiles.add(path + " [missing params]");
					continue;
				}
				final String params = paramsMatcher.group(1);
				try {
					BleedingAttackerFactory.get(params);
				} catch (final RuntimeException e) {
					invalidProfiles.add(path + " [" + params + "]");
				}
			}
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot inspect " + path, e);
		}
	}

	private String read(final Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
