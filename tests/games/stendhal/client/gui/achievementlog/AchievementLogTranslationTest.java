/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.achievementlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Field;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.Translation;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.factory.AbstractAchievementFactory;
import games.stendhal.server.maps.MockStendlRPWorld;

public class AchievementLogTranslationTest {
	private Field preferences;
	private Object previousPreferences;
	private final Properties isolated = new Properties();

	@Before
	public void isolateLanguageWithoutWritingPreferences() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		preferences.set(WtWindowManager.getInstance(), isolated);
		language("en");
	}

	@After
	public void restoreLanguage() throws Exception {
		preferences.set(WtWindowManager.getInstance(), previousPreferences);
	}

	private void language(final String language) {
		isolated.setProperty("config." + ClientLanguage.PROPERTY, language);
	}

	private String render(final String canonicalHtml) {
		final JTable table = new JTable(new Object[][] {{canonicalHtml}}, new Object[] {"Opis"});
		final AchievementLogRenderers.DescriptionCellRenderer renderer = AchievementLogRenderers.get()
				.new DescriptionCellRenderer();
		final JLabel label = (JLabel) renderer.getTableCellRendererComponent(table,
				table.getValueAt(0, 0), false, false, 0, 0);
		assertEquals(canonicalHtml, table.getValueAt(0, 0));
		return label.getText();
	}

	@Test
	public void translatesFullRequirementBeforeTheLogAddsItsPeriod() {
		final String html = AchievementLog.getAchievementDesc("Pomocnik Ados",
				"Ukończono dzienne zadanie na przedmiot 10 razy");
		final String english = render(html);
		assertTrue(english, english.contains("Ados Helper"));
		assertTrue(english, english.contains("Completed the daily item quest 10 times"));
		assertFalse(english, english.contains("Ukończono"));
		language("pl");
		assertTrue(render(html).contains("Ukończono dzienne zadanie na przedmiot 10 razy"));
	}

	@Test
	public void descriptionsWithTheirOwnPunctuationDoNotGetAnExtraPeriod() {
		final String html = AchievementLog.getAchievementDesc("test", "Spędzono ponad 20 000 godzin w grze!");
		final String english = render(html);
		assertTrue(english, english.contains("Spent over 20 000 hours in the game!"));
		assertFalse(english, english.contains("!."));
		final String alreadyDotted = AchievementLog.getAchievementDesc("test", "Opis.");
		assertFalse(alreadyDotted, alreadyDotted.contains(".."));
	}

	@Test
	public void customHeaderRendererTranslatesIconAndDescriptionOnEveryPaint() {
		final AchievementLogRenderers.HeaderRenderer renderer = new AchievementLogRenderers.HeaderRenderer();
		final JTable table = new JTable();
		final String[][] labels = {{"en", "Icon", "Description"}, {"es", "Icono", "Descripción"}, {"pl", "Ikona", "Opis"}};
		for (final String[] labelsForLanguage : labels) {
			language(labelsForLanguage[0]);
			int index = 1;
			for (final String source : new String[] {"Ikona", "Opis"}) {
				final JPanel rendered = (JPanel) renderer.getTableCellRendererComponent(table, source, false, false, 0, index);
				assertEquals(labelsForLanguage[index++], ((JLabel) rendered.getComponent(0)).getText());
			}
		}
	}

	@Test
	public void everyCurrentAchievementRequirementTranslatesInsideTheActualLogMarkup() {
		MockStendlRPWorld.get();
		for (final String locale : new String[] {"en", "es"}) {
			language(locale);
			int checked = 0;
			for (final AbstractAchievementFactory factory : AbstractAchievementFactory.createFactories()) {
				for (final Achievement achievement : factory.createAchievements()) {
					final String source = achievement.getDescription();
					final String translated = Translation.getText(locale, source);
					assertFalse(locale + ": " + source, source.equals(translated));
					final String html = render(AchievementLog.getAchievementDesc(achievement.getTitle(), source));
					assertTrue(locale + ": " + source + " -> " + html, html.contains(translated));
					assertEquals(source, achievement.getDescription());
					checked++;
				}
			}
			assertTrue("Audit must include the full catalogue: " + checked, checked > 200);
		}
	}

	@Test
	public void legacyServerDescriptionsStillTranslateAndWindowTitleKeepsCharacterName() {
		for (final String legacy : new String[] {
				"Zebrano po 8 sztuk z pośród wszystkich warzyw rosnących w Faiumoni",
				"Wyprodukowano po 50 rodzai każdego cennego metalu i zasobów",
				"Ulepszono pierścień z mithrilu do jej maksymalnego poziomu"}) {
			assertFalse(render(AchievementLog.getAchievementDesc("test", legacy)).contains(legacy));
		}
		assertEquals("Zamknij's achievement log", ClientLanguage.getText("Zamknij dziennik osiągnięć"));
		assertEquals("Available achievements: 270", ClientLanguage.getText("Liczba aktywnych osiągnięć: 270"));
		language("es");
		assertEquals("Registro de logros de Zamknij", ClientLanguage.getText("Zamknij dziennik osiągnięć"));
		assertEquals("Logros disponibles: 270", ClientLanguage.getText("Liczba aktywnych osiągnięć: 270"));
	}
}
