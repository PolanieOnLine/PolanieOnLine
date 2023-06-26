/***************************************************************************
 *                       Copyright © 2023 - Stendhal                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.behaviour.adder;

import java.util.Arrays;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.BankTellerBehaviour;
import games.stendhal.server.entity.player.Player;

/**
 * Adds bank teller behaviour to an NPC.
 */
public class BankTellerAdder {
	/** Action when player deposits money into account. */
	private static final ChatAction depositAction = new ChatAction() {
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			List<String> deposit = Arrays.asList("deposit", "depozyt", "wpłata", "wpłacić");
			if (deposit.contains(sentence.getTrimmedText().toLowerCase())) {
				npc.say("Podaj kwotę do wpłaty.");
				return;
			}
			npc.say(BankTellerBehaviour.deposit(player,
					BankTellerAdder.parseTransactionAmount(player, sentence, true)));
		}
	};

	/** Action when player withdraws money from account. */
	private static final ChatAction withdrawAction = new ChatAction() {
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			List<String> withdraw = Arrays.asList("withdraw", "wypłata", "wypłacić");
			if (withdraw.contains(sentence.getTrimmedText().toLowerCase())) {
				npc.say("Proszę podać kwotę do wypłaty.");
				return;
			}
			npc.say(BankTellerBehaviour.withdraw(player,
					BankTellerAdder.parseTransactionAmount(player, sentence, false)));
		}
	};

	/** Action when player wants to know account balance. */
	private static final ChatAction queryBalanceAction = new ChatAction() {
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			npc.say(BankTellerBehaviour.queryBalance(player));
		}
	};

	/**
	 * Adds bank teller behaviour to an NPC.
	 *
	 * @param npc
	 *	 SpeakerNPC to receive behaviour.
	 */
	public static void addTeller(final SpeakerNPC npc) {
		npc.put("job_teller", "");

		final String help_res = npc.getReply("help");
		if (help_res == null) {
			npc.addHelp("Pomogę Ci zarządzać Twoim #saldem bankowym. Powiedz mi, czy chcesz dokonać"
					+ " #wpłaty na swoje konto lub #wypłacić je. Jeśli chcesz, możesz #'wpłacić wszystko'"
					+ " lub #'wypłacić wszystko'.");
		} else {
			npc.addHelp("Pomogę Ci zarządzać Twoim #saldem bankowym. Powiedz mi, czy chcesz dokonać"
					+ " #wpłaty na swoje konto lub #wypłacić je. Jeśli chcesz, możesz #'wpłacić wszystko'"
					+ " lub #'wypłacić wszystko'.");
		}
		final String job_res = npc.getJob();
		if (job_res == null) {
			npc.addJob("Mogę #pomóc Ci zarządzać Twoim #saldem w banku.");
		}

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("deposit", "depozyt", "wpłata", "wpłacić"),
			null,
			ConversationStates.ATTENDING,
			null,
			depositAction);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("withdraw", "wypłata", "wypłacić"),
			null,
			ConversationStates.ATTENDING,
			null,
			withdrawAction);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("balance", "saldo"),
			null,
			ConversationStates.ATTENDING,
			null,
			queryBalanceAction);
	}

	/**
	 * Checks if an NPC is a bank teller.
	 *
	 * @param npc
	 *	 SpeakerNPC to check.
	 * @return
	 *	 `true` if NPC has "job_teller" attribute.
	 */
	public static boolean isTeller(final SpeakerNPC npc) {
		return npc.has("job_teller");
	}

	/**
	 * Parses requested transaction amount from player's sentence.
	 *
	 * @param player
	 *	 Player requesting transaction.
	 * @param sentence
	 *	 Sentence to parse.
	 * @param deposit
	 *	 `true` for deposits, `false` for withdrawals.
	 * @return
	 *	 Amount of money for transaction or `null` if invalid amount.
	 */
	private static Integer parseTransactionAmount(final Player player, final Sentence sentence,
			final boolean deposit) {
		Integer amount = null;
		final List<String> statement = Arrays.asList(sentence.getTrimmedText().split(" "));
		if (statement.size() < 2) {
			return null;
		}
		try {
			amount = Integer.parseInt(statement.get(1));
		} catch (final NumberFormatException e) {
			List<String> all = Arrays.asList("all", "wszystko", "całość");
			if (all.contains(statement.get(1).toLowerCase())) {
				// player wants to withdraw or deposit all money
				if (deposit) {
					amount = player.getNumberOfEquipped("money");
				} else {
					amount = BankTellerBehaviour.getBalanceAmount(player);
				}
			}
		}
		return amount;
	}
}