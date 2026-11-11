/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.item.Corpse;
import marauroa.common.game.RPSlot;

/**
 * Detached story variants of native monsters, never global creature
 * definitions.
 */
final class TrailCreatureVariants {
	private static final Map<String, Spec> VARIANTS = new LinkedHashMap<>();
	static {
		register(new Spec("strażnik składu", "bandyta",
				"Oto strażnik ukrytego składu. Pilnuje worków z zapasami i dokumentów fałszywych dostaw.")
				.combat(120, 105, 0, 12, 8)
				.armour("light", 0)
				.movement(0.5, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human", "patrolling")
				.experience(2000)
				.respawn(200)
				.money(2, 5));

		register(new Spec("człowiek Radana", "bandyta",
				"Oto człowiek Radana. Pilnuje skradzionego towaru w kryjówce pod Semos.")
				.combat(150, 125, 0, 15, 11)
				.armour("light", 0)
				.movement(0.5, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human")
				.respawn(200));

		register(new Spec("strażnik więźnia", "bandyta",
				"Oto strażnik więźnia. Pilnuje skradzionego towaru w kryjówce pod Semos.")
				.combat(170, 135, 0, 16, 12)
				.armour("light", 0)
				.movement(0.5, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human")
				.respawn(200));

		register(new Spec("zbrojny Radana", "bandyta",
				"Oto zbrojny Radana. Pilnuje skradzionego towaru w kryjówce pod Semos.")
				.combat(300, 160, 0, 22, 15)
				.armour("light", 75)
				.movement(0.5, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human")
				.respawn(200));

		register(new Spec("wspólnik Radana", "bandyta",
				"Oto wspólnik Radana. Pilnuje przygotowań do napadów na transporty między Zakopanem a Semos.")
				.combat(380, 330, 0, 52, 42)
				.armour("light", 95)
				.movement(0.4, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human"));

		register(new Spec("wartownik buntowników", "rycerz karmazynowy",
				"Oto wartownik buntowników. Pozostaje w warszawskiej warowni podczas sporu księcia z załogą.")
				.combat(2200, 700, 0, 105, 150)
				.armour("heavy", 1100)
				.movement(0.45, 100, 0)
				.remains("human", "bag", 1, 1)
				.behaviour("human", "abnormal"));

		register(new Spec("zarządca Radomir", "czarny rycerz",
				"Oto zarządca Radomir. Pozostaje w warszawskiej warowni podczas sporu księcia z załogą.")
				.combat(3400, 850, 0, 120, 155)
				.armour("heavy", 1700)
				.movement(0.45, 100, 0)
				.remains("human", "bag", 1, 1)
				.behaviour("human", "abnormal"));

		register(new Spec("rabuś wybrzeża", "pirat",
				"Oto rabuś wybrzeża. Jego banda zwabia statki na mieliznę i zabiera ładunki rozbitków.")
				.combat(3200, 950, 1800, 135, 200)
				.armour("light", 800)
				.movement(0.45, 100, 0)
				.remains("human", "bag", 1, 1)
				.behaviour("human", "abnormal"));

		register(new Spec("herszt nadbrzeżnych rabusiów", "kapitan piratów",
				"Oto herszt nadbrzeżnych rabusiów. Jego banda zwabia statki na mieliznę i zabiera ładunki rozbitków.")
				.combat(5200, 1150, 1800, 155, 210)
				.armour("medium", 1300)
				.movement(0.45, 100, 0)
				.remains("human", "bag", 1, 1)
				.behaviour("human", "abnormal"));

		register(new Spec("niszczyciel sygnałów", "pirat",
				"Oto niszczyciel sygnałów. Jego banda zwabia statki na mieliznę i zabiera ładunki rozbitków.")
				.combat(3800, 1000, 1800, 140, 205)
				.armour("light", 950)
				.movement(0.45, 100, 0)
				.remains("human", "bag", 1, 1)
				.behaviour("human", "abnormal"));

		register(new Spec("strażnik milczącego dzwonu", "archanioł",
				"Oto strażnik milczącego dzwonu. W szczelinach jego pancerza drżą głosy ludzi, których zwabił na wyspy.")
				.combat(22000, 5000, 3455, 210, 550)
				.armour("heavy", 5500)
				.movement(0.8, 100, 0)
				.remains("giant_humanoid", "bag_2x2", 3, 2)
				.respawn(33)
				.damage( Nature.LIGHT)
				.susceptibility( Nature.DARK, 1.1));

		register(new Spec("echo strażnika", "archanioł",
				"Oto echo strażnika. Ostatni głos dzwonu przybrał skrzydlatą postać i zagrodził drogę ocalałym.")
				.combat(9000, 4000, 3455, 175, 500)
				.armour("heavy", 2250)
				.movement(0.8, 100, 0)
				.remains("giant_humanoid", "bag_2x2", 3, 2)
				.respawn(33)
				.damage( Nature.LIGHT)
				.susceptibility( Nature.DARK, 1.1));

		register(new Spec("łącznik duergarów", "duergar",
				"Oto łącznik duergarów. Pilnuje pakietów z rozkazami przy bocznym zakręcie kopalni.")
				.combat(240, 260, 0, 35, 30)
				.armour("light", 60)
				.movement(0.5, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human")
				.experience(5000)
				.respawn(240)
				.money(3, 6));

		register(new Spec("wysłannik duergarów", "duergar elitarny",
				"Oto wysłannik duergarów. Pilnuje przygotowań do napadów na transporty między Zakopanem a Semos.")
				.combat(430, 365, 0, 58, 46)
				.armour("light", 107)
				.movement(0.4, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human"));

		register(new Spec("dowódca duergarów", "duergar bohater",
				"Oto dowódca duergarów. Pilnuje przygotowań do napadów na transporty między Zakopanem a Semos.")
				.combat(650, 405, 0, 68, 55)
				.armour("heavy", 162)
				.movement(0.4, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human"));

		register(new Spec("żywiołak solanki", "żywioł wody",
				"Oto żywiołak solanki. Z dna starego zbiornika podnosi się postać z gęstej, słonej wody.")
				.combat(7800, 1450, 0, 170, 260)
				.armour("none", 0)
				.movement(0.45, 100, 0)
				.remains("humanoid", "bag", 1, 1)
				.behaviour("human", "abnormal"));

		register(new Spec("strażnik warowni", "kamienny golem",
				"Oto dawny strażnik warowni. Popękany kamień wciąż wykonuje rozkazy nieżyjącej załogi.")
				.combat(1050, 480, 0, 82, 80)
				.armour("heavy", 262)
				.movement(0.35, 100, 0)
				.remains("rock", "rock", 1, 1)
				.behaviour("animal"));

		register(new Spec("strażnik łowców jaj", "bandyta",
				"Oto strażnik łowców jaj. Pilnuje przewodnika, który odmówił dalszej pomocy jego bandzie.")
				.combat(6500, 2000, 0, 180, 400)
				.armour("light", 1625)
				.movement(0.45, 100, 100)
				.remains("human", "bag", 1, 1)
				.behaviour("human", "abnormal"));

		register(new Spec("herszt łowców jaj", "barbarzyńca lider",
				"Oto herszt łowców jaj. Podrzuca lęg przy ludzkich osadach, by odciągać smoki od następnych gniazd.")
				.combat(12000, 2600, 0, 205, 410)
				.armour("medium", 3000)
				.movement(0.45, 100, 100)
				.remains("human", "bag", 1, 1)
				.behaviour("human", "abnormal"));

		register(new Spec("solny szlam", "zielony glut",
				"Oto solny szlam. Wypełzł z zalanego chodnika i wciska się między kamienie.")
				.combat(4300, 1200, 0, 140, 250)
				.armour("none", 0)
				.movement(0.35, 100, 0)
				.remains("puddle_green", "puddle_green", 1, 1)
				.behaviour("human", "abnormal")
				.movementSound("loop-drag-slimy-1"));

		register(new Spec("poległy wartownik", "duch",
				"Oto poległy wartownik. Wciąż wykonuje rozkaz, który wydano mu za życia.")
				.combat(1200, 470, 0, 80, 100)
				.armour("none", 0)
				.movement(0.45, 55, 100)
				.remains("cloaked_white", "cloaked_white", 1, 1)
				.behaviour("animal", "abnormal")
				.withoutShadow());

		register(new Spec("strażnik dawnej chorągwi", "duch",
				"Oto strażnik dawnej chorągwi. Wciąż wykonuje rozkaz, który wydano mu za życia.")
				.combat(1800, 580, 0, 95, 105)
				.armour("none", 0)
				.movement(0.45, 55, 100)
				.remains("cloaked_white", "cloaked_white", 1, 1)
				.behaviour("animal", "abnormal")
				.withoutShadow());

	}

	private TrailCreatureVariants() {
	}

	private static void register(Spec spec) {
		VARIANTS.put(spec.name, spec);
	}

	static Creature create(String name) {
		Spec spec = VARIANTS.get(name);
		if (spec == null) {
			throw new IllegalArgumentException(
					"Unknown story opponent: " + name);
		}
		Creature template = SingletonRepository.getEntityManager().getCreature(
				spec.template);
		if (template == null) {
			throw new IllegalStateException(
					"Missing native creature: " + spec.template);
		}
		return new Variant(template, spec);
	}

	private static final class Variant extends Creature {
		@Override
		protected void dropItemsOn(final Corpse corpse) {
			if (PlayerPrivateScene.ownerName(this) == null) {
				super.dropItemsOn(corpse);
			}
		}

		Variant(Creature template, Spec spec) {
			super(template);
			for (RPSlot slot : slots()) {
				slot.clear();
			}
			updateItemAtkDef();
			setName(spec.name);
			setDescription(spec.description);
			setAtk(spec.atk);
			setRatk(spec.ratk);
			setDef(spec.def);
			initHP(spec.hp);
			initArmorHP(spec.armourHP);
			setArmorType(spec.armourType);
			setLevel(spec.level);
			setXP(spec.xp);
			setBaseSpeed(spec.speed);
			setSize(spec.width, spec.height);
			setVisibility(spec.visibility);
			setResistance(spec.resistance);
			setRespawnTime(spec.respawn);
			setCorpse(spec.corpse, spec.harmless, spec.width, spec.height);
			setBlood(null);
			// Copying a native template must not import its loot, poison,
			// healing, noises or resistances into a scripted encounter.
			clearDropItemList();
			if (spec.moneyMax > 0) {
				addDropItem("money", 100.0, spec.moneyMin, spec.moneyMax);
			}
			statusAttackers = ImmutableList.of();
			setAIProfiles(new LinkedHashMap<>(spec.profiles));
			setSusceptibilities(new EnumMap<>(spec.susceptibilities));
			setDamageTypes(spec.damage, spec.damage);
			setNoises(new LinkedHashMap<String, LinkedList<String>>());
			setSounds(Collections.emptyList());
			setDeathSound(null);
			setMovementSound(spec.movementSound);
			remove("active_idle");
			remove("flying");
			remove("unnamed");
			remove("no_hpbar");
			remove("immortal");
			remove("no_shadow");
			remove("shadow_style");
			if (spec.noShadow) {
				setShadowStyle("none");
			}
			update();
			updateModifiedAttributes();
		}
	}

	/** Encounter overrides apply only to the newly-created quest instance. */
	private static final class Spec {
		final String name, template, description;
		final Map<String, String> profiles = new LinkedHashMap<>();
		final EnumMap<Nature, Double> susceptibilities = new EnumMap<>(
				Nature.class);
		int hp, atk, ratk, def, level, xp, armourHP, width = 1, height = 1;
		int visibility, resistance, respawn = 180, moneyMin, moneyMax;
		double speed;
		String armourType, corpse, harmless, movementSound;
		boolean noShadow;
		Nature damage = Nature.CUT;

		Spec(String name, String template, String description) {
			this.name = name;
			this.template = template;
			this.description = description;
			behaviour("brave", "offensive", "no_elite");
		}

		Spec combat(int hp, int atk, int ratk, int def, int level) {
			this.hp = hp;
			this.atk = atk;
			this.ratk = ratk;
			this.def = def;
			this.level = level;
			return this;
		}

		Spec armour(String type, int hp) {
			armourType = type;
			armourHP = hp;
			return this;
		}

		Spec movement(double speed, int visibility, int resistance) {
			this.speed = speed;
			this.visibility = visibility;
			this.resistance = resistance;
			return this;
		}

		Spec remains(String corpse, String harmless, int width, int height) {
			this.corpse = corpse;
			this.harmless = harmless;
			this.width = width;
			this.height = height;
			return this;
		}

		Spec movementSound(String sound) {
			movementSound = sound;
			return this;
		}

		Spec behaviour(String... names) {
			for (String name : names) {
				profiles.put(name, null);
			}
			return this;
		}

		Spec experience(int xp) {
			this.xp = xp;
			return this;
		}

		Spec respawn(int turns) {
			respawn = turns;
			return this;
		}

		Spec withoutShadow() {
			noShadow = true;
			return this;
		}

		Spec damage(Nature nature) {
			damage = nature;
			return this;
		}

		Spec susceptibility(Nature nature, double value) {
			susceptibilities.put(nature, value);
			return this;
		}

		Spec money(int min, int max) {
			moneyMin = min;
			moneyMax = max;
			return this;
		}
	}
}
