package games.stendhal.server.core.events.advent;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import games.stendhal.common.Constants;
import games.stendhal.server.core.engine.db.AdventCalendarDAO;
import games.stendhal.server.core.engine.db.AdventCalendarDAO.Account;
import games.stendhal.server.core.events.seasonal.SeasonalEventType;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;

/** One authoritative calendar for every character on an account. */
public final class AdventCalendarService {
	public static final String DEBUG_PROPERTY = "stendhal.advent.debug";
	// Match the level required to summon items; support access alone is insufficient.
	private static final int DEBUG_ADMIN_LEVEL = 35;

	public interface Accounts {
		AccountState load(Player player, int year) throws SQLException;
	}

	public static final class AccountState {
		public final int id;
		public final int mask;

		public AccountState(final int id, final int mask) {
			this.id = id;
			this.mask = mask;
		}
	}

	private final Clock clock;
	private final Accounts accounts;
	private final IntFunction<List<Item>> rewards;
	// Includes gifts not yet written by the regular character save queue.
	// Their account records are saved in the same transaction as the inventory.
	private final Map<Long, Integer> pendingClaims = new HashMap<>();

	public AdventCalendarService() {
		this(Clock.systemUTC(), AdventCalendarService::loadAccount, AdventCalendarRewards::createItems);
	}

	public AdventCalendarService(final Clock clock, final Accounts accounts,
			final IntFunction<List<Item>> rewards) {
		this.clock = clock;
		this.accounts = accounts;
		this.rewards = rewards;
	}

	private static AccountState loadAccount(final Player player, final int year) throws SQLException {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		try {
			final AdventCalendarDAO dao = new AdventCalendarDAO();
			final Account account = dao.accountFor(transaction, player.getName());
			final int mask = dao.claimedMask(transaction, account.id, year);
			pool.commit(transaction);
			return new AccountState(account.id, mask);
		} catch (final SQLException | RuntimeException exception) {
			pool.rollback(transaction);
			throw exception;
		}
	}

	public Result process(final Player player, final String command, final int day) throws SQLException {
		final LocalDate date = AdventCalendarRules.dateAt(clock.instant());
		final boolean debug = Boolean.getBoolean(DEBUG_PROPERTY) && player.getAdminLevel() >= DEBUG_ADMIN_LEVEL;
		if (!SeasonalEventType.CHRISTMAS.isEnabled() && !debug) {
			return new Result(date, 0, "inactive",
					"Kalendarz jest dostępny tylko podczas wydarzenia świątecznego.",
					0, Collections.emptyList(), false);
		}
		final AccountState account = accounts.load(player, date.getYear());
		final long key = ((long) date.getYear() << 32) | (account.id & 0xffffffffL);
		int mask = account.mask | pendingClaims.getOrDefault(key, 0);
		if ("open".equals(command) || "refresh".equals(command)) {
			final String message = debug
					? "Możesz otworzyć dowolne okienko. Nagrody są przyznawane raz na konto."
					: date.getMonthValue() != 12
					? "Kalendarz otwiera się 1 grudnia. Wróć wtedy po pierwszy prezent."
					: date.getDayOfMonth() > 24 + AdventCalendarRules.CATCH_UP_DAYS
					? "Tegoroczny kalendarz dobiegł końca. Do zobaczenia za rok!"
					: date.getDayOfMonth() > 24 ? "Możesz jeszcze odebrać zaległe prezenty."
					: "Otwórz dzisiejsze okienko lub odbierz zaległy prezent.";
			return new Result(date, mask, "ok", message, 0, Collections.emptyList(), debug);
		}
		final String state = AdventCalendarRules.state(date, day, mask, debug);
		if ("view".equals(command) && "claimed".equals(state)) {
			return new Result(date, mask, "claimed", "Ten prezent został już odebrany na twoim koncie.",
					day, Collections.emptyList(), debug);
		}
		if (!"claim".equals(command) || !"available".equals(state)) {
			final String message = "claimed".equals(state) ? "Ten prezent został już odebrany na twoim koncie."
					: "expired".equals(state) ? "Minął już termin odbioru tego prezentu."
					: "future".equals(state) ? "Na ten prezent trzeba jeszcze poczekać."
					: "Nie można otworzyć tego okienka.";
			return new Result(date, mask, state, message, 0, Collections.emptyList(), debug);
		}
		final int ownMask = ownMask(player, date.getYear());
		final List<Item> items = rewards.apply(day);
		for (final Item item : items) {
			if (item instanceof StackableItem) {
				continue;
			}
			for (final String slot : Constants.EQUIPMENT_SLOTS) {
				if (item.getPossibleSlots().contains(slot)) {
					item.setBoundTo(player.getName());
					break;
				}
			}
		}
		if (!AdventInventoryGrant.give(player, items)) {
			return new Result(date, mask, "no_space",
					"Zwolnij miejsce w plecaku, aby odebrać świąteczny prezent.", 0, Collections.emptyList(), debug);
		}
		final int bit = 1 << (day - 1);
		player.setQuest(AdventCalendarDAO.QUEST_SLOT, date.getYear() + ";" + (ownMask | bit));
		mask |= bit;
		pendingClaims.put(key, mask);
		player.notifyWorldAboutChanges();
		return new Result(date, mask, "received", "Świąteczny prezent trafił do twojego plecaka.", day, items, debug);
	}

	private int ownMask(final Player player, final int year) {
		if (player.hasQuest(AdventCalendarDAO.QUEST_SLOT)) {
			final String[] fields = player.getQuest(AdventCalendarDAO.QUEST_SLOT).split(";", -1);
			if (fields.length == 2 && Integer.toString(year).equals(fields[0])) {
				try {
					return Integer.parseInt(fields[1]) & AdventCalendarDAO.ALL_DAYS;
				} catch (final NumberFormatException exception) {
					throw new IllegalStateException("Invalid advent calendar marker", exception);
				}
			}
		}
		return 0;
	}

	public static final class Result {
		public final LocalDate date;
		public final int mask;
		public final String status;
		public final String message;
		public final int revealedDay;
		public final List<Item> items;
		private final boolean debug;

		private Result(final LocalDate date, final int mask, final String status, final String message,
				final int revealedDay, final List<Item> items, final boolean debug) {
			this.date = date;
			this.mask = mask;
			this.status = status;
			this.message = debug ? "Tryb testowy administratora. " + message : message;
			this.revealedDay = revealedDay;
			this.items = Collections.unmodifiableList(items);
			this.debug = debug;
		}

		/** Both clients receive exactly the eligibility used to process claims. */
		public String stateForDay(final int day) {
			return "inactive".equals(status) ? "inactive" : AdventCalendarRules.state(date, day, mask, debug);
		}
	}
}
