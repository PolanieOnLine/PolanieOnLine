/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.item.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Single source of truth for item-upgrade preview, validation and execution.
 */
public final class ItemUpgradeService {
	private static final Logger LOGGER =
			Logger.getLogger(ItemUpgradeService.class);
	private static final String DISCOUNT_QUEST = "ciupaga_trzy_wasy";
	private static final double KARMA_LIMIT = 0.1;
	private static final double KARMA_GRANULARITY = 0.01;
	private static final double FAILURE_REFUND = 0.4;

	private static final Map<Integer, Map<String, Integer>> MATERIALS_BY_LEVEL =
			createMaterialRequirements();

	private static final ItemUpgradeService INSTANCE =
			new ItemUpgradeService(new Random());

	private final Random random;
	private final Map<String, PendingAttempt> pendingAttempts =
			new ConcurrentHashMap<String, PendingAttempt>();

	public ItemUpgradeService(final Random random) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		this.random = random;
	}

	public static ItemUpgradeService getInstance() {
		return INSTANCE;
	}

	/** Returns carried/equipped upgrade candidates in stable display order. */
	public List<Item> findUpgradeCandidates(final Player player) {
		final List<Item> candidates = new ArrayList<Item>();
		if (player == null) {
			return candidates;
		}
		for (final RPSlot slot : player.slots(Slots.CARRYING)) {
			collectCandidates(slot, candidates);
		}
		Collections.sort(candidates, new Comparator<Item>() {
			@Override
			public int compare(final Item first, final Item second) {
				final int name = first.getName().compareToIgnoreCase(second.getName());
				if (name != 0) {
					return name;
				}
				final int level = Integer.compare(first.getUpgradeLevel(),
						second.getUpgradeLevel());
				if (level != 0) {
					return level;
				}
				return Integer.compare(first.getID().getObjectID(),
						second.getID().getObjectID());
			}
		});
		return candidates;
	}

	private void collectCandidates(final RPSlot slot,
			final List<Item> candidates) {
		for (final RPObject object : slot) {
			if (!(object instanceof Item)) {
				continue;
			}
			final Item item = (Item) object;
			if (item.hasUpgradeLimit() && item.getMaxUpgradeLevel() > 0) {
				candidates.add(item);
			}
			for (final RPSlot child : item.slots()) {
				collectCandidates(child, candidates);
			}
		}
	}

	/** Builds a non-consuming, exact preview and binds it to a one-use token. */
	public ItemUpgradePreview createPreview(final Player player,
			final Item item) {
		if (player == null || item == null) {
			clearPendingAttempt(player);
			return null;
		}

		final int currentLevel = item.getUpgradeLevel();
		final int maximumLevel = item.getMaxUpgradeLevel();
		final int nextLevel = Math.min(maximumLevel, currentLevel + 1);
		ItemUpgradeStats currentStats = ItemUpgradeStats.atLevel(item,
				currentLevel);
		ItemUpgradeStats upgradedStats = ItemUpgradeStats.atLevel(item,
				nextLevel);
		if (item.has("range")) {
			final int currentRange = item.getRangeAtUpgradeLevel(currentLevel);
			final int upgradedRange = item.getRangeAtUpgradeLevel(nextLevel);
			if (currentRange != upgradedRange) {
				currentStats = currentStats.with(ItemUpgradeStats.RANGE,
						currentRange);
				upgradedStats = upgradedStats.with(ItemUpgradeStats.RANGE,
						upgradedRange);
			}
		}
		if (item.has("rate")) {
			final int currentRate = item.getAttackRateAtUpgradeLevel(currentLevel);
			final int upgradedRate = item.getAttackRateAtUpgradeLevel(nextLevel);
			if (currentRate != upgradedRate) {
				currentStats = currentStats.with(ItemUpgradeStats.ATTACK_RATE,
						currentRate);
				upgradedStats = upgradedStats.with(ItemUpgradeStats.ATTACK_RATE,
						upgradedRate);
			}
		}

		final ItemUpgradeResult.Status baseStatus = validateBase(item);
		final ItemUpgradeRequirements requirements = createRequirements(player,
				item, baseStatus == ItemUpgradeResult.Status.READY);
		final double karmaModifier = baseStatus == ItemUpgradeResult.Status.READY
				? calculateKarmaModifier(player, item) : 0.0;
		final double successProbability = clampProbability(
				calculateBaseSuccessProbability(item) + karmaModifier);

		ItemUpgradeResult.Status blockingStatus = baseStatus;
		if (blockingStatus == ItemUpgradeResult.Status.READY
				&& !requirements.hasEnoughMoney()) {
			blockingStatus = ItemUpgradeResult.Status.NOT_ENOUGH_MONEY;
		}
		if (blockingStatus == ItemUpgradeResult.Status.READY
				&& !requirements.hasAllMaterials()) {
			blockingStatus = ItemUpgradeResult.Status.MISSING_RESOURCES;
		}

		final boolean allowed = blockingStatus == ItemUpgradeResult.Status.READY;
		final String token = allowed ? UUID.randomUUID().toString() : null;
		if (allowed) {
			pendingAttempts.put(player.getName(), new PendingAttempt(token, item,
					currentLevel, player.getKarma(), karmaModifier,
					requirements.getFee(), item.isPersistent(),
					snapshotContainment(item)));
		} else {
			clearPendingAttempt(player);
		}

		return new ItemUpgradePreview(item, token, displayName(item),
				item.getRarityOrCommon(), currentLevel, nextLevel, maximumLevel,
				currentStats, upgradedStats, requirements, successProbability,
				karmaModifier, allowed, blockingStatus);
	}

	/** Executes an upgrade only when the exact preview token is still current. */
	public ItemUpgradeResult performUpgrade(final Player player,
			final Item item, final String requestToken) {
		if (player == null || item == null || requestToken == null) {
			return result(ItemUpgradeResult.Status.INVALID_REQUEST);
		}

		synchronized (player) {
			final PendingAttempt pending = pendingAttempts.remove(player.getName());
			if (pending == null || !requestToken.equals(pending.token)) {
				return result(ItemUpgradeResult.Status.STALE_PREVIEW);
			}
			if (pending.item != item) {
				return result(ItemUpgradeResult.Status.INVALID_ITEM);
			}
			if (!hasSameContainment(item, pending.containmentSnapshot)) {
				return result(ItemUpgradeResult.Status.STALE_PREVIEW);
			}
			final ItemUpgradeResult.Status baseStatus = validateBase(item);
			if (baseStatus != ItemUpgradeResult.Status.READY) {
				return result(baseStatus);
			}
			if (item.getUpgradeLevel() != pending.upgradeLevel
					|| Double.compare(player.getKarma(), pending.karmaSnapshot) != 0
					|| calculateUpgradeFee(player, item) != pending.fee) {
				return result(ItemUpgradeResult.Status.STALE_PREVIEW);
			}

			final ItemUpgradeRequirements requirements = createRequirements(player,
					item, true);
			if (!requirements.hasEnoughMoney()) {
				return result(ItemUpgradeResult.Status.NOT_ENOUGH_MONEY);
			}
			if (!requirements.hasAllMaterials()) {
				return result(ItemUpgradeResult.Status.MISSING_RESOURCES);
			}

			final List<Map.Entry<String, Integer>> consumedMaterials =
					new ArrayList<Map.Entry<String, Integer>>();
			for (final Map.Entry<String, Integer> material
					: requirements.getMaterials().entrySet()) {
				if (!player.drop(material.getKey(), material.getValue())) {
					restoreMaterials(player, consumedMaterials);
					return result(ItemUpgradeResult.Status.TRANSACTION_FAILED);
				}
				consumedMaterials.add(material);
			}

			if (!MoneyUtils.removeMoney(player, requirements.getFee())) {
				restoreMaterials(player, consumedMaterials);
				return result(ItemUpgradeResult.Status.TRANSACTION_FAILED);
			}

			player.consumeKarmaModifier(pending.karmaModifier);
			if (isUpgradeSuccessful(calculateBaseSuccessProbability(item)
					+ pending.karmaModifier)) {
				try {
					item.upgrade();
					item.setPersistent(true);
				} catch (final RuntimeException e) {
					LOGGER.error("Unable to mutate upgraded item " + item.getName(), e);
					item.setUpgradeLevel(pending.upgradeLevel);
					item.setPersistent(pending.persistentSnapshot);
					restoreMaterials(player, consumedMaterials);
					MoneyUtils.giveMoney(player, requirements.getFee());
					player.addKarma(pending.karmaSnapshot - player.getKarma());
					return result(ItemUpgradeResult.Status.TRANSACTION_FAILED);
				}
				player.incUpgradedForItem(player.getName(), 1);
				player.incUpgradedForItem(item.getName(), 1);
				try {
					new GameEvent(player.getName(), "upgraded-item", item.getName(),
							"+" + item.getUpgradeLevel()).raise();
				} catch (final RuntimeException e) {
					LOGGER.error("Unable to log successful item upgrade", e);
				}
				return result(ItemUpgradeResult.Status.SUCCESS);
			}

			final int refund = (int) (requirements.getFee() * FAILURE_REFUND);
			MoneyUtils.giveMoney(player, refund);
			return new ItemUpgradeResult(ItemUpgradeResult.Status.FAILURE,
					"Ulepszenie nie powiodło się. Otrzymujesz "
					+ MoneyUtils.formatPrice(refund) + " rekompensaty.");
		}
	}

	public void clearPendingAttempt(final Player player) {
		if (player != null && player.getName() != null) {
			pendingAttempts.remove(player.getName());
		}
	}

	public int calculateUpgradeFee(final Player player, final Item item) {
		if (player == null || item == null || !item.isUpgradeable()) {
			return 0;
		}
		final int nextLevel = item.getUpgradeLevel() + 1;
		final int offense = Math.max(item.getAttack(), item.getRangedAttack());
		final long combatValue = Math.max(0L,
				(long) offense + item.getDefense());
		final int feePerLevel = item.getMaxUpgradeLevel() <= 3 ? 3000 : 5000;
		long fee = nextLevel * combatValue * feePerLevel;

		if (("sztylecik z mithrilu".equals(item.getName())
				&& item.getMaxUpgradeLevel() <= 2)
				|| item.getMaxUpgradeLevel() == 1) {
			fee = nextLevel * combatValue * 17400L;
		}
		if (item.getName().endsWith(" z mithrilu")
				&& item.getMaxUpgradeLevel() == 1) {
			fee = 5000000L;
		}
		if (player.isQuestCompleted(DISCOUNT_QUEST)) {
			fee = Math.round(fee * 0.7);
		}
		return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, fee));
	}

	public Map<String, Integer> getMaterialRequirements(final int level) {
		final Map<String, Integer> requirements = MATERIALS_BY_LEVEL.get(level);
		return requirements == null ? Collections.<String, Integer>emptyMap()
				: requirements;
	}

	public int getMaximumConfiguredLevel() {
		return MATERIALS_BY_LEVEL.size();
	}

	public double calculateBaseSuccessProbability(final Item item) {
		if (item == null) {
			return 0.0;
		}
		double probability = 1.0 - (0.1 * item.getUpgradeLevel());
		if (item.getUpgradeLevel() > 4) {
			probability = Math.max(probability, 0.2);
		}
		return clampProbability(probability);
	}

	private ItemUpgradeResult.Status validateBase(final Item item) {
		if (item == null) {
			return ItemUpgradeResult.Status.INVALID_ITEM;
		}
		if (!item.hasUpgradeLimit() || item.getMaxUpgradeLevel() <= 0) {
			return ItemUpgradeResult.Status.NOT_UPGRADEABLE;
		}
		if (item.isAtMaxUpgradeLevel()) {
			return ItemUpgradeResult.Status.MAX_LEVEL;
		}
		if (item.getMaxUpgradeLevel() > getMaximumConfiguredLevel()
				|| !MATERIALS_BY_LEVEL.containsKey(item.getUpgradeLevel() + 1)) {
			LOGGER.warn("Missing item upgrade requirements for " + item.getName()
					+ " level " + (item.getUpgradeLevel() + 1));
			return ItemUpgradeResult.Status.MISSING_CONFIGURATION;
		}
		return ItemUpgradeResult.Status.READY;
	}

	private ItemUpgradeRequirements createRequirements(final Player player,
			final Item item, final boolean configured) {
		final int fee = configured ? calculateUpgradeFee(player, item) : 0;
		final Map<String, Integer> materials = configured
				? getMaterialRequirements(item.getUpgradeLevel() + 1)
				: Collections.<String, Integer>emptyMap();
		final Map<String, Integer> owned = new LinkedHashMap<String, Integer>();
		for (final String material : materials.keySet()) {
			owned.put(material, countCarried(player, material));
		}
		return new ItemUpgradeRequirements(fee, MoneyUtils.formatPrice(fee),
				MoneyUtils.getTotalMoneyInCopper(player), materials, owned);
	}

	private int countCarried(final Player player, final String itemName) {
		long count = 0;
		for (final Item item : player.getAllEquipped(itemName)) {
			count += item.getQuantity();
		}
		return (int) Math.min(Integer.MAX_VALUE, count);
	}

	private double calculateKarmaModifier(final Player player,
			final Item item) {
		final double karma = player.getKarma();
		if (karma == 0.0) {
			return 0.0;
		}
		final double limit = karma < 0.0 ? Math.max(-KARMA_LIMIT, karma)
				: Math.min(KARMA_LIMIT, karma);
		long seed = 1125899906842597L;
		seed = seed * 31L + player.getName().hashCode();
		seed = seed * 31L + item.getID().getObjectID();
		seed = seed * 31L + item.getUpgradeLevel();
		seed = seed * 31L + Double.doubleToLongBits(karma);
		seed ^= seed >>> 33;
		seed *= 0xff51afd7ed558ccdL;
		seed ^= seed >>> 33;
		final double unit = (seed & 0x1fffffffffffffL)
				/ (double) 0x20000000000000L;
		final double score = (0.2 + unit * 0.8) * limit;
		return Math.floor(score / KARMA_GRANULARITY) * KARMA_GRANULARITY;
	}

	private boolean isUpgradeSuccessful(final double probability) {
		final int roll;
		synchronized (random) {
			roll = random.nextInt(100) + 1;
		}
		return roll <= clampProbability(probability) * 100.0;
	}

	private double clampProbability(final double probability) {
		return Math.max(0.0, Math.min(1.0, probability));
	}

	private String displayName(final Item item) {
		final StringBuilder result = new StringBuilder();
		result.append(item.getRarityOrCommon().getPolishDisplayName())
				.append(' ').append(item.getName());
		if (item.getUpgradeLevel() > 0) {
			result.append(" +").append(item.getUpgradeLevel());
		}
		return result.toString();
	}

	private List<ContainmentStep> snapshotContainment(final Item item) {
		final List<ContainmentStep> path = new ArrayList<ContainmentStep>();
		RPObject current = item;
		while (current != null) {
			path.add(new ContainmentStep(current, current.getContainerSlot()));
			current = current.getContainer();
		}
		return Collections.unmodifiableList(path);
	}

	private boolean hasSameContainment(final Item item,
			final List<ContainmentStep> snapshot) {
		RPObject current = item;
		int index = 0;
		while (current != null) {
			if (index >= snapshot.size()) {
				return false;
			}
			final ContainmentStep step = snapshot.get(index++);
			if (step.object != current
					|| step.slot != current.getContainerSlot()) {
				return false;
			}
			current = current.getContainer();
		}
		return index == snapshot.size();
	}

	private void restoreMaterials(final Player player,
			final List<Map.Entry<String, Integer>> consumed) {
		for (final Map.Entry<String, Integer> material : consumed) {
			try {
				final Item restored = SingletonRepository.getEntityManager()
						.getItem(material.getKey());
				if (restored instanceof StackableItem) {
					((StackableItem) restored).setQuantity(material.getValue());
				}
				player.equipOrPutOnGround(restored);
			} catch (final RuntimeException e) {
				LOGGER.error("Unable to roll back item upgrade material "
						+ material.getKey(), e);
			}
		}
	}

	public ItemUpgradeResult resultForStatus(
			final ItemUpgradeResult.Status status) {
		switch (status) {
		case SUCCESS:
			return new ItemUpgradeResult(status, "Przedmiot został ulepszony.");
		case INVALID_REQUEST:
			return new ItemUpgradeResult(status, "Nieprawidłowe żądanie ulepszenia.");
		case INVALID_ITEM:
			return new ItemUpgradeResult(status,
					"Wybrany przedmiot nie jest już dostępny.");
		case STALE_PREVIEW:
			return new ItemUpgradeResult(status,
					"Stan przedmiotu uległ zmianie. Podgląd został odświeżony.");
		case NOT_UPGRADEABLE:
			return new ItemUpgradeResult(status,
					"Tego przedmiotu nie można ulepszyć.");
		case MAX_LEVEL:
			return new ItemUpgradeResult(status,
					"Przedmiot osiągnął maksymalny poziom ulepszenia.");
		case MISSING_CONFIGURATION:
			return new ItemUpgradeResult(status,
					"Brakuje konfiguracji dla następnego poziomu ulepszenia.");
		case NOT_ENOUGH_MONEY:
			return new ItemUpgradeResult(status,
					"Nie masz wystarczającej ilości pieniędzy.");
		case MISSING_RESOURCES:
			return new ItemUpgradeResult(status,
					"Nie masz wszystkich wymaganych materiałów.");
		case TRANSACTION_FAILED:
			return new ItemUpgradeResult(status,
					"Transakcja nie mogła zostać zakończona. Koszt nie został pobrany.");
		case NPC_TOO_FAR:
			return new ItemUpgradeResult(status,
					"Musisz pozostać przy kowalu podczas ulepszania.");
		case NPC_BUSY:
			return new ItemUpgradeResult(status,
					"Kowal rozmawia teraz z innym graczem.");
		case FAILURE:
		default:
			return new ItemUpgradeResult(status, "Ulepszenie nie powiodło się.");
		}
	}

	private ItemUpgradeResult result(final ItemUpgradeResult.Status status) {
		return resultForStatus(status);
	}

	private static Map<Integer, Map<String, Integer>> createMaterialRequirements() {
		final Map<Integer, Map<String, Integer>> levels =
				new LinkedHashMap<Integer, Map<String, Integer>>();
		levels.put(1, materials("polano", 5, "szafir", 1));
		levels.put(2, materials("polano", 7, "szafir", 2, "ametyst", 1));
		levels.put(3, materials("polano", 9, "szafir", 2, "ametyst", 2,
				"szmaragd", 1));
		levels.put(4, materials("polano", 12, "szafir", 5, "ametyst", 3,
				"szmaragd", 3, "rubin", 1));
		levels.put(5, materials("polano", 15, "ametyst", 5, "szmaragd", 5,
				"rubin", 3, "obsydian", 1));
		levels.put(6, materials("polano", 15, "ametyst", 7, "szmaragd", 7,
				"rubin", 6, "obsydian", 3));
		levels.put(7, materials("polano", 15, "ametyst", 8, "rubin", 7,
				"obsydian", 5, "diament", 1));
		levels.put(8, materials("polano", 15, "ametyst", 10, "rubin", 8,
				"obsydian", 5, "diament", 2));
		return Collections.unmodifiableMap(levels);
	}

	private static Map<String, Integer> materials(final Object... values) {
		final Map<String, Integer> materials =
				new LinkedHashMap<String, Integer>();
		for (int index = 0; index < values.length; index += 2) {
			materials.put((String) values[index], (Integer) values[index + 1]);
		}
		return Collections.unmodifiableMap(materials);
	}

	private static final class PendingAttempt {
		private final String token;
		private final Item item;
		private final int upgradeLevel;
		private final double karmaSnapshot;
		private final double karmaModifier;
		private final int fee;
		private final boolean persistentSnapshot;
		private final List<ContainmentStep> containmentSnapshot;

		private PendingAttempt(final String token, final Item item,
				final int upgradeLevel, final double karmaSnapshot,
				final double karmaModifier, final int fee,
				final boolean persistentSnapshot,
				final List<ContainmentStep> containmentSnapshot) {
			this.token = token;
			this.item = item;
			this.upgradeLevel = upgradeLevel;
			this.karmaSnapshot = karmaSnapshot;
			this.karmaModifier = karmaModifier;
			this.fee = fee;
			this.persistentSnapshot = persistentSnapshot;
			this.containmentSnapshot = containmentSnapshot;
		}
	}

	private static final class ContainmentStep {
		private final RPObject object;
		private final RPSlot slot;

		private ContainmentStep(final RPObject object, final RPSlot slot) {
			this.object = object;
			this.slot = slot;
		}
	}
}
