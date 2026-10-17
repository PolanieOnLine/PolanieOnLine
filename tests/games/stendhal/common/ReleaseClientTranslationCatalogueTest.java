package games.stendhal.common;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/** Exercises release catalogues before their index entries are enabled. */
public class ReleaseClientTranslationCatalogueTest {
	@Test public void unsupportedJavaWarningPreservesVendorAndVmDetails() {
		String source="PolanieOnLine jest przeznaczona i testowana dla Oracle Java i OpenJDK. Używasz Zamknij TestVM {0}. Mogą wynikać problemy takie jak: czarny lub szary ekran.\n Jeżeli masz doświadczenie w programowaniu z JDK to zapraszamy do współpracy.";
		assertEquals("PolanieOnLine is designed and tested for Oracle Java and OpenJDK. You are using Zamknij TestVM {0}. You may encounter problems such as a black or gray screen.\n If you have JDK programming experience, you are welcome to contribute.",Translation.getText("en",source));
	}
	@Test public void englishProfileDeletionPreservesTheExactAccountAndHost() throws Exception {
		assertEquals("Your profile will be removed from the local account list.\nThis will not delete your account from any server.\nDo you want to remove the profile 'Zamknij@127.0.0.1'?",
				translate("en", "Zostanie usunięty twój profil z lokalnej listy kont.\nNie usunie to twojego konta z żadnego serwera.\nCzy chcesz usunąć profil 'Zamknij@127.0.0.1'?"));
	}

	@Test public void spanishTradeAndUpgradeTemplatesPreservePlayerNamesAndLevels() throws Exception {
		assertEquals("Oferta de Zamknij", translate("es", "Oferta Zamknij"));
		assertEquals("Nivel de mejora: +3 / +7", translate("es", "Poziom ulepszenia: +3 / +7"));
		assertEquals("ACEPTADO", translate("es", "ZAAKCEPTOWANE"));
	}

	@Test public void englishStatusAndProviderMessagesKeepNamesAndCounters() throws Exception {
		assertEquals("Steam login is only available on the official server.", translate("en", "Logowanie Steam jest dostępne tylko na oficjalnym serwerze."));
		assertEquals("Total: 1234567 coins", translate("en", "Łącznie: 1234567 coins"));
		assertEquals("Mining:\r\u00a017\r\u00a0(903)", translate("en", "Górnictwo:\r\u00a017\r\u00a0(903)"));
		assertEquals("Checking item...", translate("en", "Sprawdzanie przedmiotu..."));
	}

	@Test public void fullPrivacyNoticePreservesMarkupAndExplainsDataUseInBothLanguages() throws Exception {
		String source = privacyNotice();
		String english = translate("en", source);
		String spanish = translate("es", source);
		assertTrue(english, english.contains("We do NOT use"));
		assertTrue(spanish, spanish.contains("NO utilizamos"));
		assertTrue(english, english.contains("third parties"));
		assertTrue(spanish, spanish.contains("terceros"));
		assertEquals(tags(source), tags(english));
		assertEquals(tags(source), tags(spanish));
	}

	@Test public void settingsHintsTranslateTheirVisibleHtmlInBothLanguages() throws Exception {
		String source = "<html>Tłumaczenia są jeszcze uzupełniane.<br>Brakujący tekst pozostaje po polsku.</html>";
		assertEquals("<html>Translations are still being added.<br>Missing text stays in Polish.</html>", translate("en", source));
		assertEquals("<html>Las traducciones aún se están completando.<br>El texto que falta permanece en polaco.</html>", translate("es", source));
	}

	@Test public void generatedEmailWarningTranslatesItsInternalReasonWithoutAnEmailAddress() throws Exception {
		String source = "Nie podałeś adresu e-mail.\nAdres e-mail jest jedynym sposobem, w jaki administratorzy mogą kontaktować się z prawowitym właścicielem konta.\n"
				+ "Jeśli go nie dostarczysz, nie będziesz mógł uzyskać nowego hasła do tego konta, na przykład:\n"
				+ "- Zapomniałeś hasła.\n- Inny gracz w jakiś sposób dostaje hasło i zmienia je.\nCzy mimo to chcesz kontynuować?";
		String english = translate("en", source);
		assertTrue(english, english.startsWith("You did not enter an email address.\nEmail is the only way"));
		assertTrue(english, english.endsWith("Do you still want to continue?"));
		assertFalse(english, english.contains("Zapomniałeś"));
	}

	private static String tags(String html) { return html.replaceAll("(?s)>[^<]*<", "><"); }

	private String translate(String language, String source) throws Exception {
		Map<String, String> entries = new LinkedHashMap<String, String>();
		read(language + ".txt", entries, false);
		try (InputStream stream = resource(language + "-index.txt")) {
			if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
				String file;
				while ((file = reader.readLine()) != null) if (!file.trim().isEmpty() && !file.startsWith("#")) read(file.trim(), entries, false);
			}
		}
		read(language + "-release-client.txt", entries, true);
		Class<?> catalogue = Class.forName("games.stendhal.common.Translation$Catalogue");
		Constructor<?> constructor = catalogue.getDeclaredConstructor(String.class, Map.class);
		constructor.setAccessible(true);
		Method translate = Translation.class.getDeclaredMethod("translate", catalogue, String.class, int.class);
		translate.setAccessible(true);
		return (String) translate.invoke(null, constructor.newInstance(language, entries), source, 0);
	}

	private InputStream resource(String file) { return getClass().getClassLoader().getResourceAsStream("data/languages/" + file); }

	private void read(String file, Map<String, String> entries, boolean required) throws Exception {
		try (InputStream stream = resource(file)) {
			if (required) assertNotNull("Missing release catalogue " + file, stream);
			if (stream == null) return;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.trim().isEmpty() || line.startsWith("#")) continue;
					int separator = Translation.separator(line);
					assertTrue(file + ": malformed catalogue entry", separator > 0);
					String key = Translation.unescape(line.substring(0, separator));
					String value = Translation.unescape(line.substring(separator + 1));
					assertFalse(file + ": blank translation", value.trim().isEmpty());
					if (!entries.containsKey(key)) entries.put(key, value);
				}
			}
		}
	}

	private static String privacyNotice() {
		return "<html><body><p><font size=\"-2\">Podczas logowania, informacje, które identyfikują twój komputer zostaną zapisane<br>"
				+ "w celu zapobiegnięcia nadużyciom (np. przy próbie odgadnięcia hasła w celu włamania się na konto<br>"
				+ "lub tworzeniu wielu kont w celu sprawiania problemów).</font></p>"
				+ "<p><font size=\"-2\">Ponadto wszystkie zdarzenia i akcje, które wydarzą się w grze-<br>"
				+ "(np. rozwiązywanie zadań, atakowanie potworów) będą zapisywane w logach.<br>"
				+ "Informacje te wykorzystywane będą w celu analizy luk oraz rzadko w sprawach nadużyć.</font></p><br>"
				+ "<p><font size=\"-2\">PolanieOnLine to niekomercyjny projekt spędzania czasu wolnego. NIE wykorzystujemy<br>"
				+ "Twoich danych do celów reklamowych oraz NIE udostępniamy ich osobom trzecim.</font></p></body></html>";
	}
}
