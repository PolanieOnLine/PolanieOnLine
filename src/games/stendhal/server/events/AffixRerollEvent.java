/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.events;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Translation;
import games.stendhal.server.core.rule.rarity.ItemAffixRerollService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/** Server-owned, correlated reroll window shared by both clients. */
public final class AffixRerollEvent extends RPEvent {
	public static final String NAME = "affix_reroll_event";
	private static final ItemAffixRerollService SERVICE = ItemAffixRerollService.getInstance();
	private static final Map<String, String> NAMES = names();
	private static final Map<String, String> FIXED_EFFECTS = fixedEffects();

	public static void generateRPClass() {
		final RPClass type = new RPClass(NAME);
		for (final String key : new String[] {"window_id", "phase", "request_id", "status", "selected_affix", "request_token", "offer_token", "item_name", "item_class", "item_subclass", "rerolled_affix"}) type.addAttribute(key, Type.STRING, Definition.PRIVATE);
		for (final String key : new String[] {"message", "selected_path", "current_label", "new_label"}) type.addAttribute(key, Type.LONG_STRING, Definition.PRIVATE);
		for (final String key : new String[] {"candidate_paths", "candidate_names", "candidate_classes", "candidate_subclasses", "candidate_rarities", "candidate_levels", "affix_ids", "affix_labels"}) type.addAttribute(key, Type.VERY_LONG_STRING, Definition.PRIVATE);
		type.addAttribute("npc_id", Type.INT, Definition.PRIVATE);
		type.addAttribute("stone_count", Type.INT, Definition.PRIVATE);
		type.addAttribute("stone_cost", Type.INT, Definition.PRIVATE);
		type.addAttribute("can_roll", Type.BYTE, Definition.PRIVATE);
		type.addAttribute("can_accept", Type.BYTE, Definition.PRIVATE);
	}

	public AffixRerollEvent(final Player player, final int npcId, final String window,
			final String request, final String phase, final String status, final Item item, final String affix) {
		super(NAME);
		put("window_id", window); put("request_id", request); put("npc_id", npcId);
		put("phase", phase); put("status", status); put("message", text(player, message(status)));
		put("stone_cost", 0); put("rerolled_affix", "");
		put("stone_count", SERVICE.stones(player)); put("can_roll", 0); put("can_accept", 0);
		for (final String key : new String[] {"selected_path", "selected_affix", "current_label", "new_label", "request_token", "offer_token", "item_name", "item_class", "item_subclass"}) put(key, "");
		final List<String> paths = new ArrayList<String>(), names = new ArrayList<String>(), classes = new ArrayList<String>(), subclasses = new ArrayList<String>(), rarities = new ArrayList<String>(), levels = new ArrayList<String>();
		for (final Item candidate : SERVICE.candidates(player)) {
			paths.add(ItemUpgradeEvent.encodePath(candidate)); names.add(text(player, candidate.getName()));
			classes.add(candidate.getItemClass()); subclasses.add(candidate.getItemSubclass());
			rarities.add(candidate.getRarityOrCommon().getId()); levels.add(Integer.toString(candidate.getUpgradeLevel()));
		}
		put("candidate_paths", paths); put("candidate_names", names); put("candidate_classes", classes);
		put("candidate_subclasses", subclasses); put("candidate_rarities", rarities); put("candidate_levels", levels);
		final List<String> ids = item == null ? Collections.<String>emptyList() : SERVICE.affixes(item);
		final List<String> labels = new ArrayList<String>();
		for (final String id : ids) labels.add(label(player, id, ItemAffixState.getValues(item).get(id)));
		put("affix_ids", ids); put("affix_labels", labels);
		if (item == null) return;
		put("selected_path", ItemUpgradeEvent.encodePath(item)); put("item_name", text(player, item.getName()));
		put("item_class", item.getItemClass()); put("item_subclass", item.getItemSubclass());
		final Map<String, String> pending = ItemAffixRerollService.state(item);
		final String locked = pending.get("locked");
		if (locked != null && ItemAffixState.has(item, locked)) put("rerolled_affix", locked);
		final String pricedAffix = SERVICE.hasPending(item) ? pending.get("old_id") : affix;
		if (pricedAffix != null && ids.contains(pricedAffix)) put("stone_cost", ItemAffixRerollService.cost(pricedAffix));
		if (SERVICE.hasPending(item)) {
			put("can_accept", SERVICE.validPending(player, item) ? 1 : 0);
			put("selected_affix", pending.get("old_id")); put("offer_token", pending.get("token"));
			put("current_label", label(player, pending.get("old_id"), pending.get("old_value")));
			put("new_label", label(player, pending.get("new_id"), pending.get("new_value")));
		} else if (affix != null && ids.contains(affix)) {
			put("selected_affix", affix); put("current_label", label(player, affix, ItemAffixState.getValues(item).get(affix)));
			put("request_token", SERVICE.selectionToken(player));
			put("can_roll", "READY".equals(status) && SERVICE.stones(player) >= ItemAffixRerollService.cost(affix) ? 1 : 0);
		}
	}

	private static String text(final Player player, final String value) { return Translation.getText(player.getLanguage(), value); }

	public static String label(final Player player, final String id, final String raw) {
		final String name = NAMES.get(id);
		if (name == null) return text(player, "Właściwość przedmiotu") + ": " + raw;
		final String effect = FIXED_EFFECTS.get(id);
		if (effect != null) return text(player, name) + " — " + text(player, effect);
		final double value = Double.parseDouble(raw);
		final boolean flat = id.equals("flat_attack_bonus") || id.equals("flat_defense_bonus") || id.equals("legendary_relic_power") || id.equals("legendary_bastion_bonus");
		final boolean points = id.equals("accuracy_bonus") || id.equals("critical_chance") || id.equals("lifesteal_effectiveness");
		final String number = BigDecimal.valueOf(flat || points ? value : value * 100).stripTrailingZeros().toPlainString();
		return text(player, name) + ": +" + number + (flat ? "" : "%");
	}

	private static String message(final String status) {
		switch (status) {
		case "READY": return "Wybraną właściwość przypiszesz do przemiany na stałe. Zwykły afiks kosztuje 1 kamień, a legendarny 3 kamienie.";
		case "OFFER": return "Wybierz nową właściwość albo zachowaj poprzednią. Kamienie zostały już zużyte.";
		case "ACCEPTED": return "Przyjęto nową właściwość.";
		case "KEPT": return "Zachowano poprzednią właściwość.";
		case "MISSING_STONE": return "Nie masz dość kamieni przemiany. Zwykły afiks wymaga 1, a legendarny 3 kamieni.";
		case "NPC_TOO_FAR": return "Podejdź do Zoryka Runicznego.";
		case "CHANGED_ITEM": return "Przedmiot został przeniesiony lub zmieniony. Możesz tylko zachować poprzednią właściwość.";
		case "INVALID_ITEM": return "Wybierz własny przedmiot z losowymi właściwościami.";
		case "INVALID_REQUEST": return "Wybór wygasł. Wybierz przedmiot i właściwość ponownie.";
		case "NO_REPLACEMENT": return "Ta właściwość nie ma dostępnego zamiennika.";
		default: return "Wybierz przedmiot i jedną losową właściwość do przemiany.";
		}
	}

	private static Map<String, String> names() {
		final Map<String, String> result = new LinkedHashMap<String, String>();
		final String[][] values = {
			{"flat_attack_bonus", "Dodatkowy atak"}, {"flat_defense_bonus", "Dodatkowy pancerz"}, {"spiked_plating", "Kolczaste okucie"}, {"hunter_mark", "Znak łowcy"}, {"giant_slayer", "Łowca olbrzymów"},
			{"armor_crushing", "Kruszenie"}, {"critical_hardening", "Hartowanie"}, {"lifesteal", "Kradzież życia"}, {"lifesteal_effectiveness", "Skuteczność kradzieży życia"}, {"accuracy_bonus", "Celność"}, {"critical_chance", "Szansa na trafienie krytyczne"}, {"critical_damage_bonus", "Obrażenia krytyczne"}, {"parry_chance", "Parowanie"},
			{"bleed_on_hit", "Krwawienie"}, {"poison_on_hit", "Zatrucie"}, {"execute_damage", "Dobijanie"}, {"distance_damage", "Obrażenia z dystansu"}, {"armor_penetration", "Przebicie pancerza"},
			{"resist_poisoned", "Odporność na zatrucie"}, {"resist_bleeding", "Odporność na krwawienie"}, {"resist_shocked", "Odporność na szok"}, {"resist_confused", "Odporność na dezorientację"}, {"resist_heavy", "Odporność na ociężałość"}, {"resist_stunned", "Odporność na ogłuszenie"},
			{"steady_hand", "Pewna ręka"}, {"poison_potency", "Zjadliwość"}, {"bleed_potency", "Rozdarcie"}, {"vigilance", "Czujność"}, {"ammo_conservation", "Oszczędny strzał"},
			{"legendary_cleave", "Rozmach"}, {"legendary_tenacity", "Zaciętość"}, {"legendary_breach", "Przełamanie"}, {"legendary_quick_recovery", "Szybkie otrząśnięcie"}, {"legendary_deep_wounds", "Głębokie Rany"}, {"legendary_armor_breaker", "Łamacz Pancerzy"}, {"legendary_longshot", "Dalekosiężność"}, {"legendary_executioner", "Egzekutor"}, {"legendary_duel_master", "Mistrz Pojedynku"}, {"legendary_crushing_blow", "Miażdżący Cios"}, {"legendary_stunning_force", "Ogłuszająca Siła"}, {"legendary_binding_strike", "Pętający Cios"}, {"legendary_merciless_reach", "Bezlitosny Zasięg"}, {"legendary_falcon_eye", "Sokole Oko"}, {"legendary_first_salvo", "Pierwsza Salwa"}, {"legendary_power_overload", "Przeciążenie Mocy"}, {"legendary_arcane_focus", "Skupienie Arkanów"},
			{"legendary_bastion_bonus", "Bastion"}, {"legendary_wall_of_gord", "Wał grodu"}, {"legendary_life_tide", "Przypływ życia"}, {"legendary_iron_will", "Żelazna Wola"}, {"legendary_unyielding_protection", "Nieugięta Ochrona"}, {"legendary_relic_power", "Relikt Mocy"}, {"legendary_hero_eye", "Oko Bohatera"}, {"legendary_guardian_seal", "Pieczęć Strażnika"},
			{"legendary_defense_circle", "Krąg obrony"}, {"legendary_hunters_pace", "Łowczy pęd"}, {"legendary_last_stand", "Ostatni zryw"}, {"legendary_inevitability", "Nieuchronność"}, {"legendary_iron_resolve", "Żelazny upór"}
		};
		for (final String[] value : values) result.put(value[0], value[1]);
		return result;
	}

	/** Keep fixed powers consistent with the item tooltip, not their internal marker value. */
	private static Map<String, String> fixedEffects() {
		final Map<String, String> result = new LinkedHashMap<String, String>();
		result.put("legendary_deep_wounds", "15% szansy na krwawienie; rana zadaje 35% obrażeń trafienia.");
		result.put("legendary_armor_breaker", "Redukuje 40% niekorzystnej kary wynikającej z pancerza celu.");
		result.put("legendary_longshot", "Ataki wykonane z dystansu zadają +25% obrażeń.");
		result.put("legendary_executioner", "Przeciw celom poniżej 20% PW zadajesz +35% obrażeń.");
		result.put("legendary_duel_master", "Zyskujesz +5 pkt proc. szansy na parowanie; po udanym parowaniu następne trafienie zadaje +30% obrażeń.");
		result.put("legendary_crushing_blow", "Przeciw celom w średnim lub ciężkim pancerzu zadajesz +25% obrażeń.");
		result.put("legendary_stunning_force", "15% szansy na ogłuszenie celu przez 3 s (4 s przeciw graczom) po trafieniu zadającym obrażenia.");
		result.put("legendary_binding_strike", "15% szansy na nałożenie ociężałości przez 10 s po trafieniu.");
		result.put("legendary_merciless_reach", "Zyskujesz +1 pole dodatkowego zasięgu ataku.");
		result.put("legendary_falcon_eye", "Przy ataku z co najmniej 4 pól zyskujesz +10 pkt proc. szansy na trafienie krytyczne.");
		result.put("legendary_first_salvo", "Przeciw celom mającym co najmniej 80% PW ataki dystansowe zadają +30% obrażeń.");
		result.put("legendary_power_overload", "Atak dystansowy ma 15% szansy zadać +50% obrażeń.");
		result.put("legendary_arcane_focus", "Ataki dystansowe przeciw celom z aktywnym negatywnym statusem zadają +25% obrażeń.");
		result.put("legendary_wall_of_gord", "Co 8 s, gdy pojedyncze bezpośrednie trafienie stworzenia zadałoby co najmniej 10% maksymalnych PW, zmniejsza obrażenia tego trafienia o 35%.");
		result.put("legendary_iron_will", "Zyskujesz +20 pkt proc. odporności na zatrucie, krwawienie, porażenie, dezorientację, ociężałość i ogłuszenie.");
		result.put("legendary_unyielding_protection", "Poniżej 30% PW zyskujesz +10 pkt proc. szansy na pełne sparowanie ataku wręcz; końcowa szansa nie przekracza 15%.");
		result.put("legendary_hero_eye", "Zyskujesz +8 pkt proc. szansy na trafienie krytyczne.");
		result.put("legendary_guardian_seal", "Zyskujesz +20 pkt proc. odporności na zatrucie, krwawienie, porażenie, dezorientację, ociężałość i ogłuszenie.");
		result.put("legendary_inevitability", "Po 2 kolejnych chybieniach następny atak nie chybi, a przez 4 s zadajesz o 20% więcej obrażeń. Zmiana celu lub broni zeruje efekt. Efekt nie kumuluje się.");
		result.put("legendary_iron_resolve", "Śmiertelne trafienie pozostawia 1 PZ. Odnowienie: 120 s. Nie chroni przed trucizną i krwawieniem. Efekt nie kumuluje się.");
		result.put("legendary_defense_circle", "Przy walce z kilkoma potworami każdy kolejny zmniejsza obrażenia ich trafień o 3%, maksymalnie o 12%. Efekt nie kumuluje się.");
		result.put("legendary_hunters_pace", "Zabicie potwora zwiększa prędkość poruszania się o 10% na 4 s. Kolejne zabójstwo odświeża czas. Zdjęcie butów usuwa bonus. Efekt nie kumuluje się.");
		result.put("legendary_last_stand", "Poniżej 30% maksymalnych PZ zadajesz o 20% więcej obrażeń trafieniami. Efekt nie kumuluje się.");
		result.put("legendary_life_tide", "Eliksiry mogą uleczyć cię do 20% ponad maksymalne PZ. Po 10 s nadwyżka zanika po 20 PZ co 2 s. Zdjęcie przedmiotu usuwa nadwyżkę. Efekt nie kumuluje się.");
		result.put("legendary_cleave", "Co 4 celne trafienia wręcz zadaje 30% obrażeń trafienia maksymalnie 2 pobliskim potworom walczącym z tobą. Dodatkowe trafienia nie kradną życia ani nie uruchamiają efektów broni.");
		result.put("legendary_tenacity", "Każde celne trafienie tego samego celu zwiększa obrażenia następnych o 3%, maksymalnie 15%. Zmiana celu lub 5 s bez trafienia zeruje bonus.");
		result.put("legendary_breach", "Po rozbiciu opancerzenia celu zadajesz mu o 15% więcej obrażeń zdrowiu przez 6 s.");
		result.put("legendary_quick_recovery", "Skraca czas ogłuszenia, ociężałości i dezorientacji o 30%. Efekt nie kumuluje się.");
		result.put("hunter_mark", "trafienie przez przeciwnika oznacza go na 6 s; przeciw oznaczonemu celowi redukujesz dodatkowe 5% niekorzystnej kary pancerza.");
		result.put("giant_slayer", "za każde pełne 50 poziomów przewagi celu redukujesz 1% niekorzystnej kary pancerza, maksymalnie 10%.");
		return result;
	}
}
