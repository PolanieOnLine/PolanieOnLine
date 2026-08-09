/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class LegendaryAffixPresentationTest {
	@Test
	public void deepWoundsUsesPolishOrangeTextCreamNumbersAndNoDiamond() {
		final RPObject object = ItemTestHelper.createItem("legendary sword");
		put(object, ItemTooltip.LEGENDARY_DEEP_WOUNDS, "1.0");

		final String html = LegendaryAffixPresentation.build(object);

		assertTrue(html.contains("Głębokie Rany:"));
		assertTrue(html.contains("15%"));
		assertTrue(html.contains("35%"));
		assertTrue(html.contains("#f28c28"));
		assertTrue(html.contains("#f3e2b8"));
		assertFalse(html.contains("&#9670;"));
		assertFalse(html.contains("◆"));
	}

	@Test
	public void allLegendaryTitlesArePolish() {
		final RPObject object = ItemTestHelper.createItem("legendary set");
		put(object, ItemTooltip.LEGENDARY_DEEP_WOUNDS, "1.0");
		put(object, ItemTooltip.LEGENDARY_ARMOR_BREAKER, "1.0");
		put(object, ItemTooltip.LEGENDARY_LONGSHOT, "1.0");
		put(object, ItemTooltip.LEGENDARY_EXECUTIONER, "1.0");
		put(object, ItemTooltip.LEGENDARY_DUEL_MASTER, "1.0");
		put(object, ItemTooltip.LEGENDARY_CRUSHING_BLOW, "1.0");
		put(object, ItemTooltip.LEGENDARY_STUNNING_FORCE, "1.0");
		put(object, ItemTooltip.LEGENDARY_BINDING_STRIKE, "1.0");
		put(object, ItemTooltip.LEGENDARY_MERCILESS_REACH, "1.0");
		put(object, ItemTooltip.LEGENDARY_FALCON_EYE, "1.0");
		put(object, ItemTooltip.LEGENDARY_FIRST_SALVO, "1.0");
		put(object, ItemTooltip.LEGENDARY_POWER_OVERLOAD, "1.0");
		put(object, ItemTooltip.LEGENDARY_ARCANE_FOCUS, "1.0");
		put(object, ItemTooltip.LEGENDARY_BASTION_BONUS, "24");
		put(object, ItemTooltip.LEGENDARY_IRON_WILL, "1.0");
		put(object, ItemTooltip.LEGENDARY_UNYIELDING_PROTECTION, "1.0");
		put(object, ItemTooltip.LEGENDARY_RELIC_POWER, "6");
		put(object, ItemTooltip.LEGENDARY_HERO_EYE, "1.0");
		put(object, ItemTooltip.LEGENDARY_GUARDIAN_SEAL, "1.0");

		final String html = LegendaryAffixPresentation.build(object);

		assertTrue(html.contains("Głębokie Rany:"));
		assertTrue(html.contains("Łamacz Pancerzy:"));
		assertTrue(html.contains("Dalekosiężność:"));
		assertTrue(html.contains("Egzekutor:"));
		assertTrue(html.contains("Mistrz Pojedynku:"));
		assertTrue(html.contains("Miażdżący Cios:"));
		assertTrue(html.contains("Ogłuszająca Siła:"));
		assertTrue(html.contains("Pętający Cios:"));
		assertTrue(html.contains("Bezlitosny Zasięg:"));
		assertTrue(html.contains("Sokole Oko:"));
		assertTrue(html.contains("Pierwsza Salwa:"));
		assertTrue(html.contains("Przeciążenie Mocy:"));
		assertTrue(html.contains("Skupienie Arkanów:"));
		assertTrue(html.contains("Niezłomny Bastion:"));
		assertTrue(html.contains("Żelazna Wola:"));
		assertTrue(html.contains("Nieugięta Ochrona:"));
		assertTrue(html.contains("Relikt Mocy:"));
		assertTrue(html.contains("Oko Bohatera:"));
		assertTrue(html.contains("Pieczęć Strażnika:"));
		assertTrue(html.contains("+24"));
		assertTrue(html.contains("+6"));
	}

	@Test
	public void newLegendaryDescriptionsExposeTheirActualThresholdsAndBonuses() {
		final RPObject object = ItemTestHelper.createItem("legendary details");
		put(object, ItemTooltip.LEGENDARY_DUEL_MASTER, "1.0");
		put(object, ItemTooltip.LEGENDARY_STUNNING_FORCE, "1.0");
		put(object, ItemTooltip.LEGENDARY_MERCILESS_REACH, "1.0");
		put(object, ItemTooltip.LEGENDARY_FALCON_EYE, "1.0");
		put(object, ItemTooltip.LEGENDARY_FIRST_SALVO, "1.0");
		put(object, ItemTooltip.LEGENDARY_POWER_OVERLOAD, "1.0");
		put(object, ItemTooltip.LEGENDARY_UNYIELDING_PROTECTION, "1.0");
		put(object, ItemTooltip.LEGENDARY_HERO_EYE, "1.0");

		final String html = LegendaryAffixPresentation.build(object);

		assertTrue(html.contains("+5 pkt proc."));
		assertTrue(html.contains("+30%"));
		assertTrue(html.contains("10 s"));
		assertTrue(html.contains("+1 pole"));
		assertTrue(html.contains("4 pól"));
		assertTrue(html.contains("+10 pkt proc."));
		assertTrue(html.contains("80%"));
		assertTrue(html.contains("+50%"));
		assertTrue(html.contains("30%"));
		assertTrue(html.contains("15%"));
		assertTrue(html.contains("+8 pkt proc."));
	}

	private void put(final RPObject object, final String key, final String value) {
		object.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
