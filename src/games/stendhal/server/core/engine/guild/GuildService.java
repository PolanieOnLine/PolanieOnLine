/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.engine.guild;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.db.GuildDAO;
import games.stendhal.server.core.engine.db.GuildDAO.GuildMemberData;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DAORegister;

/**
 * Service orchestrating guild operations with centralized validation.
 */
public class GuildService {
	private static final int GUILD_TAG_MIN_LENGTH = 4;
	private static final int GUILD_TAG_MAX_LENGTH = 5;
	private static final int DESCRIPTION_MAX_LENGTH = 1000;
	private static final long DEFAULT_INVITE_TTL_HOURS = 48L;

	private final GuildDAO guildDAO;

	public static final class RequiredItem {
		private final String name;
		private final int quantity;

		public RequiredItem(final String name, final int quantity) {
			this.name = name;
			this.quantity = quantity;
		}

		public String getName() {
			return name;
		}

		public int getQuantity() {
			return quantity;
		}
	}

	public GuildService() {
		this(DAORegister.get().get(GuildDAO.class));
	}

	GuildService(final GuildDAO guildDAO) {
		this.guildDAO = guildDAO;
	}

	/**
	 * Flow used by NPC dialogs to create guilds with resource requirements.
	 */
	public int createGuildFromNpc(final Player player, final String guildName, final String guildTag,
			final String description, final int goldFee, final List<RequiredItem> requiredItems)
			throws SQLException {
		validateGuildLeader(player);
		verifyGoldFee(player, goldFee);
		verifyRequiredItems(player, requiredItems);

		final List<RequiredItem> withdrawnItems = new ArrayList<RequiredItem>();
		boolean goldWithdrawn = false;
		try {
			goldWithdrawn = takeGold(player, goldFee);
			takeItems(player, requiredItems, withdrawnItems);

			return createGuild(player.getID().getObjectID(), guildName, guildTag, description);
		} catch (RuntimeException e) {
			refundResources(player, goldWithdrawn ? goldFee : 0, withdrawnItems);
			throw e;
		} catch (SQLException e) {
			refundResources(player, goldWithdrawn ? goldFee : 0, withdrawnItems);
			throw e;
		}
	}

	public int createGuild(final int actorPlayerId, final String guildName, final String guildTag,
			final String description) throws SQLException {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			assertNotInAnyGuild(transaction, actorPlayerId);

			final String normalizedName = requireNonBlank(guildName, "guildName");
			final String normalizedTag = validateTag(guildTag);
			final String normalizedDescription = validateDescription(description);
			assertNameAndTagUnique(transaction, normalizedName, normalizedTag);

			final int guildId = guildDAO.createGuild(transaction, normalizedName, normalizedTag,
					normalizedDescription, actorPlayerId);
			guildDAO.addMember(transaction, guildId, actorPlayerId, "LEADER");
			guildDAO.logEvent(transaction, guildId, Integer.valueOf(actorPlayerId), "GUILD_CREATED",
					toJson(mapOf("name", normalizedName, "tag", normalizedTag)));

			TransactionPool.get().commit(transaction);
			return guildId;
		} catch (SQLException e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		} catch (RuntimeException e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}

	public void inviteMember(final int actorPlayerId, final int guildId, final int invitedPlayerId)
			throws SQLException {
		executeInTransaction(new GuildOperation() {
			@Override
			public void execute(final DBTransaction transaction) throws SQLException {
				assertRoleInGuild(transaction, actorPlayerId, guildId, "LEADER", "OFFICER");
				assertNotInAnyGuild(transaction, invitedPlayerId);

				if (guildDAO.hasActiveInvite(transaction, guildId, invitedPlayerId)) {
					throw new IllegalStateException("Player already has a pending invite to this guild.");
				}

				guildDAO.createInvite(transaction, guildId, invitedPlayerId, actorPlayerId,
						buildInviteExpiryTimestamp());
				guildDAO.logEvent(transaction, guildId, Integer.valueOf(actorPlayerId), "MEMBER_INVITED",
						toJson(mapOf("invitedPlayerId", Integer.valueOf(invitedPlayerId))));
			}
		});
	}

	public void acceptInvite(final int actorPlayerId, final int guildId) throws SQLException {
		executeInTransaction(new GuildOperation() {
			@Override
			public void execute(final DBTransaction transaction) throws SQLException {
				assertNotInAnyGuild(transaction, actorPlayerId);
				if (!guildDAO.hasActiveInvite(transaction, guildId, actorPlayerId)) {
					throw new IllegalStateException("No active invite for player in guild.");
				}

				guildDAO.addMember(transaction, guildId, actorPlayerId, "MEMBER");
				guildDAO.deleteInvite(transaction, guildId, actorPlayerId);
				guildDAO.logEvent(transaction, guildId, Integer.valueOf(actorPlayerId), "INVITE_ACCEPTED",
						toJson(mapOf("playerId", Integer.valueOf(actorPlayerId))));
			}
		});
	}

	public void kickMember(final int actorPlayerId, final int guildId, final int targetPlayerId)
			throws SQLException {
		executeInTransaction(new GuildOperation() {
			@Override
			public void execute(final DBTransaction transaction) throws SQLException {
				assertRoleInGuild(transaction, actorPlayerId, guildId, "LEADER");
				final GuildMemberData targetMembership = assertMemberInGuild(transaction, targetPlayerId, guildId);
				if ("LEADER".equals(targetMembership.getRole())) {
					throw new IllegalStateException("Leader cannot be kicked from guild.");
				}

				guildDAO.removeMember(transaction, guildId, targetPlayerId);
				guildDAO.logEvent(transaction, guildId, Integer.valueOf(actorPlayerId), "MEMBER_KICKED",
						toJson(mapOf("targetPlayerId", Integer.valueOf(targetPlayerId))));
			}
		});
	}

	public void leaveGuild(final int actorPlayerId, final int guildId) throws SQLException {
		executeInTransaction(new GuildOperation() {
			@Override
			public void execute(final DBTransaction transaction) throws SQLException {
				final GuildMemberData membership = assertMemberInGuild(transaction, actorPlayerId, guildId);
				if ("LEADER".equals(membership.getRole())) {
					throw new IllegalStateException("Leader cannot leave guild without transferring leadership.");
				}

				guildDAO.removeMember(transaction, guildId, actorPlayerId);
				guildDAO.logEvent(transaction, guildId, Integer.valueOf(actorPlayerId), "MEMBER_LEFT",
						toJson(mapOf("playerId", Integer.valueOf(actorPlayerId))));
			}
		});
	}

	public void transferLeadership(final int actorPlayerId, final int guildId, final int targetPlayerId)
			throws SQLException {
		executeInTransaction(new GuildOperation() {
			@Override
			public void execute(final DBTransaction transaction) throws SQLException {
				assertRoleInGuild(transaction, actorPlayerId, guildId, "LEADER");
				assertMemberInGuild(transaction, targetPlayerId, guildId);

				guildDAO.updateGuildLeader(transaction, guildId, targetPlayerId);
				guildDAO.updateMemberRole(transaction, guildId, actorPlayerId, "MEMBER");
				guildDAO.updateMemberRole(transaction, guildId, targetPlayerId, "LEADER");
				guildDAO.logEvent(transaction, guildId, Integer.valueOf(actorPlayerId), "LEADERSHIP_TRANSFERRED",
						toJson(mapOf("newLeaderPlayerId", Integer.valueOf(targetPlayerId))));
			}
		});
	}

	public void updateDescription(final int actorPlayerId, final int guildId, final String description)
			throws SQLException {
		executeInTransaction(new GuildOperation() {
			@Override
			public void execute(final DBTransaction transaction) throws SQLException {
				assertRoleInGuild(transaction, actorPlayerId, guildId, "LEADER", "OFFICER");
				final String normalizedDescription = validateDescription(description);

				guildDAO.updateDescription(transaction, guildId, normalizedDescription);
				guildDAO.logEvent(transaction, guildId, Integer.valueOf(actorPlayerId), "DESCRIPTION_UPDATED",
						toJson(mapOf("descriptionLength",
								Integer.valueOf(normalizedDescription == null ? 0 : normalizedDescription.length()))));
			}
		});
	}

	private void executeInTransaction(final GuildOperation operation) throws SQLException {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			operation.execute(transaction);
			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		} catch (RuntimeException e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}

	private void assertNameAndTagUnique(final DBTransaction transaction, final String name,
			final String tag) throws SQLException {
		if (guildDAO.guildNameExists(transaction, name)) {
			throw new IllegalStateException("Nazwa gildii jest już zajęta.");
		}
		if (guildDAO.guildTagExists(transaction, tag)) {
			throw new IllegalStateException("Tag gildii jest już zajęty.");
		}
	}

	private void assertNotInAnyGuild(final DBTransaction transaction, final int playerId)
			throws SQLException {
		if (guildDAO.loadMembership(transaction, playerId) != null) {
			throw new IllegalStateException("Jesteś już w gildii.");
		}
	}

	private void validateGuildLeader(final Player player) {
		if (player == null) {
			throw new IllegalArgumentException("Brak lidera do utworzenia gildii.");
		}
	}

	private void verifyGoldFee(final Player player, final int goldFee) {
		if (goldFee < 0) {
			throw new IllegalArgumentException("Niepoprawna opłata za założenie gildii.");
		}
		if (goldFee > 0 && !player.isEquipped("money", goldFee)) {
			throw new IllegalStateException("Brak środków.");
		}
	}

	private void verifyRequiredItems(final Player player, final List<RequiredItem> requiredItems) {
		for (RequiredItem item : safeRequiredItems(requiredItems)) {
			if (item.getQuantity() <= 0) {
				throw new IllegalArgumentException("Niepoprawna lista przedmiotów do założenia gildii.");
			}
			if (!player.isSubmittableEquipped(item.getName(), item.getQuantity())) {
				throw new IllegalStateException("Brak wymaganych przedmiotów.");
			}
		}
	}

	private boolean takeGold(final Player player, final int goldFee) {
		if (goldFee <= 0) {
			return false;
		}
		if (!player.drop("money", goldFee)) {
			throw new IllegalStateException("Brak środków.");
		}
		return true;
	}

	private void takeItems(final Player player, final List<RequiredItem> requiredItems,
			final List<RequiredItem> withdrawnItems) {
		for (RequiredItem item : safeRequiredItems(requiredItems)) {
			if (!player.dropSubmittable(item.getName(), item.getQuantity())) {
				throw new IllegalStateException("Brak wymaganych przedmiotów.");
			}
			withdrawnItems.add(item);
		}
	}

	private void refundResources(final Player player, final int refundedGold, final List<RequiredItem> refundedItems) {
		if (refundedGold > 0) {
			final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
			money.setQuantity(refundedGold);
			player.equipOrPutOnGround(money);
		}

		for (RequiredItem item : refundedItems) {
			final Item restoredItem = SingletonRepository.getEntityManager().getItem(item.getName());
			if (restoredItem instanceof StackableItem) {
				((StackableItem) restoredItem).setQuantity(item.getQuantity());
				player.equipOrPutOnGround(restoredItem);
				continue;
			}

			for (int i = 0; i < item.getQuantity(); i++) {
				player.equipOrPutOnGround(SingletonRepository.getEntityManager().getItem(item.getName()));
			}
		}
	}

	private List<RequiredItem> safeRequiredItems(final List<RequiredItem> requiredItems) {
		return requiredItems == null ? Collections.<RequiredItem>emptyList() : requiredItems;
	}

	private GuildMemberData assertMemberInGuild(final DBTransaction transaction, final int playerId,
			final int guildId) throws SQLException {
		final GuildMemberData membership = guildDAO.loadMembership(transaction, playerId);
		if (membership == null || membership.getGuildId() != guildId) {
			throw new IllegalStateException("Player is not a member of this guild.");
		}
		return membership;
	}

	private void assertRoleInGuild(final DBTransaction transaction, final int playerId, final int guildId,
			final String... allowedRoles) throws SQLException {
		final GuildMemberData membership = assertMemberInGuild(transaction, playerId, guildId);
		for (int i = 0; i < allowedRoles.length; i++) {
			if (allowedRoles[i].equals(membership.getRole())) {
				return;
			}
		}
		throw new IllegalStateException("Player does not have required guild role.");
	}

	private String validateTag(final String guildTag) {
		final String normalizedTag = requireNonBlank(guildTag, "guildTag");
		if (normalizedTag.length() < GUILD_TAG_MIN_LENGTH || normalizedTag.length() > GUILD_TAG_MAX_LENGTH) {
			throw new IllegalArgumentException("Guild tag length must be between " + GUILD_TAG_MIN_LENGTH
					+ " and " + GUILD_TAG_MAX_LENGTH + " characters.");
		}
		return normalizedTag;
	}

	private String validateDescription(final String description) {
		if (description == null) {
			return null;
		}
		final String normalizedDescription = description.trim();
		if (normalizedDescription.length() > DESCRIPTION_MAX_LENGTH) {
			throw new IllegalArgumentException("Guild description length cannot exceed "
					+ DESCRIPTION_MAX_LENGTH + " characters.");
		}
		return normalizedDescription.length() == 0 ? null : normalizedDescription;
	}

	private String requireNonBlank(final String value, final String fieldName) {
		if (value == null || value.trim().length() == 0) {
			throw new IllegalArgumentException(fieldName + " cannot be blank.");
		}
		return value.trim();
	}

	private static Map<String, Object> mapOf(final String firstKey, final Object firstValue,
			final String secondKey, final Object secondValue) {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put(firstKey, firstValue);
		map.put(secondKey, secondValue);
		return map;
	}

	private static Map<String, Object> mapOf(final String key, final Object value) {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put(key, value);
		return map;
	}

	private String toJson(final Map<String, Object> payload) {
		final StringBuilder json = new StringBuilder();
		json.append('{');
		boolean first = true;
		for (Map.Entry<String, Object> entry : payload.entrySet()) {
			if (!first) {
				json.append(',');
			}
			first = false;
			json.append('"').append(escape(entry.getKey())).append('"').append(':');
			final Object value = entry.getValue();
			if (value == null) {
				json.append("null");
			} else if (value instanceof Number || value instanceof Boolean) {
				json.append(value.toString());
			} else {
				json.append('"').append(escape(String.valueOf(value))).append('"');
			}
		}
		json.append('}');
		return json.toString();
	}

	private String escape(final String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private interface GuildOperation {
		void execute(DBTransaction transaction) throws SQLException;
	}

	private Timestamp buildInviteExpiryTimestamp() {
		return new Timestamp(System.currentTimeMillis() + DEFAULT_INVITE_TTL_HOURS * 60L * 60L * 1000L);
	}
}
