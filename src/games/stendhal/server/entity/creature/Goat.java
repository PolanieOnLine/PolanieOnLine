/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.entity.Killer;
import games.stendhal.server.entity.mapstuff.spawner.GoatFood;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.SyntaxException;

/**
 * A goat is a domestic animal that can be owned by a player. It eats berries
 * from bushes and can be sold.
 */
/**
 * @author KarajuSs
 *
 */
public class Goat extends DomesticAnimal {

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(Goat.class);
	private static final List<String> largeGoatSounds = Arrays.asList("goat-2");
	private static final List<String> smallGoatSounds = Arrays.asList("goat-1");

	/**
	 * The amount of hunger that indicates hungry.
	 */
	protected static final int HUNGER_HUNGRY = 50;

	/**
	 * The amount of hunger that indicates extremely hungry.
	 */
	protected static final int HUNGER_EXTREMELY_HUNGRY = 500;

	/**
	 * The amount of hunger that indicates starvation.
	 */
	protected static final int HUNGER_STARVATION = 1000;

	/**
	 * The weight at which the goat will stop eating.
	 */
	public static final int MAX_WEIGHT = 100;

	private static final int HP = 30;

	private static final int ATK = 5;

	private static final int DEF = 15;

	private static final int XP = 0;

	/**
	 * Random timing offset to give goat non-synchronized reactions.
	 */
	private final int timingAdjust;

	private int hunger;

	@Override
	public void setAttackStrategy(final Map<String, String> aiProfiles) {
		final Map<String, String> goatProfile = new HashMap<String, String>();
		goatProfile.put("gandhi", null);
		super.setAttackStrategy(aiProfiles);
	}

	public static void generateRPClass() {
		try {
			final RPClass goat = new RPClass("goat");
			goat.isA("creature");
			goat.addAttribute("weight", Type.BYTE);
		} catch (final SyntaxException e) {
			logger.error("cannot generate RPClass", e);
		}
	}

	/**
	 * Creates a new wild goat.
	 */
	public Goat() {
		this(null);

		// set the default perception range for enemy detection
		setPerceptionRange(20);

		// set the default movement range
		setMovementRange(20);
		updateSoundList();
	}

	/**
	 * Creates a new goat that is owned by a player.
	 * @param owner owning player, or <code>null</code>
	 */
	public Goat(final Player owner) {
		super();
		super.setOwner(owner);
		setRPClass("goat");
		put("type", "goat");

		initHP(HP);
		setUp();
		hunger = 0;
		timingAdjust = Rand.rand(10);

		if (owner != null) {
			// add goat to zone and create RPID to be used in setGoat()
			owner.getZone().add(this);
			owner.setGoat(this);
		}

		update();
		updateSoundList();
		logger.debug("Created Goat: " + this);
	}

	/**
	 * Creates a goat based on an existing goat RPObject, and assigns it to a
	 * player.
	 *
	 * @param object object containing the data for the goat
	 * @param owner
	 *            The player who should own the goat
	 */
	public Goat(final RPObject object, final Player owner) {
		super(object, owner);

		setRPClass("goat");
		put("type", "goat");
		hunger = 0;
		timingAdjust = Rand.rand(10);

		if (owner != null) {
			// add goat to zone and create RPID to be used in setGoat()
			owner.getZone().add(this);
			owner.setGoat(this);
		}

		update();
		updateSoundList();
		logger.debug("Created Goat: " + this);
	}

	@Override
	void setUp() {
		setHP(HP);
		setAtk(ATK);
		setDef(DEF);
		setXP(XP);
		incHP = 5;
		baseSpeed = 0.25;
	}

	/**
	 * Is called when the goat dies. Removes the dead goat from the owner.
	 *
	 */
	@Override
	public void onDead(final Killer killer, final boolean remove) {
		cleanUpGoat();
		super.onDead(killer, remove);
	}

	private void cleanUpGoat() {
		if (owner != null) {
			if (owner.hasGoat()) {
				owner.removeGoat(this);
			} else {
				logger.debug("INCOHERENCE: Goat " + this + " isn't owned by " + owner);
			}
		}
	}

	/**
	 * Returns a list of GoatFood in the given range ordered by distance.
	 *
	 * The first in list is the nearest.
	 *
	 * @param range
	 *            The maximum distance to a GoatFood
	 * @return a list of GoatFood or emptyList if none is found in the given
	 *         range
	 */
	protected List<GoatFood> getFoodinRange(final double range) {

		final List<GoatFood> resultList = new LinkedList<GoatFood>();

		for (final GoatFood food : getZone().getGoatFoodList()) {

			if ((food.getAmount() > 0) && (squaredDistance(food) < range * range)) {
				resultList.add(food);
			}
		}
		Collections.sort(resultList, new Comparator<GoatFood>() {
			@Override
			public int compare(final GoatFood o1, final GoatFood o2) {
				return Double.compare(squaredDistance(o1), squaredDistance(o2));

			}
		});
		return resultList;
	}

	/**
	 * Called when the goat is hungry.
	 *
	 * @return <code>true</code> if the goat is hunting for food.
	 */
	protected boolean onHungry() {
		/*
		 * Will try to eat if one of... - Food already on the mind and not
		 * moving (collision?) - Food not on the mind and hunger pains (every
		 * 10)
		 */
		if ("food".equals(getIdea())) {
			if (!stopped()) {
				return true;
			}
		} else {
			/*
			 * Only do something on occasional hunger pains
			 */
			if ((hunger % 10) != 0) {
				return false;
			}
		}

		return searchForFood();
	}

	protected boolean searchForFood() {

		final List<GoatFood> list = getFoodinRange(6);

		for (final GoatFood food : list) {
			if (food.nextTo(this)) {
				logger.debug("Goat eats");
				setIdea("eat");
				maybeMakeSound(15);
				eat(food);
				clearPath();
				stop();
				return true;
			} else {
				final List<Node> path = Path.searchPath(this, food, 6 * 6);
				if (path.size() != 0) {

					logger.debug("Goat moves to food");
					setIdea("food");
					maybeMakeSound(20);

					setPath(new FixedPath(path, false));
					return true;
				}

			}

		}
		setIdea(null);
		return false;
	}

	/**
	 * Called when the goat is idle.
	 */
	protected void onIdle() {
		final int turn = SingletonRepository.getRuleProcessor().getTurn() + timingAdjust;

		if (owner == null) {
			/*
			 * Check if player near (creature's enemy)
			 */
			if (((turn % 15) == 0) && isEnemyNear(getPerceptionRange())) {
				logger.debug("Goat (ownerless) moves randomly");
				setIdea("walk");
				maybeMakeSound(20);
				moveRandomly();
			} else {
				logger.debug("Goat sleeping");
				setIdea(null);
			}
		} else if (((turn % 10) == 0) && (hunger >= HUNGER_EXTREMELY_HUNGRY)) {
			setIdea("food");
			maybeMakeSound(20);
			setRandomPathFrom(owner.getX(), owner.getY(), getMovementRange());
			setSpeed(getBaseSpeed());
		} else if (!nextTo(owner)) {
			moveToOwner();
			maybeMakeSound(20);
		} else {
			if ((turn % 100) == 0) {
				logger.debug("Goat is bored");
				setRandomPathFrom(owner.getX(), owner.getY(), getMovementRange()/2);
				setSpeed(getBaseSpeed());
			} else {
				logger.debug("Goat has nothing to do");
				setIdea(null);
			}
		}
	}

	protected void onStarve() {
		if (weight > 0) {
			setWeight(weight - 1);
			updateSoundList();
		} else {
			delayedDamage(1, "starvation");
		}
		logger.warn("Goat starve " + getZone().getName() + " " + getX() + ": " + getY());
		hunger /= 2;
	}

	protected void eat(final GoatFood food) {
		final int amount = food.getAmount();

		if (amount > 0) {
			food.onFruitPicked(null);

			if (weight < MAX_WEIGHT) {
				setWeight(weight + 1);
				updateSoundList();
			}

			heal(incHP);
			hunger = 0;
		}
	}

	/**
	 * Determines what the goat shall do next.
	 */
	@Override
	public void logic() {
		if (!getZone().getPlayers().isEmpty()) {
			hunger++;
		}

		/*
		 * Allow owner to call goat (will override other reactions)
		 */
		if (isOwnerCallingMe()) {
			moveToOwner();
			maybeMakeSound(20);
		} else if (stopped()) {
			/*
			 * Hungry?
			 */
			if ((hunger < HUNGER_HUNGRY) || !onHungry()) {
				/*
				 * If not hunting for food, do other things
				 */
				onIdle();
			}
		} else if (hunger >= HUNGER_EXTREMELY_HUNGRY) {
			onHungry();
		}

		/*
		 * Starving?
		 */
		if (hunger >= HUNGER_STARVATION) {
			onStarve();

		}
		applyMovement();
		notifyWorldAboutChanges();
	}

	/**
	 * Update the available sound list according to goat weight.
	 */
	private void updateSoundList() {
		if (getWeight() > 50) {
			setSounds(largeGoatSounds);
		} else {
			setSounds(smallGoatSounds);
		}
	}

	@Override
	public String describe() {
		String text = "Oto goat. Wygląda na to, że waży około " + weight + ".";
		if (hasDescription()) {
			text = getDescription();
		}
		return (text);
	}
}