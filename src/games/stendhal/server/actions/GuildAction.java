package games.stendhal.server.actions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.db.GuildDAO.GuildData;
import games.stendhal.server.core.engine.db.GuildDAO.GuildMemberView;
import games.stendhal.server.core.engine.guild.GuildService;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

/**
 * Handles /guild subcommands sent by client as server extension command.
 */
public class GuildAction implements ActionListener {
	private static final String CMD_CREATE = "create";
	private static final String CMD_INVITE = "invite";
	private static final String CMD_ACCEPT = "accept";
	private static final String CMD_LEAVE = "leave";
	private static final String CMD_KICK = "kick";
	private static final String CMD_PROMOTE = "promote";
	private static final String CMD_DEMOTE = "demote";
	private static final String CMD_TRANSFER = "transfer";
	private static final String CMD_INFO = "info";

	private final GuildService guildService = new GuildService();

	public static void register() {
		CommandCenter.register("guild", new GuildAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final String subCommand = normalize(action.get("target"));
		final String args = normalize(action.get("args"));
		if (subCommand == null) {
			sendUsage(player);
			return;
		}

		try {
			if (CMD_CREATE.equals(subCommand)) {
				handleCreate(player, args);
			} else if (CMD_INVITE.equals(subCommand)) {
				handleInvite(player, args);
			} else if (CMD_ACCEPT.equals(subCommand)) {
				handleAccept(player, args);
			} else if (CMD_LEAVE.equals(subCommand)) {
				handleLeave(player);
			} else if (CMD_KICK.equals(subCommand)) {
				handleKick(player, args);
			} else if (CMD_PROMOTE.equals(subCommand)) {
				handlePromote(player, args);
			} else if (CMD_DEMOTE.equals(subCommand)) {
				handleDemote(player, args);
			} else if (CMD_TRANSFER.equals(subCommand)) {
				handleTransfer(player, args);
			} else if (CMD_INFO.equals(subCommand)) {
				handleInfo(player);
			} else {
				sendUsage(player);
			}
		} catch (IllegalArgumentException e) {
			player.sendPrivateText(NotificationType.ERROR, e.getMessage());
		} catch (IllegalStateException e) {
			player.sendPrivateText(NotificationType.ERROR, e.getMessage());
		} catch (SQLException e) {
			player.sendPrivateText(NotificationType.ERROR, "Wystąpił błąd bazy danych dla komendy gildii.");
		}
	}

	private void handleCreate(final Player player, final String args) throws SQLException {
		final String[] tokens = splitCreateArguments(args);
		if (tokens.length < 2) {
			player.sendPrivateText(NotificationType.ERROR, "Użycie: /guild create <nazwa> <tag> [opis]");
			return;
		}

		final String name = tokens[0];
		final String tag = tokens[1];
		final String description = tokens.length >= 3 ? tokens[2] : null;
		final int guildId = guildService.createGuild(player.getID().getObjectID(), name, tag, description);
		player.setGuildMembership(guildId, name, tag);
		player.sendPrivateText("Utworzono gildię [" + tag + "] " + name + ".");
	}

	private void handleInvite(final Player player, final String args) throws SQLException {
		final Player target = requireOnlineTarget(args, player, "Użycie: /guild invite <gracz>");
		guildService.inviteMember(player.getID().getObjectID(), requireGuildId(player), target.getID().getObjectID());
		player.sendPrivateText("Wysłano zaproszenie do gildii dla: " + target.getName() + ".");
		target.sendPrivateText("Otrzymałeś zaproszenie do gildii. Użyj: /guild accept " + requireGuildId(player));
	}

	private void handleAccept(final Player player, final String args) throws SQLException {
		if (args == null) {
			player.sendPrivateText(NotificationType.ERROR, "Użycie: /guild accept <id_gildii>");
			return;
		}
		final int guildId = Integer.parseInt(args);
		guildService.acceptInvite(player.getID().getObjectID(), guildId);
		final GuildData guild = guildService.getGuildInfo(guildId);
		if (guild != null) {
			player.setGuildMembership(guildId, guild.getName(), guild.getTag());
			player.sendPrivateText("Dołączyłeś do gildii [" + guild.getTag() + "] " + guild.getName() + ".");
		}
	}

	private void handleLeave(final Player player) throws SQLException {
		guildService.leaveGuild(player.getID().getObjectID(), requireGuildId(player));
		player.clearGuildMembership();
		player.sendPrivateText("Opuściłeś gildię.");
	}

	private void handleKick(final Player player, final String args) throws SQLException {
		final Player target = requireOnlineTarget(args, player, "Użycie: /guild kick <gracz>");
		guildService.kickMember(player.getID().getObjectID(), requireGuildId(player), target.getID().getObjectID());
		target.clearGuildMembership();
		player.sendPrivateText("Wyrzucono gracza z gildii: " + target.getName() + ".");
		target.sendPrivateText("Zostałeś wyrzucony z gildii.");
	}

	private void handlePromote(final Player player, final String args) throws SQLException {
		final Player target = requireOnlineTarget(args, player, "Użycie: /guild promote <gracz>");
		guildService.promoteMember(player.getID().getObjectID(), requireGuildId(player), target.getID().getObjectID());
		player.sendPrivateText("Awansowano: " + target.getName() + " do OFFICER.");
		target.sendPrivateText("Zostałeś awansowany do roli OFFICER.");
	}

	private void handleDemote(final Player player, final String args) throws SQLException {
		final Player target = requireOnlineTarget(args, player, "Użycie: /guild demote <gracz>");
		guildService.demoteMember(player.getID().getObjectID(), requireGuildId(player), target.getID().getObjectID());
		player.sendPrivateText("Zdegradowano: " + target.getName() + " do MEMBER.");
		target.sendPrivateText("Zostałeś zdegradowany do roli MEMBER.");
	}

	private void handleTransfer(final Player player, final String args) throws SQLException {
		final Player target = requireOnlineTarget(args, player, "Użycie: /guild transfer <gracz>");
		guildService.transferLeadership(player.getID().getObjectID(), requireGuildId(player), target.getID().getObjectID());
		player.sendPrivateText("Przekazano przywództwo graczowi: " + target.getName() + ".");
		target.sendPrivateText("Otrzymałeś przywództwo gildii.");
	}

	private void handleInfo(final Player player) throws SQLException {
		final int guildId = requireGuildId(player);
		final GuildData guildData = guildService.getGuildInfo(guildId);
		if (guildData == null) {
			player.sendPrivateText(NotificationType.ERROR, "Nie znaleziono danych gildii.");
			return;
		}
		final List<GuildMemberView> members = guildService.listGuildMembers(guildId);
		player.sendPrivateText("Gildia [" + guildData.getTag() + "] " + guildData.getName());
		if (guildData.getDescription() != null) {
			player.sendPrivateText("Opis: " + guildData.getDescription());
		}
		final StringBuilder sb = new StringBuilder("Członkowie: ");
		for (int i = 0; i < members.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(members.get(i).getCharacterName()).append("(").append(members.get(i).getRole()).append(")");
		}
		player.sendPrivateText(sb.toString());
	}

	private Player requireOnlineTarget(final String targetName, final Player actor, final String usageError) {
		if (targetName == null || targetName.length() == 0) {
			throw new IllegalArgumentException(usageError);
		}
		final Player target = SingletonRepository.getRuleProcessor().getPlayer(targetName);
		if (target == null) {
			throw new IllegalStateException("Ta operacja wymaga gracza online: " + targetName + ".");
		}
		if (target.getName().equals(actor.getName())) {
			throw new IllegalStateException("Nie możesz wykonać tej operacji na sobie.");
		}
		return target;
	}

	private int requireGuildId(final Player player) {
		if (!player.has("guild_id")) {
			throw new IllegalStateException("Nie jesteś członkiem żadnej gildii.");
		}
		return player.getInt("guild_id");
	}

	private String normalize(final String value) {
		if (value == null) {
			return null;
		}
		final String normalized = value.trim();
		return normalized.length() == 0 ? null : normalized;
	}

	static String[] splitCreateArguments(final String input) {
		if (input == null) {
			return new String[0];
		}

		final List<String> tokens = tokenizeRespectingQuotes(input);
		if (tokens.size() <= 2) {
			return tokens.toArray(new String[tokens.size()]);
		}

		final String[] result = new String[3];
		result[0] = tokens.get(0);
		result[1] = tokens.get(1);
		final StringBuilder description = new StringBuilder();
		for (int i = 2; i < tokens.size(); i++) {
			if (i > 2) {
				description.append(' ');
			}
			description.append(tokens.get(i));
		}
		result[2] = description.toString();
		return result;
	}

	private static List<String> tokenizeRespectingQuotes(final String input) {
		final List<String> tokens = new ArrayList<String>();
		final StringBuilder current = new StringBuilder();
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;

		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
				continue;
			}
			if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
				continue;
			}
			if (Character.isWhitespace(c) && !inSingleQuote && !inDoubleQuote) {
				if (current.length() > 0) {
					tokens.add(current.toString());
					current.setLength(0);
				}
				continue;
			}
			current.append(c);
		}

		if (current.length() > 0) {
			tokens.add(current.toString());
		}

		return tokens;
	}

	private void sendUsage(final Player player) {
		player.sendPrivateText(NotificationType.ERROR,
				"Użycie: /guild create|invite|accept|leave|kick|promote|demote|transfer|info");
	}
}
