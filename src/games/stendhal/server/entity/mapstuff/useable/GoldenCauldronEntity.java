/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.EntitySlot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import org.apache.log4j.Logger;

/**
 * A brewing cauldron used in the golden ciupaga quest line.
 */
public class GoldenCauldronEntity extends UseableEntity {
	private static final Logger LOGGER = Logger.getLogger(GoldenCauldronEntity.class);

	private static final String RPCLASS_NAME = "golden_cauldron";
	private static final String SLOT_CONTENT = "content";
	private static final int SLOT_CAPACITY = 8;
	private static final int STATE_IDLE = 0;
	private static final int STATE_ACTIVE = 1;
	private static final int RESET_DELAY = 20;

	private static final Map<String, Integer> RECIPE = new LinkedHashMap<String, Integer>();
	static {
		RECIPE.put("pióro azazela", 3);
		RECIPE.put("magiczna krew", 10);
		RECIPE.put("smocza krew", 5);
		RECIPE.put("cudowna krew", 1);
	}

	private String brewer;
	private TurnListener resetTask;

	/**
	 * Create a new cauldron instance.
	 */
	public GoldenCauldronEntity() {
		super();
		setRPClass(RPCLASS_NAME);
		setEntityClass("useable_entity");
		setEntitySubclass(RPCLASS_NAME);
		setSize(1, 2);
		put("name", RPCLASS_NAME);
		setStatus("Kocioł jest wygaszony. Wlej składniki, aby przygotować wywar.");
		setState(STATE_IDLE);

		final CauldronSlot slot = new CauldronSlot();
		slot.setCapacity(SLOT_CAPACITY);
		addSlot(slot);
	}

	/**
	 * Ensure the RPClass exists.
	 */
	public static void generateRPClass() {
		if (!RPClass.hasRPClass(RPCLASS_NAME)) {
			final RPClass rpClass = new RPClass(RPCLASS_NAME);
			rpClass.isA("useable_entity");
			rpClass.addAttribute("open", Type.FLAG);
			rpClass.addAttribute("brewer", Type.STRING);
			rpClass.addAttribute("status", Type.LONG_STRING);
			rpClass.addRPSlot(SLOT_CONTENT, SLOT_CAPACITY);
		}
	}

	@Override
	public String getDescriptionName() {
		return "kocioł";
	}

	@Override
	public void update() {
		super.update();
		brewer = get("brewer");
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (!(user instanceof Player)) {
			return false;
		}

		final Player player = (Player) user;

		if (!player.nextTo(this)) {
			player.sendPrivateText("Musisz podejść bliżej kotła.");
			return false;
		}

		if (!isOpen()) {
			openFor(player);
			return true;
		}

		if (!isControlledBy(player)) {
			player.sendPrivateText("Ktoś już miesza w tym kotle.");
			return false;
		}

		close();
		return true;
	}

	private boolean isOpen() {
		return has("open");
	}

	private void openFor(final Player player) {
		if (isOpen()) {
			return;
		}

		brewer = player.getName();
		put("open", "");
		put("brewer", brewer);
		setStatus("Draconia kiwa głową, gdy zaczynasz przygotowania.");
		notifyWorldAboutChanges();
		player.sendPrivateText("Otwierasz kocioł Draconii.");
	}

	private void close() {
		if (!isOpen()) {
			return;
		}

		remove("open");
		remove("brewer");
		brewer = null;
		setStatus("Kocioł stygnie, a płomień gaśnie.");
		notifyWorldAboutChanges();
	}

	private boolean isControlledBy(final Player player) {
		return (brewer != null) && brewer.equalsIgnoreCase(player.getName());
	}

	private void setStatus(final String text) {
		put("status", text);
	}

	/**
	 * Start brewing the quest potion.
	 *
	 * @param player controlling player
	 */
	public void mix(final Player player) {
		if (!isControlledBy(player)) {
			player.sendPrivateText(
				"Najpierw poproś Draconię o pozwolenie i otwórz kocioł.");
			return;
		}

		if (!player.nextTo(this)) {
			player.sendPrivateText("Musisz stać bliżej kotła, aby mieszać składniki.");
			return;
		}

		final RPSlot slot = getSlot(SLOT_CONTENT);
		if (slot == null) {
			LOGGER.error("Golden cauldron missing content slot");
			player.sendPrivateText("Kocioł nie jest gotowy do mieszania.");
			return;
		}

		final List<String> missing = getMissingIngredients(slot);
		if (!missing.isEmpty()) {
			player.sendPrivateText(
				"Brakuje składników: " + String.join(", ", missing) + ".");
			setStatus(
				"W kotle brakuje składników: " + String.join(", ", missing) + ".");
			notifyWorldAboutChanges();
			return;
		}

		if (!consumeIngredients(slot)) {
			player.sendPrivateText("Coś poszło nie tak i składniki rozsypały się.");
			return;
		}

		final Item result =
			SingletonRepository.getEntityManager().getItem("wywar wąsatych smoków");
		if (result == null) {
			LOGGER.error("Unable to create wywar wąsatych smoków item");
			player.sendPrivateText("Kocioł syczy, ale nic się nie wydarza.");
			return;
		}

		slot.add(result);
		setState(STATE_ACTIVE);
		setStatus("Płomienie buchają spod kotła, a wywar nabiera wąsatego aromatu!");
		player.sendPrivateText("Wywar wąsatych smoków jest gotowy.");
		notifyWorldAboutChanges();
		scheduleReset();
	}

	private void scheduleReset() {
		final TurnNotifier notifier = SingletonRepository.getTurnNotifier();
		if (resetTask != null) {
			notifier.dontNotify(resetTask);
		}

		resetTask = new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				resetTask = null;
				setState(STATE_IDLE);
				setStatus("Kocioł znów jest wygaszony.");
				notifyWorldAboutChanges();
			}
		};

		notifier.notifyInSeconds(RESET_DELAY, resetTask);
	}

	private List<String> getMissingIngredients(final RPSlot slot) {
		final Map<String, Integer> present = new LinkedHashMap<String, Integer>();

		for (final RPObject obj : slot) {
			if (obj instanceof Item) {
				final Item item = (Item) obj;
				final String name = item.getName();
				int quantity = 1;
				if (item instanceof StackableItem) {
					quantity = ((StackableItem) item).getQuantity();
				}
				present.put(name, present.getOrDefault(name, 0) + quantity);
			}
		}

		final List<String> missing = new ArrayList<String>();
		for (final Map.Entry<String, Integer> entry : RECIPE.entrySet()) {
			final int have = present.getOrDefault(entry.getKey(), 0);
			if (have < entry.getValue()) {
				missing.add(Grammar.quantityplnounWithHash(
					entry.getValue() - have, entry.getKey()));
			}
		}

		return missing;
	}

	private boolean consumeIngredients(final RPSlot slot) {
		for (final Map.Entry<String, Integer> entry : RECIPE.entrySet()) {
			int remaining = entry.getValue();
			final List<Item> toRemove = new ArrayList<Item>();

			for (final RPObject obj : slot) {
				if (!(obj instanceof Item)) {
					continue;
				}
				final Item item = (Item) obj;
				if (!item.getName().equals(entry.getKey())) {
					continue;
				}

				if (item instanceof StackableItem) {
					final StackableItem stack = (StackableItem) item;
					final int use = Math.min(stack.getQuantity(), remaining);
					final int left = stack.sub(use);
					remaining -= use;
					if (left <= 0) {
						toRemove.add(stack);
					}
				} else {
					remaining--;
					toRemove.add(item);
				}

				if (remaining == 0) {
					break;
				}
			}

			if (remaining > 0) {
				return false;
			}

			for (final Item item : toRemove) {
				item.removeFromWorld();
			}
		}
		return true;
	}

	private final class CauldronSlot extends EntitySlot {
		CauldronSlot() {
			super(SLOT_CONTENT, SLOT_CONTENT);
		}

		@Override
		public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
			if (!(entity instanceof Player)) {
				setErrorMessage("Tylko alchemicy mogą sięgać do kotła.");
				return false;
			}
			final Player player = (Player) entity;
			if (!player.nextTo(GoldenCauldronEntity.this)) {
				setErrorMessage("Musisz podejść bliżej kotła.");
				return false;
			}
			if (!isControlledBy(player)) {
				if (brewer == null) {
					setErrorMessage("Tylko aktualny mistrz mikstur może "
						+ "dotykać kotła.");
				} else {
					setErrorMessage("Kocioł obsługuje teraz " + brewer + ".");
				}
				return false;
			}
			clearErrorMessage();
			return true;
		}

		@Override
		public boolean isReachableForThrowingThingsIntoBy(final Entity entity) {
			return isReachableForTakingThingsOutOfBy(entity);
		}
	}
}
