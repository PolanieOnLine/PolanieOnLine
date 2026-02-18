package games.stendhal.server.maps.ados.townhall;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.guild.GuildService;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AlwaysTrueCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Guild master handling guild creation flow.
 */
public class GuildMasterNPC implements ZoneConfigurator {
	private static final String QUEST_SLOT = "guild_master_creation";
	private static final String SEPARATOR = ";";
	private static final String SKIP_DESCRIPTION = "pomiń";

	private final GuildService guildService = new GuildService();

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Mistrz Gildii") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj. Jeśli chcesz #załóż gildię, przeprowadzę Cię przez cały proces.");
				addJob("Nadzoruję zakładanie gildii i pilnuję opłat wpisowych.");
				addHelp("Powiedz #załóż gildię. Potrzebuję od Ciebie nazwy, tagu i opcjonalnego opisu.");
				addOffer("Koszt założenia gildii to " + GuildCreationConfig.GOLD_FEE
						+ " money oraz przedmioty: " + getRequiredItemsText() + ".");
				addGoodbye("Powodzenia na szlaku wojowniku.");

				add(ConversationStates.ATTENDING,
					Arrays.asList("załóż gildię", "zaloz gildie", "guild"),
					null,
					ConversationStates.INFORMATION_1,
					"Dobrze. Podaj #nazwa nowej gildii.",
					new ResetConversationDataAction());

				add(ConversationStates.INFORMATION_1,
					"",
					new AlwaysTrueCondition(),
					ConversationStates.INFORMATION_2,
					null,
					new StoreGuildNameAction());

				add(ConversationStates.INFORMATION_2,
					"",
					new AlwaysTrueCondition(),
					ConversationStates.INFORMATION_3,
					null,
					new StoreGuildTagAction());

				add(ConversationStates.INFORMATION_3,
					Arrays.asList("pomiń", "pomijam"),
					null,
					ConversationStates.ATTENDING,
					"W porządku, pomijam opis. Spróbuję od razu założyć gildię.",
					new FinalizeGuildCreationAction(SKIP_DESCRIPTION));

				add(ConversationStates.INFORMATION_3,
					"",
					new AlwaysTrueCondition(),
					ConversationStates.ATTENDING,
					null,
					new FinalizeGuildCreationAction());
			}
		};

		npc.setDescription("Oto Mistrz Gildii. Pomaga bohaterom zakładać nowe gildie.");
		npc.setEntityClass("mayornpc");
		npc.setGender("M");
		npc.setPosition(14, 8);
		zone.add(npc);
	}

	private static String getRequiredItemsText() {
		final StringBuilder builder = new StringBuilder();
		for (GuildService.RequiredItem item : GuildCreationConfig.REQUIRED_ITEMS) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append(item.getQuantity()).append("x ").append(item.getName());
		}
		return builder.toString();
	}

	private static String normalizeInput(final String input, final String keyword) {
		if (input == null) {
			return "";
		}
		final String lowered = input.trim();
		if (lowered.toLowerCase().startsWith(keyword + " ")) {
			return lowered.substring(keyword.length()).trim();
		}
		return lowered;
	}

	private static String[] loadConversationData(final Player player) {
		final String value = player.getQuest(QUEST_SLOT);
		if (value == null || value.length() == 0) {
			return new String[] { "", "" };
		}
		final String[] split = value.split(SEPARATOR, -1);
		if (split.length < 2) {
			return new String[] { "", "" };
		}
		return split;
	}

	private static final class ResetConversationDataAction implements ChatAction {
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			player.setQuest(QUEST_SLOT, "");
		}
	}

	private static final class StoreGuildNameAction implements ChatAction {
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final String guildName = normalizeInput(sentence.getOriginalText(), "nazwa");
			if (guildName.length() == 0) {
				npc.say("Nie podałeś nazwy gildii. Napisz proszę #nazwa <twoja nazwa>.");
				return;
			}
			player.setQuest(QUEST_SLOT, guildName + SEPARATOR);
			npc.say("Zapisano nazwę: " + guildName + ". Teraz podaj #tag gildii (4-5 znaków).");
		}
	}

	private static final class StoreGuildTagAction implements ChatAction {
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final String[] data = loadConversationData(player);
			if (data[0].length() == 0) {
				npc.say("Najpierw muszę znać nazwę gildii. Zacznij od komendy #załóż gildię.");
				return;
			}
			final String guildTag = normalizeInput(sentence.getOriginalText(), "tag");
			if (guildTag.length() == 0) {
				npc.say("Nie podałeś tagu. Napisz proszę #tag <skrót>.");
				return;
			}

			player.setQuest(QUEST_SLOT, data[0] + SEPARATOR + guildTag);
			npc.say("Świetnie. Podaj teraz opis gildii albo napisz #pomiń jeśli chcesz założyć bez opisu.");
		}
	}

	private final class FinalizeGuildCreationAction implements ChatAction {
		private final String forcedDescription;

		private FinalizeGuildCreationAction() {
			this(null);
		}

		private FinalizeGuildCreationAction(final String forcedDescription) {
			this.forcedDescription = forcedDescription;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final String[] data = loadConversationData(player);
			if (data[0].length() == 0 || data[1].length() == 0) {
				npc.say("Brakuje nazwy albo tagu gildii. Powiedz ponownie #załóż gildię.");
				player.setQuest(QUEST_SLOT, "");
				return;
			}

			final String description;
			if (forcedDescription != null) {
				description = null;
			} else {
				final String rawDescription = sentence.getOriginalText() == null ? "" : sentence.getOriginalText().trim();
				description = SKIP_DESCRIPTION.equalsIgnoreCase(rawDescription) ? null : rawDescription;
			}

			try {
				guildService.createGuildFromNpc(player,
						data[0],
						data[1],
						description,
						GuildCreationConfig.GOLD_FEE,
						GuildCreationConfig.REQUIRED_ITEMS);
				npc.say("Gotowe! Założono gildię [" + data[1] + "] " + data[0] + ".");
			} catch (IllegalArgumentException e) {
				npc.say("Nie mogę założyć gildii: " + e.getMessage());
			} catch (IllegalStateException e) {
				npc.say("Nie mogę założyć gildii: " + e.getMessage());
			} catch (SQLException e) {
				npc.say("Wystąpił błąd podczas zakładania gildii. Spróbuj ponownie za chwilę.");
			}

			player.setQuest(QUEST_SLOT, "");
		}
	}
}
