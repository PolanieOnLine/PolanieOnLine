/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Killer;
import games.stendhal.server.entity.creature.Creature;
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
    private final List<PlayerPrivateQuestProp> rescueGate = new ArrayList<>(), cargoGate = new ArrayList<>();
    private boolean bossSpawned;
    RadanDungeonScene(Player owner, StendhalRPZone zone) { this.owner = owner; this.zone = zone; }
    private boolean present(Player player) { return player == owner && player.getZone() == zone && zone.getPlayers().contains(player); }
    void populate() {
        int killed = RadanDungeonProgress.guards(owner);
        if ((killed & 1) == 0) fighter("człowiek Radana", 1, 6, 10);
        if ((killed & 2) == 0) fighter("strażnik więźnia", 2, 11, 8);
        gate(rescueGate, 17, 8, true); gate(cargoGate, 29, 20, true);
        Actor prisoner = new Actor(owner, false); prisoner.setPosition(3, 6); zone.add(prisoner);
        Actor radan = new Actor(owner, true); radan.setPosition(36, 23); zone.add(radan);
        Mechanism brake = new Mechanism(false); brake.setPosition(23, 7); zone.add(brake);
        Mechanism chain = new Mechanism(true); chain.setPosition(26, 7); zone.add(chain);
        refresh();
    }
    private void fighter(String name, int role, int x, int y) {
        Fighter fighter = new Fighter(SingletonRepository.getEntityManager().getCreature(name), role);
        fighter.setPosition(x, y); zone.add(fighter);
    }
    private void gate(List<PlayerPrivateQuestProp> target, int x, int y, boolean vertical) {
        for (int i = 0; i < 3; i++) {
            PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(owner, "item/pot/crate_small", 0, 2, true, true);
            prop.setDescription("Oto skrzynie z towarem. Zastawiają przejście.");
            prop.setPosition(x + (vertical ? 0 : i), y + (vertical ? i : 0)); zone.add(prop); target.add(prop);
        }
    }
    private void open(List<PlayerPrivateQuestProp> gate) {
        for (PlayerPrivateQuestProp prop : gate) zone.remove(prop);
        gate.clear();
    }
    private void refresh() {
        if (owner.isQuestCompleted(Step.RADAN_RESCUE.slot)) open(rescueGate);
        if (owner.isQuestCompleted(Step.RADAN_TRANSPORT.slot)) open(cargoGate);
        if (!bossSpawned && owner.isQuestInState(Step.RADAN_FINALE.slot, "guard")) {
            bossSpawned = true; fighter("zbrojny Radana", 4, 33, 21);
        }
    }
    final class Fighter extends Creature {
        final int role;
        private boolean recorded;
        Fighter(Creature template, int role) {
            super(template); this.role = role;
            // Rewards are checkpointed by the quest, never farmed by re-entering.
            setXP(0); clearDropItemList();
        }
        @Override public void onDead(Killer killer, boolean remove) {
            if (!recorded) {
                recorded = true;
                if (present(owner)) RadanDungeonProgress.defeated(owner, role);
            }
            super.onDead(killer, remove);
        }
    }
    final class Mechanism extends PlayerPrivateQuestUseableProp {
        final boolean chain;
        Mechanism(boolean chain) {
            super(owner, chain ? "item/armor/chains" : "item/tools", chain ? 0 : 8, chain ? 2 : 3, false); this.chain = chain;
            setDescription(chain ? "Oto zaczep łańcucha wyciągu. Łańcuch jest mocno napięty."
                    : "Oto korba hamulca. Obraca się ciężko, ale nie jest zablokowana.");
            setMenu(chain ? "Odepnij|Użyj" : "Zaciągnij|Użyj");
        }
        @Override protected boolean onUsedByOwner(Player player) {
            if (!present(player)) return false;
            boolean changed = chain ? RadanDungeonProgress.chain(player) : RadanDungeonProgress.brake(player);
            if (changed) {
                player.sendPrivateText(chain ? "Odpinasz łańcuch. Wyciąg nie zabierze już wózka. Odsuwasz skrzynie z przejścia do ostatniej izby."
                        : "Obracasz korbę. Hamulec zaciska się na kołach. Teraz możesz odpiąć łańcuch po prawej stronie.");
                refresh();
            } else player.sendPrivateText(chain && player.isQuestInState(Step.RADAN_TRANSPORT.slot, "brake")
                    ? "Łańcuch jest napięty. Jeśli go odepniesz, wózek ruszy. Najpierw zaciągnij hamulec obok."
                    : "Nie musisz teraz niczego tu przestawiać. Sprawdź swój cel w dzienniku.");
            return changed;
        }
        @Override public String getDescriptionName() { return chain ? "łańcuch wyciągu" : "hamulec wózka"; }
    }
    final class Actor extends PlayerPrivateSpeakerNPC {
        final boolean radan;
        Actor(Player player, boolean radan) {
            super(player, radan ? "Radan" : "Bartek"); this.radan = radan;
            setEntityClass(radan ? "seller3npc" : "minernpc"); setGender("M"); setIdleDirection(Direction.DOWN);
            setDescription(radan ? "Oto Radan. Pilnuje księgi i co chwilę spogląda w stronę wózka."
                    : "Oto Bartek, robotnik zatrzymany przez ludzi Radana.");
        }
        @Override protected void createDialog() {
            add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES, null,
                    ConversationStates.ATTENDING, null, (player, sentence, speaker) -> {
                if (!present(player)) return;
                speaker.say(radan ? (player.isQuestInState(Step.RADAN_FINALE.slot, "return")
                        ? "Oddałem ci księgę. Poczekam na straż."
                        : "Czego tu szukasz? To prywatny skład. Ten #transport jest już opłacony.")
                        : (owner.isQuestCompleted(Step.RADAN_RESCUE.slot)
                        ? "Dzięki za pomoc. Poczekam tu na straż. Pamiętaj: najpierw #hamulec, potem łańcuch."
                        : "Pomóż mi. Związali mi ręce. Dopóki strażnicy tu stoją, nie próbuj przecinać moich #więzów."));
            });
            add(ConversationStates.ATTENDING, Arrays.asList("więzy", "wiezy", "uwolnij", "transport", "hamulec", "wózek", "wozek", "łańcuch", "lancuch"),
                    null, ConversationStates.ATTENDING, null, (player, sentence, speaker) -> {
                if (!present(player)) return;
                if (radan) {
                    if (RadanDungeonProgress.confront(player)) {
                        speaker.say("Dobrze, już dobrze. Zabierz księgę. Są w niej nazwiska i wszystkie dostawy. "
                                + "Nie chcę kolejnej walki. Powiedz Drilenunowi, że poczekam na straż.");
                    } else speaker.say(player.isQuestInState(Step.RADAN_FINALE.slot, "return")
                            ? "Masz księgę. Idź do Drilenuna. Nigdzie się stąd nie ruszam."
                            : "Moi ludzie jeszcze tu są. Nie będę z tobą o niczym rozmawiał.");
                } else if (RadanDungeonProgress.free(player)) {
                    speaker.say("Nareszcie! Miałem tylko naprawić tor, a potem nie pozwolili mi odejść. "
                            + "Radan chce dziś wywieźć zapasy. Przy wózku jest #hamulec. Zaciągnij go, a potem odepnij #łańcuch. "
                            + "Odsunę skrzynie, żebyś mógł przejść. Ja poczekam tu na straż."); refresh();
                } else speaker.say(owner.isQuestCompleted(Step.RADAN_RESCUE.slot)
                        ? "Najpierw hamulec przy północnej ścianie izby z wózkiem. Potem łańcuch po jego prawej stronie. Inaczej wózek ruszy."
                        : "Najpierw rozpraw się z oboma strażnikami. Inaczej znowu mnie złapią.");
            });
            addGoodbye("Uważaj na siebie.");
        }
    }
}
