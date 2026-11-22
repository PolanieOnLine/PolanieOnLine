/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import games.stendhal.client.gui.AffixRerollWindow.Candidate;
import games.stendhal.client.gui.AffixRerollWindow.Correlation;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPEvent;

/** Headless checks for authoritative item lists and reply correlation. */
public class AffixRerollWindowTest {
	@Test public void changedAffixMarkerPrecedesTheEscapedName() {
		final String html = AffixRerollPresentation.labelHtml("Zaciętość: pełny <opis> afiksu.", true, 250);
		assertTrue(html, html.indexOf("affix_rerolled") < html.indexOf("Zaciętość:"));
		assertEquals(1, html.split("affix_rerolled", -1).length - 1);
	}

	@Test public void listEntriesEscapeBothNamesAndDescriptions() {
		final String html = AffixRerollPresentation.listLabelHtml("<moc>: +5% & <opis>", true, 280, true, false);
		assertTrue(html, html.contains("<b>&lt;moc&gt;:</b> +5% &amp; &lt;opis&gt;"));
		assertTrue(html, html.indexOf("affix_rerolled") < html.indexOf("&lt;moc&gt;"));
		assertTrue(html, html.contains(ItemRarityPresentation.dividerHtml()));
		assertFalse(html, html.contains("<moc>") || html.contains("<opis>"));
	}

	@Test public void ordinaryComparisonsUseTheSameNameAndDescriptionAsTheList() {
		final String label = "Celność: +7%";
		final String name = "<b>Celność:</b> +7%";
		assertTrue(AffixRerollPresentation.labelHtml(label, false, 280, "Obecny afiks: ", false).contains(name));
		assertTrue(AffixRerollPresentation.offerLabelHtml(label, 268, false).contains(name));
		assertFalse(AffixRerollPresentation.labelHtml(label, false, 280, "", false).contains("#f28c28"));
	}

	@Test public void legendaryLabelsShareTheTooltipNumberHighlightingAcrossTheService() {
		final String label = "Zaciętość: trafienia zwiększają obrażenia o 3%, maksymalnie 15%, przez 5 s.";
		final String tooltipStyle = LegendaryAffixPresentation.labelHtml(label);
		assertTrue(tooltipStyle, tooltipStyle.lastIndexOf("</b>") < tooltipStyle.indexOf("trafienia"));
		assertTrue(AffixRerollPresentation.listLabelHtml(label, true, 280, true, true).contains(tooltipStyle));
		assertTrue(AffixRerollPresentation.labelHtml(label, true, 280, "Obecny afiks: ", true).contains(tooltipStyle));
		assertTrue(AffixRerollPresentation.offerLabelHtml(label, 268, true).contains(tooltipStyle));
		for (String value : Arrays.asList("3%", "15%", "5 s")) {
			assertTrue(tooltipStyle, tooltipStyle.contains("color='#f3e2b8'>" + value + "</font>"));
		}
	}

	@Test public void legendaryDescriptionsAreEscapedBeforeNumericHighlighting() {
		final String html = AffixRerollPresentation.offerLabelHtml("<moc>: +5% & <opis>", 268, true);
		assertTrue(html, html.contains("&lt;moc&gt;"));
		assertTrue(html, html.contains("&amp; &lt;opis&gt;"));
		assertTrue(html, html.contains("color='#f3e2b8'>+5%</font>"));
		assertFalse(html, html.contains("<moc>") || html.contains("<opis>"));
	}

	@Test
	public void repliesRequireTheCurrentRequestWindowAndNpc() {
		final Correlation state = new Correlation("window-a", "42");
		assertFalse(state.accepts(reply("window-a", "42", "window-a:1", "select")));
		final String request = state.nextRequest();
		assertTrue(state.accepts(reply("window-a", "42", request, "select")));
		assertTrue(state.accepts(reply("window-a", "42", request, "offer")));
		assertTrue(state.accepts(reply("window-a", "42", request, "result")));
		assertFalse(state.accepts(reply("window-b", "42", request, "select")));
		assertFalse(state.accepts(reply("window-a", "43", request, "select")));
		assertFalse(state.accepts(reply("window-a", "42", request, "open")));
		assertFalse(state.accepts(reply("window-a", "42", request, "unknown")));
	}

	@Test
	public void refreshInvalidatesLostRollRepliesAndCloseRejectsEveryReply() {
		final Correlation state = new Correlation("window-a", "42");
		final String roll = state.nextRequest();
		final String refresh = state.nextRequest();
		assertFalse(state.accepts(reply("window-a", "42", roll, "offer")));
		assertTrue(state.accepts(reply("window-a", "42", refresh, "offer")));
		state.close();
		assertFalse(state.accepts(reply("window-a", "42", refresh, "offer")));
		assertFalse(state.accepts(reply("window-a", "42", state.nextRequest(), "result")));
	}

	@Test
	public void candidatesKeepTheirDistinctServerPathsRarityAndUpgradeLevels() {
		final RPEvent event = catalog();
		final List<Candidate> candidates = AffixRerollWindow.candidates(event);
		assertEquals(2, candidates.size());
		assertEquals("bag/7", candidates.get(0).path);
		assertEquals("bag/9", candidates.get(1).path);
		assertEquals("miecz +3", candidates.get(0).caption());
		assertEquals("miecz", candidates.get(1).caption());
		assertEquals(ItemRarity.EPIC, candidates.get(0).rarity);
		assertEquals(ItemRarity.RARE, candidates.get(1).rarity);
	}

	@Test
	public void missingOrMismatchedServerListsNeverInventCandidates() {
		assertTrue(AffixRerollWindow.candidates(new RPEvent("affix_reroll_event")).isEmpty());
		final RPEvent event = catalog();
		event.put("candidate_names", Collections.singletonList("miecz"));
		assertTrue(AffixRerollWindow.candidates(event).isEmpty());
	}

	@Test
	public void serverLabelsAreEscapedForSwingHtml() {
		assertEquals("<html><div style='width:250px'>&lt;b&gt;Moc &amp; " +
				"&quot;ogień&quot;&lt;/b&gt;</div></html>",
				AffixRerollWindow.html("<b>Moc & \"ogień\"</b>", 250));
	}

	private static RPEvent reply(final String window, final String npc, final String request, final String phase) {
		final RPEvent event = new RPEvent("affix_reroll_event");
		event.put("window_id", window);
		event.put("npc_id", npc);
		event.put("request_id", request);
		event.put("phase", phase);
		return event;
	}

	private static RPEvent catalog() {
		final RPEvent event = new RPEvent("affix_reroll_event");
		event.put("candidate_paths", Arrays.asList("bag/7", "bag/9"));
		event.put("candidate_names", Arrays.asList("miecz", "miecz"));
		event.put("candidate_classes", Arrays.asList("sword", "sword"));
		event.put("candidate_subclasses", Arrays.asList("sword", "sword"));
		event.put("candidate_rarities", Arrays.asList("epic", "rare"));
		event.put("candidate_levels", Arrays.asList("3", "0"));
		return event;
	}
}
