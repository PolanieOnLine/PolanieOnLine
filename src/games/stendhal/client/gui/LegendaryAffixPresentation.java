/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import games.stendhal.client.ClientLanguage;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;

/** Builds the dedicated Polish presentation of materialized item affixes. */
final class LegendaryAffixPresentation {
	private static final String LEGENDARY_TEXT_COLOR = "#f28c28";
	private static final String LEGENDARY_NUMBER_COLOR = "#f3e2b8";
	private static final Pattern NUMERIC_FRAGMENT = Pattern.compile(
			"[+-]?\\d+(?:[.,]\\d+)?(?:%|[ \\u00a0]+(?:pkt\\.?(?:[ \\u00a0]+proc\\.)?|seconds?|segundos?|s|PZ|PW|HP|PV)(?![A-Za-z]))?");
	private static final DecimalFormat ONE_DECIMAL = createDecimalFormat("0.0");
	private static final DecimalFormat COMPACT_DECIMAL = createDecimalFormat("0.#");

	private LegendaryAffixPresentation() {
		// utility class
	}

	static String build(final RPObject object) {
		if (object == null || !object.hasMap(ItemTooltip.ATTRIBUTE)) {
			return "";
		}

		final StringBuilder result = new StringBuilder();
		appendAttackRateReduction(result, object);
		appendSpikedPlating(result, object);
		appendRegularCombatAffixes(result, object);
		if (has(object, ItemTooltip.HUNTER_MARK)) {
			appendRegularText(result, "Znak łowcy",
					"trafienie przez przeciwnika oznacza go na 6 s; przeciw oznaczonemu celowi redukujesz dodatkowe 5% niekorzystnej kary pancerza.");
		}
		if (has(object, ItemTooltip.GIANT_SLAYER)) {
			appendRegularText(result, "Łowca olbrzymów",
					"za każde pełne 50 poziomów przewagi celu redukujesz 1% niekorzystnej kary pancerza, maksymalnie 10%.");
		}
		appendLegendaryCombatAffixes(result, object);

		if (has(object, ItemTooltip.LEGENDARY_DEEP_WOUNDS)) {
			appendFixed(result, "Głębokie Rany", new String[] {
					"", "15%", " szansy na krwawienie; rana zadaje ", "35%",
					" obrażeń trafienia."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_ARMOR_BREAKER)) {
			appendFixed(result, "Łamacz Pancerzy", new String[] {
					"Redukuje ", "40%",
					" niekorzystnej kary wynikającej z pancerza celu."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_LONGSHOT)) {
			appendFixed(result, "Dalekosiężność", new String[] {
					"Ataki wykonane z dystansu zadają ", "+25%", " obrażeń."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_EXECUTIONER)) {
			appendFixed(result, "Egzekutor", new String[] {
					"Przeciw celom poniżej ", "20%", " PW zadajesz ", "+35%",
					" obrażeń."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_DUEL_MASTER)) {
			appendFixed(result, "Mistrz Pojedynku", new String[] {
					"Zyskujesz ", "+5 pkt proc.",
					" szansy na parowanie; po udanym parowaniu następne trafienie zadaje ",
					"+30%", " obrażeń."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_CRUSHING_BLOW)) {
			appendFixed(result, "Miażdżący Cios", new String[] {
					"Przeciw celom w średnim lub ciężkim pancerzu zadajesz ",
					"+25%", " obrażeń."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_STUNNING_FORCE)) {
			appendFixed(result, "Ogłuszająca Siła", new String[] {
					"", "15%", " szansy na ogłuszenie celu przez ", "3 s",
					" (", "4 s", " przeciw graczom) po trafieniu zadającym obrażenia."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_BINDING_STRIKE)) {
			appendFixed(result, "Pętający Cios", new String[] {
					"", "15%", " szansy na nałożenie ociężałości przez ", "10 s",
					" po trafieniu."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_MERCILESS_REACH)) {
			appendFixed(result, "Bezlitosny Zasięg", new String[] {
					"Zyskujesz ", "+1 pole", " dodatkowego zasięgu ataku."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_FALCON_EYE)) {
			appendFixed(result, "Sokole Oko", new String[] {
					"Przy ataku z co najmniej ", "4 pól", " zyskujesz ",
					"+10 pkt proc.", " szansy na trafienie krytyczne."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_FIRST_SALVO)) {
			appendFixed(result, "Pierwsza Salwa", new String[] {
					"Przeciw celom mającym co najmniej ", "80%", " PW ataki dystansowe zadają ",
					"+30%", " obrażeń."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_POWER_OVERLOAD)) {
			appendFixed(result, "Przeciążenie Mocy", new String[] {
					"Atak dystansowy ma ", "15%", " szansy zadać ", "+50%",
					" obrażeń."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_ARCANE_FOCUS)) {
			appendFixed(result, "Skupienie Arkanów", new String[] {
					"Ataki dystansowe przeciw celom z aktywnym negatywnym statusem zadają ",
					"+25%", " obrażeń."
			});
		}

		// Legacy Bastion remains visible on saved items, but is no longer rolled.
		appendRolled(result, object, ItemTooltip.LEGENDARY_BASTION_BONUS,
				"Niezłomny Bastion", "+", " pkt. dodatkowego pancerza.");
		if (has(object, ItemTooltip.LEGENDARY_WALL_OF_GORD)) {
			appendFixed(result, "Wał grodu", new String[] {
					"Co ", "8 s",
					", gdy pojedyncze bezpośrednie trafienie stworzenia zadałoby co najmniej ",
					"10%", " maksymalnych PW, zmniejsza obrażenia tego trafienia o ",
					"35%", "."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_IRON_WILL)) {
			appendFixed(result, "Żelazna Wola", new String[] {
					"Zyskujesz ", "+20 pkt proc.",
					" odporności na zatrucie, krwawienie, porażenie, dezorientację, ociężałość i ogłuszenie."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_UNYIELDING_PROTECTION)) {
			appendFixed(result, "Nieugięta Ochrona", new String[] {
					"Poniżej ", "30%", " PW zyskujesz ", "+10 pkt proc.",
					" szansy na pełne sparowanie ataku wręcz; końcowa szansa nie przekracza ",
					"15%", "."
			});
		}
		appendRolled(result, object, ItemTooltip.LEGENDARY_RELIC_POWER,
				"Relikt Mocy", "+", " pkt. dodatkowego ataku.");
		if (has(object, ItemTooltip.LEGENDARY_HERO_EYE)) {
			appendFixed(result, "Oko Bohatera", new String[] {
					"Zyskujesz ", "+8 pkt proc.",
					" szansy na trafienie krytyczne."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_GUARDIAN_SEAL)) {
			appendFixed(result, "Pieczęć Strażnika", new String[] {
					"Zyskujesz ", "+20 pkt proc.",
					" odporności na zatrucie, krwawienie, porażenie, dezorientację, ociężałość i ogłuszenie."
			});
		}
		return result.length() == 0 ? ""
				: "<table width='100%' cellpadding='0' cellspacing='0'>" + result + "</table>";
	}

	private static void appendRegularCombatAffixes(final StringBuilder result, final RPObject object) {
		appendNewPercentage(result, object, ItemTooltip.STEADY_HAND, "Pewna ręka",
				"Zwiększa minimalne obrażenia broni o {0}%, bez zmiany maksymalnych obrażeń.");
		appendNewPercentage(result, object, ItemTooltip.POISON_POTENCY, "Zjadliwość",
				"Zwiększa obrażenia trucizny nakładanej bronią o {0}%, łącznie maksymalnie o 50%.");
		appendNewPercentage(result, object, ItemTooltip.BLEED_POTENCY, "Rozdarcie",
				"Zwiększa obrażenia krwawienia nakładanego bronią o {0}%, łącznie maksymalnie o 50%.");
		appendNewPercentage(result, object, ItemTooltip.VIGILANCE, "Czujność",
				"Pierwsze trafienie danego przeciwnika zadaje o {0}% mniej obrażeń. Ochrona odnawia się po 10 s bez jego trafienia; łącznie maksymalnie 25%.");
		appendNewPercentage(result, object, ItemTooltip.AMMO_CONSERVATION, "Oszczędny strzał",
				"{0}% szansy, że strzał nie zużyje amunicji; łącznie maksymalnie 20%. Nie dotyczy zaklęć ani broni miotanej.");
		appendRegularPercentage(result, object, ItemTooltip.ARMOR_CRUSHING, "Kruszenie",
				"% więcej obrażeń opancerzeniu przeciwnika; łącznie maksymalnie 30%.");
		appendRegularPercentage(result, object, ItemTooltip.CRITICAL_HARDENING, "Hartowanie",
				"% redukcji dodatkowej części otrzymywanych obrażeń krytycznych; łącznie maksymalnie 30%.");
	}

	private static void appendLegendaryCombatAffixes(final StringBuilder result, final RPObject object) {
		if (has(object, ItemTooltip.LEGENDARY_INEVITABILITY)) {
			appendFixed(result, "Nieuchronność", new String[] {
					"Po 2 kolejnych chybieniach następny atak nie chybi, a przez 4 s zadajesz o 20% więcej obrażeń. Zmiana celu lub broni zeruje efekt. Efekt nie kumuluje się."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_IRON_RESOLVE)) {
			appendFixed(result, "Żelazny upór", new String[] {
					"Śmiertelne trafienie pozostawia 1 PZ. Odnowienie: 120 s. Nie chroni przed trucizną i krwawieniem. Efekt nie kumuluje się."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_DEFENSE_CIRCLE)) {
			appendFixed(result, "Krąg obrony", new String[] {
					"Przy walce z kilkoma potworami każdy kolejny zmniejsza obrażenia ich trafień o 3%, maksymalnie o 12%. Efekt nie kumuluje się."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_HUNTERS_PACE)) {
			appendFixed(result, "Łowczy pęd", new String[] {
					"Zabicie potwora zwiększa prędkość poruszania się o 10% na 4 s. Kolejne zabójstwo odświeża czas. Zdjęcie butów usuwa bonus. Efekt nie kumuluje się."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_LAST_STAND)) {
			appendFixed(result, "Ostatni zryw", new String[] {
					"Poniżej 30% maksymalnych PZ zadajesz o 20% więcej obrażeń trafieniami. Efekt nie kumuluje się."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_LIFE_TIDE)) {
			appendFixed(result, "Przypływ życia", new String[] {
					"Eliksiry mogą uleczyć cię do 20% ponad maksymalne PZ. Po 10 s nadwyżka zanika po 20 PZ co 2 s. Zdjęcie przedmiotu usuwa nadwyżkę. Efekt nie kumuluje się."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_CLEAVE)) {
			appendFixed(result, "Rozmach", new String[] {
					"Co ", "4", " celne trafienia wręcz zadaje ", "30%",
					" obrażeń trafienia maksymalnie ", "2",
					" pobliskim potworom walczącym z tobą. Dodatkowe trafienia nie kradną życia ani nie uruchamiają efektów broni."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_TENACITY)) {
			appendFixed(result, "Zaciętość", new String[] {
					"Każde celne trafienie tego samego celu zwiększa obrażenia następnych o ", "3%",
					", maksymalnie ", "15%", ". Zmiana celu lub ", "5 s",
					" bez trafienia zeruje bonus."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_BREACH)) {
			appendFixed(result, "Przełamanie", new String[] {
					"Po rozbiciu opancerzenia celu zadajesz mu o ", "15%",
					" więcej obrażeń zdrowiu przez ", "6 s", "."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_QUICK_RECOVERY)) {
			appendFixed(result, "Szybkie otrząśnięcie", new String[] {
					"Skraca czas ogłuszenia, ociężałości i dezorientacji o ", "30%",
					". Efekt nie kumuluje się."
			});
		}
	}

	private static void appendNewPercentage(final StringBuilder result, final RPObject object,
			final String key, final String title, final String template) {
		final String raw = WeaponPerformanceCalculator.getTooltipValue(object, key);
		final Double value = raw == null ? null : parseDouble(raw);
		if (value != null && value.doubleValue() > 0.0) {
			appendRegularText(result, title, ClientLanguage.getText(template)
					.replace("{0}", COMPACT_DECIMAL.format(value.doubleValue() * 100)));
		}
	}

	private static void appendRegularPercentage(final StringBuilder result, final RPObject object,
			final String key, final String title, final String suffix) {
		String raw = WeaponPerformanceCalculator.getTooltipValue(object, key);
		if (raw == null) {
			return;
		}
		Double value = parseDouble(raw);
		if (value != null && value.doubleValue() > 0) {
			appendRegularText(result, title, COMPACT_DECIMAL.format(value.doubleValue() * 100) + suffix);
		}
	}

	private static boolean has(final RPObject object, final String key) {
		return WeaponPerformanceCalculator.getTooltipValue(object, key) != null;
	}

	private static void appendAttackRateReduction(final StringBuilder result,
			final RPObject object) {
		if (has(object, ItemTooltip.GLYPH_LEVEL)) return;
		final String raw = WeaponPerformanceCalculator.getTooltipValue(object,
				ItemTooltip.RATE_INCREASE);
		if (raw == null) {
			return;
		}
		final Integer value = parseInteger(raw);
		if (value == null || value.intValue() == 0) {
			return;
		}
		result.append("<tr><td valign='top'><font size='-1'>&#9670; -")
				.append(Math.abs(value.intValue()))
				.append(ClientLanguage.getText(" do wagi broni"))
				.append("</font></td></tr>");
	}

	private static void appendSpikedPlating(final StringBuilder result,
			final RPObject object) {
		final String raw = WeaponPerformanceCalculator.getTooltipValue(object,
				ItemTooltip.SPIKED_PLATING);
		if (raw == null) {
			return;
		}
		final Double value = parseDouble(raw);
		if (value == null || value.doubleValue() <= 0.0) {
			return;
		}
		appendRegularText(result, "Kolczaste okucie",
				"odbija " + ONE_DECIMAL.format(value.doubleValue() * 100.0)
						+ "% obrażeń z otrzymanych ciosów wręcz; łącznie maksymalnie 50%.");
	}

	private static void appendRegularText(final StringBuilder result,
			final String title, final String text) {
		result.append("<tr><td valign='top'><font size='-1'>&#9670; <b>")
				.append(ClientLanguage.getText(title)).append(":</b> ")
				.append(ClientLanguage.getText(text))
				.append("</font></td></tr>");
	}

	/**
	 * Parts alternate orange text and cream numeric fragments, beginning with
	 * orange text. Empty first text is allowed when the line starts with a number.
	 */
	private static void appendFixed(final StringBuilder result,
			final String title, final String[] parts) {
		beginLine(result, title);
		for (int index = 0; index < parts.length; index++) {
			appendColored(result, parts[index], index % 2 == 0
					? LEGENDARY_TEXT_COLOR : LEGENDARY_NUMBER_COLOR);
		}
		endLine(result);
	}

	private static void appendRolled(final StringBuilder result,
			final RPObject object, final String key, final String title,
			final String prefix, final String suffix) {
		final String raw = WeaponPerformanceCalculator.getTooltipValue(object, key);
		if (raw == null) {
			return;
		}
		final Integer value = parseInteger(raw);
		if (value == null) {
			return;
		}
		beginLine(result, title);
		appendColored(result, prefix + value, LEGENDARY_NUMBER_COLOR);
		appendColored(result, suffix, LEGENDARY_TEXT_COLOR);
		endLine(result);
	}

	private static Integer parseInteger(final String value) {
		try {
			return Integer.valueOf(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private static Double parseDouble(final String value) {
		try {
			return Double.valueOf(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private static DecimalFormat createDecimalFormat(final String pattern) {
		return new DecimalFormat(pattern,
				DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pl-PL")));
	}

	private static void beginLine(final StringBuilder result, final String title) {
		result.append("<tr><td valign='top' style='padding-top:4px'><b><font size='-1' color='")
				.append(LEGENDARY_TEXT_COLOR).append("'>")
				.append(ClientLanguage.getText(title)).append(": </font>");
	}

	private static void appendColored(final StringBuilder result,
			final String text, final String color) {
		if (text == null || text.length() == 0) {
			return;
		}
		final String translated = ClientLanguage.getText(text);
		if (LEGENDARY_TEXT_COLOR.equals(color)) {
			final Matcher numbers = NUMERIC_FRAGMENT.matcher(translated);
			int start = 0;
			while (numbers.find()) {
				appendFont(result, translated.substring(start, numbers.start()), color);
				appendFont(result, numbers.group(), LEGENDARY_NUMBER_COLOR);
				start = numbers.end();
			}
			appendFont(result, translated.substring(start), color);
		} else {
			appendFont(result, translated, color);
		}
	}

	private static void appendFont(final StringBuilder result,
			final String text, final String color) {
		if (!text.isEmpty()) {
			result.append("<font size='-1' color='").append(color).append("'>")
					.append(text).append("</font>");
		}
	}

	private static void endLine(final StringBuilder result) {
		result.append("</b></td></tr>");
	}
}
