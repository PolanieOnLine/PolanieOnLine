/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.Killer;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestProp;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestUseableProp;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.PlayerPrivateSpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Actors and gates reconstructed from the owner's persisted checkpoints. */
final class RadanDungeonScene {
	private final Player owner;
	private final StendhalRPZone zone;
	private final List<PlayerPrivateQuestProp> rescueGate = new ArrayList<>(),
			cargoGate = new ArrayList<>();
	private boolean bossSpawned;

	RadanDungeonScene(Player owner, StendhalRPZone zone) {
		this.owner = owner;
		this.zone = zone;
	}

	private boolean present(Player player) {
		return player == owner && player.getZone() == zone
				&& zone.getPlayers().contains(player);
	}

	void populate() {
		int killed = RadanDungeonProgress.guards(owner);
		if ((killed & 1) == 0)
			fighter("człowiek Radana", 1, 6, 10);
		if ((killed & 2) == 0)
			fighter("strażnik więźnia", 2, 11, 8);
		gate(rescueGate, 17, 8, true);
		gate(cargoGate, 29, 20, true);
		Actor prisoner = new Actor(owner, false);
		prisoner.setPosition(3, 6);
		zone.add(prisoner);
		Actor radan = new Actor(owner, true);
		radan.setPosition(36, 23);
		zone.add(radan);
		Mechanism brake = new Mechanism(false);
		brake.setPosition(22, 11);
		zone.add(brake);
		Mechanism chain = new Mechanism(true);
		chain.setPosition(25, 11);
		zone.add(chain);
		refresh();
	}

	private void fighter(String name, int role, int x, int y) {
		Fighter fighter = new Fighter(
				TrailCreatureVariants.create(name), role);
		fighter.setPosition(x, y);
		zone.add(fighter);
	}

	private void gate(List<PlayerPrivateQuestProp> target, int x, int y,
			boolean vertical) {
		for (int i = 0; i < 3; i++) {
			PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(owner,
					"item/pot/crate_small", 0, 2, true, true);
			prop.setDescription(
					"Oto skrzynie z towarem. Zastawiają przejście.");
			prop.setPosition(x + (vertical ? 0 : i), y + (vertical ? i : 0));
			zone.add(prop);
			target.add(prop);
		}
	}

	private void open(List<PlayerPrivateQuestProp> gate) {
		for (PlayerPrivateQuestProp prop : gate)
			zone.remove(prop);
		gate.clear();
	}

	private void refresh() {
		for (Entity entity : zone.getEntitiesOfClass(Mechanism.class)) {
			((Mechanism) entity).refreshAppearance();
		}
		if (owner.isQuestCompleted(Step.RADAN_RESCUE.slot))
			open(rescueGate);
		if (owner.isQuestCompleted(Step.RADAN_TRANSPORT.slot))
			open(cargoGate);
		if (!bossSpawned
				&& owner.isQuestInState(Step.RADAN_FINALE.slot, "guard")) {
			bossSpawned = true;
			fighter("zbrojny Radana", 4, 33, 21);
		}
	}

	final class Fighter extends Creature {
		final int role;
		private boolean recorded;

		Fighter(Creature template, int role) {
			super(template);
			this.role = role;
			// Rewards are checkpointed by the quest, never farmed by
			// re-entering.
			setXP(0);
			clearDropItemList();
		}

		@Override
		protected void dropItemsOn(Corpse corpse) {
			// Re-entering the private scene must not generate seasonal loot.
		}

		@Override
		public void onDead(Killer killer, boolean remove) {
			if (!recorded) {
				recorded = true;
				if (present(owner))
					RadanDungeonProgress.defeated(owner, role);
			}
			super.onDead(killer, remove);
		}
	}

	final class Mechanism extends PlayerPrivateQuestUseableProp {
		final boolean chain;

		Mechanism(boolean chain) {
			super(owner, "object/radan_mine_mechanisms", chain ? 2 : 0, 2, false);
			this.chain = chain;
			setDescription(chain
					? "Oto zaczep łańcucha wyciągu. Łańcuch jest mocno napięty."
					: "Oto korba hamulca. Obraca się ciężko, ale nie jest zablokowana.");
			setMenu(chain ? "Odepnij|Użyj" : "Zaciągnij|Użyj");
		}

		void refreshAppearance() {
			boolean applied = owner.isQuestCompleted(Step.RADAN_TRANSPORT.slot)
					|| (!chain && owner.isQuestInState(Step.RADAN_TRANSPORT.slot, "chain"));
			int tile = (chain ? 2 : 0) + (applied ? 1 : 0);
			if (getInt("tile_index") != tile) {
				put("tile_index", tile);
				setDescription(chain
						? "Oto odpięty łańcuch wyciągu. Hak leży obok zaczepu wózka."
						: "Oto korba hamulca. Hamulec mocno trzyma koła wózka.");
				notifyWorldAboutChanges();
			}
		}

		@Override
		protected boolean onUsedByOwner(Player player) {
			if (!present(player))
				return false;
			boolean changed = chain ? RadanDungeonProgress.chain(player)
					: RadanDungeonProgress.brake(player);
			if (changed) {
				player.sendPrivateText(chain
						? "Odpinasz łańcuch. Wyciąg nie zabierze już wózka. Odsuwasz skrzynie z przejścia do ostatniej izby."
						: "Obracasz korbę. Hamulec zaciska się na kołach. Teraz możesz odpiąć łańcuch po prawej stronie wózka.");
				refresh();
			} else
				player.sendPrivateText(chain && player.isQuestInState(
						Step.RADAN_TRANSPORT.slot, "brake")
								? "Łańcuch jest napięty. Jeśli go odepniesz, wózek ruszy. Najpierw zaciągnij hamulec obok."
								: "Nie musisz teraz niczego tu przestawiać. Sprawdź swój cel w dzienniku.");
			return changed;
		}

		@Override
		public String getDescriptionName() {
			return chain ? "łańcuch wyciągu" : "hamulec wózka";
		}
	}

	final class Actor extends PlayerPrivateSpeakerNPC {
		final boolean radan;

		Actor(Player player, boolean radan) {
			super(player, radan ? "Radan" : "Bartek");
			this.radan = radan;
			setEntityClass(radan ? "seller3npc" : "minernpc");
			setGender("M");
			setIdleDirection(Direction.DOWN);
			setDescription(radan
					? "Oto Radan. Pilnuje księgi i co chwilę spogląda w stronę wózka."
					: "Oto Bartek, robotnik zatrzymany przez ludzi Radana.");
		}

		@Override
		protected void createDialog() {
			add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
					null, ConversationStates.ATTENDING, null,
					(player, sentence, speaker) -> {
						if (!present(player))
							return;
						speaker.say(radan ? (player.isQuestInState(
								Step.RADAN_FINALE.slot, "return")
										? "Oddałem ci księgę. Poczekam na straż."
										: "Czego tu szukasz? To prywatny skład. Ten #transport jest już opłacony.")
								: (owner.isQuestCompleted(
										Step.RADAN_RESCUE.slot)
												? "Nie mam już sił. Zaczekam tu na straż. Uważaj tylko na wózek, najpierw zaciągnij #hamulec."
												: "Cicho! Jeszcze cię usłyszą. Rozpraw się ze strażnikami, wtedy spróbujemy przeciąć te #więzy."));
					});
			add(ConversationStates.ATTENDING,
					Arrays.asList("więzy", "wiezy", "uwolnij", "transport",
							"hamulec", "wózek", "wozek", "łańcuch", "lancuch"),
					null, ConversationStates.ATTENDING, null,
					(player, sentence, speaker) -> {
						if (!present(player))
							return;
						if (radan) {
							if (RadanDungeonProgress.confront(player)) {
								speaker.say(
										"Już dobrze! Odłóż broń. Chcesz księgę? Bierz ją, wszystko w niej jest. "
												+ "Tylko nie bij. Poczekam na tę waszą straż, nigdzie się nie ruszę.");
							} else
								speaker.say(player.isQuestInState(
										Step.RADAN_FINALE.slot, "return")
												? "Masz księgę. Idź do Drilenuna. Nigdzie się stąd nie ruszam."
												: "Myślisz, że tak po prostu oddam ci księgę? Moi ludzie wiedzą, co robić z takimi gośćmi.");
						} else if (RadanDungeonProgress.free(player)) {
							speaker.say(
									"Ach, nareszcie... Nie czuję dłoni. Miałem tylko naprawić tor, a oni mnie tu zatrzymali. "
											+ "Radan chce dziś wywieźć zapasy. Zaciągnij #hamulec przy wózku, dopiero potem odczep #łańcuch. "
											+ "Inaczej cały ładunek poleci w dół. Ja odsunę skrzynie z przejścia i zaczekam tutaj na straż.");
							refresh();
						} else
							speaker.say(owner.isQuestCompleted(
									Step.RADAN_RESCUE.slot)
											? "Widzisz korbę z lewej strony wózka? To hamulec. Zaciągnij go porządnie, zanim odczepisz łańcuch z drugiej strony. Nie chcę słyszeć, jak to wszystko zjeżdża w dół."
											: "Oni wciąż tu są. Jeśli mnie teraz rozwiążesz, złapią nas oboje. Proszę, zajmij się najpierw strażnikami.");
					});
			addGoodbye("Uważaj na siebie.");
		}
	}
}
