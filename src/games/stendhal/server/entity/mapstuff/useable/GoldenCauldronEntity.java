/***************************************************************************
 *               (C) Copyright 2024-2025 - PolanieOnLine                   *
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
	private static final String ATTR_READY_IN = "ready_in";
	private static final String ATTR_STARTED_AT = "ready_started_at";
	private static final int SLOT_CAPACITY = 8;
	private static final int STATE_IDLE = 0;
	private static final int STATE_ACTIVE = 1;
	private static final int BREW_TIME_SECONDS = 5 * 60;
	private static final long BREW_TIME_MILLIS = BREW_TIME_SECONDS * 1000L;
	private static final String STATUS_IDLE = "Kocioł nie pracuje.";
	private static final String STATUS_WORKING = "";
	private static final String STATUS_READY = "Wywar gotowy.";
	private static final String STATUS_WAITING_FOR_PICKUP = "Wyjmij przygotowany wywar z kotła.";

	private static final Map<String, Integer> RECIPE = new LinkedHashMap<String, Integer>();
	static {
		RECIPE.put("pióro azazela", 3);
		RECIPE.put("magiczna krew", 10);
		RECIPE.put("smocza krew", 5);
		RECIPE.put("cudowna krew", 1);
	}

	private String brewer;
	private long readyAt;
	private TurnListener brewTask;

	/**
	* Create a new cauldron instance.
	*/
	public GoldenCauldronEntity() {
		super();
		setRPClass(RPCLASS_NAME);
		setEntityClass("useable_entity");
		setEntitySubclass(RPCLASS_NAME);
		setSize(2, 2);
		put("name", "Kocioł Draconii");
		setStatus(STATUS_IDLE);
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
			rpClass.addAttribute("ready_at", Type.LONG);
			rpClass.addAttribute(ATTR_READY_IN, Type.INT);
			rpClass.addAttribute(ATTR_STARTED_AT, Type.LONG);
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
		readyAt = has("ready_at") ? getLong("ready_at") : 0L;
		if (!has("ready_at") && has(ATTR_STARTED_AT)) {
			final long startedAt = getLong(ATTR_STARTED_AT);
			final long candidate = startedAt + BREW_TIME_MILLIS;
			if (candidate > System.currentTimeMillis()) {
				readyAt = candidate;
				put("ready_at", readyAt);
				put(ATTR_READY_IN, secondsUntilReady());
				notifyWorldAboutChanges();
			} else {
				remove(ATTR_STARTED_AT);
			}
		}

		if (isBrewing()) {
			if (readyAt <= System.currentTimeMillis()) {
				finishBrewing();
			} else {
				if (brewTask == null) {
					scheduleCompletion(secondsUntilReady());
				}
				refreshReadyInAttribute();
			}
		} else {
			if (clearReadyInAttribute()) {
				notifyWorldAboutChanges();
			}
			releaseReservationIfIdle();
		}
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
			if ((brewer != null) && !isControlledBy(player)) {
				player.sendPrivateText("Kocioł jest teraz zarezerwowany przez " + brewer + ".");
				return false;
			}
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
		if (isBrewing()) {
			setStatus(STATUS_WORKING);
		} else {
			setStatus(STATUS_IDLE);
		}
		notifyWorldAboutChanges();
		player.sendPrivateText("Otwierasz kocioł Draconii.");
	}

	private void close() {
		if (!isOpen()) {
			return;
		}

		remove("open");
		if (!isBrewing() && isContentEmpty()) {
			clearBrewer();
			setStatus(STATUS_IDLE);
		} else if (!isBrewing()) {
			setStatus(STATUS_WAITING_FOR_PICKUP);
		}
		notifyWorldAboutChanges();
	}

	private boolean isControlledBy(final Player player) {
		return (brewer != null) && brewer.equalsIgnoreCase(player.getName());
	}

	private boolean isBrewing() {
		return readyAt > System.currentTimeMillis();
	}

	private boolean isContentEmpty() {
		final RPSlot slot = getSlot(SLOT_CONTENT);
		return (slot == null) || slot.isEmpty();
	}

	private void clearBrewer() {
		remove("brewer");
		brewer = null;
	}

	private void setReadyAt(final long timestamp) {
		readyAt = timestamp;
		if (timestamp > 0) {
			put("ready_at", timestamp);
			put(ATTR_READY_IN, secondsUntilReady());
		} else {
			remove("ready_at");
			remove(ATTR_READY_IN);
			remove(ATTR_STARTED_AT);
		}
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
			player.sendPrivateText("Najpierw poproś Draconię o pozwolenie i otwórz kocioł.");
			return;
		}

		if (!player.nextTo(this)) {
			player.sendPrivateText("Musisz stać bliżej kotła, aby mieszać składniki.");
			return;
		}

		if (isBrewing()) {
			final String timeLeft = formatTimeRemaining();
			player.sendPrivateText("Kocioł już pracuje. Wywar będzie gotowy za " + timeLeft + ".");
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

		setState(STATE_ACTIVE);
		setStatus(STATUS_WORKING);
		final long startTime = System.currentTimeMillis();
		put(ATTR_STARTED_AT, startTime);
		setReadyAt(startTime + BREW_TIME_MILLIS);
		player.sendPrivateText("Rozpoczynasz warzenie. Wywar będzie gotowy za 5 minut.");
		notifyWorldAboutChanges();
		scheduleCompletion(BREW_TIME_SECONDS);
	}

	private void scheduleCompletion(final int delaySeconds) {
		final TurnNotifier notifier = SingletonRepository.getTurnNotifier();
		if (brewTask != null) {
			notifier.dontNotify(brewTask);
		}

		brewTask = new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				brewTask = null;
				finishBrewing();
			}
		};

		notifier.notifyInSeconds(Math.max(1, delaySeconds), brewTask);
	}

	private void finishBrewing() {
		if (brewTask != null) {
			SingletonRepository.getTurnNotifier().dontNotify(brewTask);
			brewTask = null;
		}
		if (!has("ready_at")) {
			setReadyAt(0);
			return;
		}

		final RPSlot slot = getSlot(SLOT_CONTENT);
		if (slot == null) {
			LOGGER.error("Golden cauldron missing content slot during completion");
			setReadyAt(0);
			setState(STATE_IDLE);
			setStatus(STATUS_IDLE);
			notifyWorldAboutChanges();
			return;
		}

		final Item result = SingletonRepository.getEntityManager().getItem("wywar wąsatych smoków");
		if (result == null) {
			LOGGER.error("Unable to create wywar wąsatych smoków item");
			setReadyAt(0);
			setState(STATE_IDLE);
			setStatus("Kocioł syczy, ale nic się nie wydarza.");
			notifyWorldAboutChanges();
			return;
		}

		result.setBoundTo(brewer);
		slot.add(result);
		setReadyAt(0);
		setState(STATE_IDLE);
		setStatus(STATUS_READY);

		if (brewer != null) {
			final Player player = SingletonRepository.getRuleProcessor().getPlayer(brewer);
			if (player != null) {
				player.sendPrivateText("Wywar wąsatych smoków jest gotowy w kotle.");
			}
		}

		notifyWorldAboutChanges();
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

	private void refreshReadyInAttribute() {
		final int seconds = secondsUntilReady();
		if (!has(ATTR_READY_IN) || getInt(ATTR_READY_IN) != seconds) {
			put(ATTR_READY_IN, seconds);
			notifyWorldAboutChanges();
		}
	}

	private boolean clearReadyInAttribute() {
		if (has(ATTR_READY_IN)) {
			remove(ATTR_READY_IN);
			return true;
		}
		return false;
	}

	private void releaseReservationIfIdle() {
		if ((brewer == null) || !isContentEmpty() || isOpen()) {
			return;
		}

		clearBrewer();
		setStatus(STATUS_IDLE);
		notifyWorldAboutChanges();
	}

	private int secondsUntilReady() {
		if (readyAt <= 0) {
			if (has(ATTR_STARTED_AT)) {
				final long startedAt = getLong(ATTR_STARTED_AT);
				final long diff = (startedAt + BREW_TIME_MILLIS) - System.currentTimeMillis();
				if (diff > 0) {
					final long seconds = diff / 1000L;
					return (int) Math.max(1L, seconds);
				}
			}
			return 0;
		}
		final long diff = readyAt - System.currentTimeMillis();
		if (diff <= 0) {
			return 0;
		}
		final long seconds = diff / 1000L;
		return (int) Math.max(1L, seconds);
	}

	private String formatTimeRemaining() {
		final long remaining = readyAt - System.currentTimeMillis();
		if (remaining <= 0) {
			return "moment";
		}

		final long totalSeconds = Math.max(0, remaining / 1000L);
		final long minutes = totalSeconds / 60L;
		final long seconds = totalSeconds % 60L;

		if (minutes > 0) {
			return minutes + " min " + seconds + " s";
		}
		return seconds + " s";
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
			if (!isOpen()) {
				setErrorMessage("Najpierw otwórz kocioł, aby sięgnąć do środka.");
				return false;
			}
			if (!isControlledBy(player)) {
				if (brewer == null) {
					setErrorMessage("Poproś Draconię o pozwolenie na mieszanie składników.");
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
			if (!(entity instanceof Player)) {
				setErrorMessage("Tylko alchemicy mogą sięgać do kotła.");
				return false;
			}

			final Player player = (Player) entity;
			if (!player.nextTo(GoldenCauldronEntity.this)) {
				setErrorMessage("Musisz podejść bliżej kotła.");
				return false;
			}
			if (!isOpen()) {
				setErrorMessage("Najpierw otwórz kocioł, aby wrzucić składniki.");
				return false;
			}

			if (!isControlledBy(player)) {
				clearErrorMessage();
				return true;
			}

			clearErrorMessage();
			return true;
		}
	}
}
