package games.stendhal.client.events;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class ItemLogEventTest {
	@Test
	public void catalogueLinkSelectsTheExactItem() throws Exception {
		assertEquals("https://polanieonline.eu/przedmioty?klasa=dagger"
				+ "&item=dagger%3Agolden_blade%3Az%C5%82ota+klinga",
				ItemLogEvent.itemCatalogUrl("https://polanieonline.eu", "dagger", "golden_blade", "złota klinga"));
	}

	@Test
	public void trailingSlashDoesNotCreateADoubleSlash() throws Exception {
		assertEquals("https://polanieonline.eu/przedmioty?klasa=sword&item=sword%3Asword%3Amiecz",
				ItemLogEvent.itemCatalogUrl("https://polanieonline.eu/", "sword", "sword", "miecz"));
	}

	@Test
	public void polishLettersAndQueryCharactersRoundTrip() throws Exception {
		String name = "żółty przedmiot A+ & #?";
		URI url = URI.create(ItemLogEvent.itemCatalogUrl("https://polanieonline.eu", "ranged", "auto_crossbow_aa", name));
		assertEquals("/przedmioty", url.getPath());
		assertEquals(null, url.getFragment());
		String itemParam = url.getRawQuery().split("&item=", 2)[1];
		assertEquals("ranged:auto_crossbow_aa:" + name,
				URLDecoder.decode(itemParam, StandardCharsets.UTF_8.name()));
	}

	@Test
	public void subclassDistinguishesItemsWithTheSameName() throws Exception {
		assertEquals("https://polanieonline.eu/przedmioty?klasa=sword&item=sword%3Aother_sword%3Amiecz",
				ItemLogEvent.itemCatalogUrl("https://polanieonline.eu", "sword", "other_sword", "miecz"));
	}
}
