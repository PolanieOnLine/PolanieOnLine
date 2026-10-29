/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.challengearena;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.Creature;

/** Endgame wave themes, balanced against the arena's existing level bracket. */
final class ChallengeArenaEncounterPool {

	static final int MINIMUM_LEVEL = 400;

	private enum Family { DRAGONS, UNDEAD, ALIENS }

	private enum Member {
		GREEN_DRAGON("zielony smok", Family.DRAGONS),
		BLUE_DRAGON("błękitny smok", Family.DRAGONS, 1.0, 0.90, 1.0),
		RED_DRAGON("czerwony smok", Family.DRAGONS, 1.0, 0.90, 1.0),
		GOLDEN_DRAGON("złoty smok", Family.DRAGONS, 1.0, 0.90, 1.0),
		DESERT_DRAGON("pustynny smok", Family.DRAGONS),
		ICE_DRAGON("lodowy smok", Family.DRAGONS),
		ARCTIC_DRAGON("smok arktyczny", Family.DRAGONS),
		PURPLE_DRAGON("purpurowy smok", Family.DRAGONS, 1.0, 0.90, 1.0),
		TWO_HEADED_BLUE_DRAGON("dwugłowy niebieski smok", Family.DRAGONS),
		TWO_HEADED_GREEN_DRAGON("dwugłowy zielony smok", Family.DRAGONS),
		TWO_HEADED_GOLDEN_DRAGON("dwugłowy złoty smok", Family.DRAGONS),
		TWO_HEADED_RED_DRAGON("dwugłowy czerwony smok", Family.DRAGONS),
		TWO_HEADED_ICE_DRAGON("dwugłowy lodowy smok", Family.DRAGONS),
		BLACK_DRAGON("dwugłowy czarny smok", Family.DRAGONS, 1.0, 1.0, 1.0),
		GREEN_DRAKE("zielone smoczysko", Family.DRAGONS, 0.90, 1.10, 1.0),
		BLUE_DRAKE("niebieskie smoczysko", Family.DRAGONS, 0.90, 1.10, 1.0),
		RED_DRAKE("czerwone smoczysko", Family.DRAGONS, 0.90, 1.10, 1.0),
		BLACK_DRAKE("czarne smoczysko", Family.DRAGONS, 0.90, 1.10, 1.0),
		SKELETON("szkielet", Family.UNDEAD),
		ARMED_SKELETON("szkielet uzbrojony", Family.UNDEAD),
		BRAVE_SKELETON("szkielet waleczny", Family.UNDEAD),
		ELDER_SKELETON("szkielet starszy", Family.UNDEAD),
		DEMONIC_SKELETON("demoniczny szkielet", Family.UNDEAD),
		MUMMY("mumia", Family.UNDEAD),
		ROYAL_MUMMY("mumia królewska", Family.UNDEAD),
		GHOST("duch", Family.UNDEAD),
		GREEN_SPECTER("zielona zjawa", Family.UNDEAD),
		SPECTER("zjawa", Family.UNDEAD),
		FALLEN_GHOST("podziemny upadły duch", Family.UNDEAD),
		ZOMBIE("zombi", Family.UNDEAD),
		BLOODY_ZOMBIE("krwawy zombi", Family.UNDEAD),
		STINKING_ZOMBIE("cuchnący zombi", Family.UNDEAD),
		LICH("licho", Family.UNDEAD),
		DEAD_LICH("martwe licho", Family.UNDEAD),
		HIGH_LICH("wysokie licho", Family.UNDEAD),
		DEATH_KNIGHT("rycerz śmierci", Family.UNDEAD),
		REAPER("kostucha", Family.UNDEAD),
		PINK_REAPER("kostucha różowa", Family.UNDEAD),
		GOLDEN_REAPER("kostucha złota", Family.UNDEAD),
		GREAT_PINK_REAPER("kostucha różowa wielka", Family.UNDEAD, 1.15, 0.90, 1.05),
		ANGEL_SKELETON("szkielet anioła", Family.UNDEAD, 1.15, 0.90, 1.05),
		GREAT_REAPER("kostucha wielka", Family.UNDEAD, 0.85, 1.10, 0.95),
		SKELETON_PRINCE("książę szkieletów", Family.UNDEAD, 1.0, 1.0, 1.0),
		ALIEN("szarak", Family.ALIENS),
		ARMED_ALIEN("szarak uzbrojony", Family.ALIENS),
		ALIEN_WARRIOR("szarak wojownik", Family.ALIENS, 0.85, 1.10, 0.95),
		DEFENSIVE_ALIEN("szarak defensywy", Family.ALIENS, 1.15, 0.90, 1.05),
		ALIEN_GUARD("szarak strażnik", Family.ALIENS, 1.15, 0.90, 1.05),
		ALIEN_GUARD_CAPTAIN("szarak kapitan strażników", Family.ALIENS, 1.15, 0.90, 1.05),
		ALIEN_WARRIOR_CAPTAIN("szarak kapitan wojowników", Family.ALIENS, 0.85, 1.10, 0.95),
		ALIEN_DEFENSE_LEADER("szarak lider defensywy", Family.ALIENS, 1.15, 0.90, 1.05),
		HIGH_ARMED_ALIEN("szarak uzbrojony wyższy", Family.ALIENS),
		HIGH_ALIEN("szarak najwyższy", Family.ALIENS, 0.85, 1.10, 0.95),
		GIANT_ALIEN("szarak gigant", Family.ALIENS, 1.0, 1.0, 1.0);

		private final String name;
		private final Family family;
		private final double hp;
		private final double attack;
		private final double defense;

		Member(final String name, final Family family) {
			this(name, family, 1.0, 1.0, 1.0);
		}

		Member(final String name, final Family family, final double hp,
				final double attack, final double defense) {
			this.name = name;
			this.family = family;
			this.hp = hp;
			this.attack = attack;
			this.defense = defense;
		}

		static Member forName(final String name) {
			for (final Member member : values()) {
				if (member.name.equals(name)) return member;
			}
			return null;
		}
	}

	private final Map<Family, List<Creature>> families = new EnumMap<>(Family.class);
	private final Deque<Family> remainingFamilies = new ArrayDeque<>();
	private final Deque<Creature> remainingCreatures = new ArrayDeque<>();
	private Family currentFamily;

	/** Named endgame enemies must not depend on which templates were cached first. */
	static Collection<Creature> loadTemplates() {
		final EntityManager manager = SingletonRepository.getEntityManager();
		for (final Member member : Member.values()) {
			if (manager.isCreature(member.name)) manager.getCreature(member.name);
		}
		return new ArrayList<>(manager.getCreatures());
	}

	ChallengeArenaEncounterPool(final Collection<Creature> creatures) {
		for (final Creature creature : creatures) {
			final Member member = Member.forName(creature.getName());
			if (member != null) {
				families.computeIfAbsent(member.family, key -> new ArrayList<>()).add(creature);
			}
		}
	}

	void beginWave() {
		if (families.isEmpty()) return;
		if (remainingFamilies.isEmpty()) {
			final List<Family> next = new ArrayList<>(families.keySet());
			Collections.shuffle(next);
			if (next.size() > 1 && next.get(0) == currentFamily) {
				Collections.swap(next, 0, 1);
			}
			remainingFamilies.addAll(next);
		}
		currentFamily = remainingFamilies.removeFirst();
		remainingCreatures.clear();
	}

	Creature select(final int targetLevel) {
		if (targetLevel < MINIMUM_LEVEL || families.isEmpty()) return null;
		if (currentFamily == null) beginWave();
		if (remainingCreatures.isEmpty()) {
			final List<Creature> next = new ArrayList<>(families.get(currentFamily));
			Collections.shuffle(next);
			remainingCreatures.addAll(next);
		}
		return remainingCreatures.removeFirst();
	}

	void balance(final ArenaCreature creature, final int targetLevel,
			final List<Creature> reference) {
		final Member member = Member.forName(creature.getName());
		if (targetLevel < MINIMUM_LEVEL || member == null || reference.isEmpty()) return;
		double hp = 0;
		double attack = 0;
		double defense = 0;
		for (final Creature baseline : reference) {
			hp += (long) baseline.getBaseHP() + baseline.getBaseArmorHP();
			attack += baseline.getAtk();
			defense += baseline.getDef();
		}
		final double scale = targetLevel / (double) Math.max(1, reference.get(0).getLevel());
		final double perCreature = scale / reference.size();
		// Creature hit points use SHORT attributes, as do their combat skills.
		final int health = scale(hp * perCreature * member.hp, Short.MAX_VALUE);
		final double total = (double) creature.getBaseHP() + creature.getBaseArmorHP();
		final double armorShare = total > 0 ? creature.getBaseArmorHP() / total : 0.0;
		final int armor = Math.min(Math.max(0, health - 1), (int) Math.round(health * armorShare));
		creature.setLevel(targetLevel);
		creature.setBaseHP(health - armor);
		creature.setHP(health - armor);
		creature.initArmorHP(armor);
		creature.setAtk(scale(attack * perCreature * member.attack, Short.MAX_VALUE));
		if (creature.getRatk() > 0) {
			creature.setRatk(scale(attack * perCreature * member.attack, Short.MAX_VALUE));
		}
		creature.setDef(scale(defense * perCreature * member.defense, Short.MAX_VALUE));
	}

	private static int scale(final double value, final int maximum) {
		if (value <= 0) return 0;
		return (int) Math.max(1L, Math.min(maximum, Math.round(value)));
	}
}
