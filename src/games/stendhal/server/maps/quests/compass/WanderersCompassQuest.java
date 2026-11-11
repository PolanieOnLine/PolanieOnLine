/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import games.stendhal.common.QuestMarker;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.navigation.NavigationGoal;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.maps.quests.QuestRewards;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;
import games.stendhal.server.maps.quests.trail.TrailSceneRegistry;
import java.util.Arrays;
import java.util.List;

/** Independent twenty-checkpoint side quest. No main-story completion is required. */
public final class WanderersCompassQuest extends AbstractQuest {
  public static final String SLOT = CompassProgress.SLOT;
  static final String REWARD = "kompas wędrowca";

  @Override
  public String getSlotName() {
    return SLOT;
  }

  @Override
  public String getName() {
    return "Na manowcach";
  }

  @Override
  public String getNPCName() {
    return "Sabała";
  }

  @Override
  public String getRegion() {
    return Region.ZAKOPANE_CITY;
  }

  @Override
  public int getMinLevel() {
    return 15;
  }

  @Override
  public List<String> getHistory(Player player) {
    return CompassProgress.history(player);
  }

  @Override
  public QuestMarker getNPCQuestMarker(Player p, SpeakerNPC npc) {
    if (!"Sabała".equals(TrailSceneRegistry.speakerName(npc))) return QuestMarker.NONE;
    return !p.hasQuest(SLOT) && p.getLevel() >= 15 ? QuestMarker.AVAILABLE : null;
  }

  public static NavigationGoal target(Player p) {
    Stage s = CompassProgress.stage(p);
    if (s == null) return null;
    int x = s.x, y = s.y;
    if (s == Stage.JUNCTIONS && CompassProgress.detail(p) == 1) {
      x = 60;
      y = 84;
    }
    if (s == Stage.ESCORT && CompassProgress.detail(p) == 1) {
      x = 51;
      y = 86;
    }
    if (s == Stage.BANDAGE && CompassProgress.detail(p) == 1) {
      x = 68;
      y = 86;
    }
    if (s == Stage.RECOGNITION || s == Stage.RETURN) {
      SpeakerNPC sabala = SingletonRepository.getNPCList().get("Sabała");
      if (sabala != null && sabala.getZone() != null)
        return new NavigationGoal(
            sabala.getZone().getName(),
            sabala.getX(),
            sabala.getY(),
            CompassProgress.objective(p),
            s.name());
    }
    return new NavigationGoal(s.zone(), x, y, CompassProgress.objective(p), s.name());
  }

  @Override
  public void addToWorld() {
    fillQuestInfo(
        getName(),
        "Odnajdź zagubionego wędrowca w Kościelisku i przywróć kompas dawnego przewodnika.",
        false);
    QuestRewards rewards =
        new QuestRewards(5000, 100).note("Stałe odblokowanie kompasu wskazującego cele zadań.");
    if (SingletonRepository.getEntityManager().getItem(REWARD) != null)
      rewards.item(REWARD, 1, null, true);
    questInfo.setRewards(rewards.build());
    StendhalRPZone forest = SingletonRepository.getRPWorld().getZone(CompassProgress.FOREST);
    if (forest == null)
      throw new IllegalStateException("Missing compass quest zone: " + CompassProgress.FOREST);
    CompassScenes.register(forest);
    npcs.get("Sabała")
        .add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            (p, s, n) -> !p.isQuestCompleted(SLOT),
            ConversationStates.ATTENDING,
            "Witaj. Martwię się o Dobromira. Poszedł na #manowce w Kościelisku i dotąd nie wrócił.",
            null);
    npcs.get("Sabała")
        .add(
            ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            (p, s, n) -> !p.isQuestCompleted(SLOT),
            ConversationStates.ATTENDING,
            "Jest coś, w czym możesz mi pomóc. Zapytaj o #manowce.",
            null);
    bind(
        "Sabała",
        (p, s, n) -> {
          if (p.hasQuest(SLOT)) {
            n.say(CompassProgress.objective(p));
            return;
          }
          if (p.getLevel() < 15) {
            n.say("To niebezpieczna droga. Nabierz trochę doświadczenia, nim wyruszysz tak daleko.");
            p.sendPrivateText("Zadanie „Na manowcach” jest dostępne od 15 poziomu.");
            return;
          }
          n.say(
              "Wędrowiec Dobromir nie wrócił z Kościeliska. Świeże znaki prowadzą ludzi ze szlaku."
                  + " Poszukasz go na południowej polanie we wschodnim Kościelisku? Boję się, że"
                  + " przytrafiło mu się coś złego.");
          n.setCurrentState(ConversationStates.QUEST_3_OFFERED);
        },
        "na manowcach",
        "manowce",
        "zaginiony wędrowiec",
        "zaginiony wedrowiec");
    npcs.get("Sabała")
        .add(
            ConversationStates.QUEST_3_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            (p, s, n) ->
                !p.hasQuest(SLOT)
                    && p.getLevel() >= 15
                    && p.getZone() == n.getZone()
                    && p.nextTo(n),
            ConversationStates.ATTENDING,
            null,
            (p, s, n) -> {
              CompassProgress.enter(p, Stage.MISSING);
              n.say(CompassProgress.objective(p));
            });
    npcs.get("Sabała")
        .add(
            ConversationStates.QUEST_3_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            (p, s, n) -> !p.hasQuest(SLOT),
            ConversationStates.ATTENDING,
            "Dobrze. Jeśli zmienisz zdanie, zapytaj o #manowce.",
            null);
    bind(
        "Sabała",
        (p, s, n) -> {
          if (CompassProgress.stage(p) != Stage.RECOGNITION) {
            n.say(CompassProgress.objective(p));
            return;
          }
          CompassProgress.next(p, Stage.RECOGNITION);
          n.say(
              "Miłorad... Znałem jego pismo. Szukał ludzi, których zwabiły ogniki. Nie chciałem"
                  + " takiej wieści. Naprawmy jego kompas, jeszcze komuś pomoże. Smolisław nad"
                  + " Wisłą w Krakowie wypali węgiel do wykucia nowej"
                  + " igły. Zamów nową porcję, odbierz ją i powiedz mu: #'węgiel do kompasu'. Potem"
                  + " odwiedź Kowala Jacka w Kościelisku.");
        },
        "stary przewodnik");
    bind(
        "Smolisław",
        (p, s, n) -> {
          if (CompassProgress.stage(p) != Stage.CHARCOAL) {
            n.say(CompassProgress.objective(p));
            return;
          }
          if (!p.isQuestCompleted("smolislaw_burn_charcoal")
              || p.getQuantityOfProducedItems("węgiel drzewny") <= CompassProgress.detail(p)
              || !p.isEquipped("węgiel drzewny", 1)) {
            n.say(
                "Do igły potrzebny jest świeżo wypalony węgiel. Kliknij mój mielerz albo powiedz:"
                    + " #'wypal 1 węgiel drzewny'. Potrzeba 2 polan i 5 monet. Odbierz zamówienie"
                    + " po 30 sekundach, potem wróć do tematu #'węgiel do kompasu'.");
            return;
          }
          CompassProgress.next(p, Stage.CHARCOAL);
          n.say(
              "Ten węgiel wypaliłem dla ciebie. Zanieś 1 sztukę i 1 sztabkę żelaza Jackowi w kuźni"
                  + " na południowym wschodzie Kościeliska. Powiedz: #'igła kompasu'.");
        },
        "węgiel do kompasu",
        "wegiel do kompasu");
    bind("Kowal Jacek", (p, s, n) -> needle(p, n), "igła kompasu", "igla kompasu");
    bind(
        CompassProgress.WANDERER,
        (p, s, n) -> {
          if (!PlayerPrivateScene.isOwnedBy(n.getEntity(), p)) return;
          Stage stage = CompassProgress.stage(p);
          if (stage == Stage.WOLVES) {
            if (CompassProgress.detail(p) != 3) {
              n.say("Najpierw odpędź oba wilki! Nie mogę wstać.");
              return;
            }
            CompassProgress.next(p, stage);
            n.say(
                "Poszedłem za świeżymi nacięciami. Wilki zagoniły mnie tutaj. Na zachód stąd rosną"
                    + " zioła. Zbierz je, przynieś 1 sukno i powiedz: #opatrunek.");
          } else if (stage == Stage.BANDAGE) {
            if (CompassProgress.detail(p) != 1) {
              n.say("Zbierz najpierw miejscowe zioła na zachód stąd.");
              return;
            }
            if (!consume(p, n, new String[] {"sukno"}, new int[] {1})) return;
            CompassProgress.next(p, stage);
            n.say("Dziękuję, opatrunek trzyma. Dołóż 3 polana do zimnego ogniska obok mnie.");
          } else n.say(CompassProgress.objective(p));
        },
        "pomoc",
        "opatrunek");
    bind("Sabała", (p, s, n) -> complete(p, n), "kompas wędrowca", "kompas wedrowca");
  }

  private void bind(String name, ChatAction action, String... triggers) {
    SpeakerNPC npc = npcs.get(name);
    if (npc == null) throw new IllegalStateException("Missing compass quest NPC: " + name);
    npc.add(
        ConversationStates.ATTENDING,
        Arrays.asList(triggers),
        null,
        ConversationStates.ATTENDING,
        null,
        (p, s, n) -> {
          if (p.getZone() != n.getEntity().getZone() || !p.nextTo(n.getEntity())) {
            n.say("Podejdź bliżej.");
            return;
          }
          action.fire(p, s, n);
        });
  }

  static boolean consume(Player p, EventRaiser n, String[] names, int[] amounts) {
    for (int i = 0; i < names.length; i++)
      if (!p.isEquipped(names[i], amounts[i])) {
        String text = "Potrzebujesz: ";
        for (int j = 0; j < names.length; j++)
          text += (j == 0 ? "" : ", ") + amounts[j] + " × " + names[j];
        if (n == null) p.sendPrivateText(text + ".");
        else n.say(text + ".");
        return false;
      }
    for (int i = 0; i < names.length; i++) p.drop(names[i], amounts[i]);
    return true;
  }

  static void needle(Player p, EventRaiser n) {
    if (CompassProgress.stage(p) != Stage.NEEDLE) {
      n.say(CompassProgress.objective(p));
      return;
    }
    if (CompassProgress.time(p) == 0) {
      if (!consume(p, n, new String[] {"sztabka żelaza", "węgiel drzewny"}, new int[] {1, 1}))
        return;
      CompassProgress.set(p, Stage.NEEDLE, 0, System.currentTimeMillis() + 60000);
      n.say(
          "Wykuję nową igłę. Ten węgiel da mi równy ogień. Wróć po minucie i powiedz:"
              + " #'igła kompasu'. Gotowa igła poczeka tu na ciebie.");
      return;
    }
    if (System.currentTimeMillis() < CompassProgress.time(p)) {
      n.say(CompassProgress.objective(p));
      return;
    }
    CompassProgress.next(p, Stage.NEEDLE);
    n.say(
        "Gotowe. Zawijam igłę w płótno, żeby nie uszkodziła się w drodze. Dobromir wspominał o"
            + " ognikach pod korzeniami drzew we wschodnim Kościelisku. Tam poszukaj obudowy,"
            + " na północny wschód od dawnego obozu.");
  }

  static void complete(Player p, EventRaiser n) {
    if (CompassProgress.stage(p) != Stage.RETURN) {
      n.say(CompassProgress.objective(p));
      return;
    }
    Item compass = SingletonRepository.getEntityManager().getItem(REWARD);
    if (compass == null) throw new IllegalStateException("Missing item: " + REWARD);
    compass.setBoundTo(p.getName());
    if (!p.equipToInventoryOnly(compass)) {
      n.say("Zwolnij jedno miejsce w plecaku na kompas i powiedz ponownie: #'kompas wędrowca'.");
      return;
    }
    p.put("compass_unlocked", 1);
    p.setQuest(SLOT, "done");
    p.addXP(5000);
    p.addKarma(10);
    new EquipItemAction("money", 100).fire(p, null, n);
    n.say(
        "Dobromir dotarł do domu. Miłorad ma spokojny grób, a jego kompas znowu prowadzi ludzi."
            + " Zachowaj go. Niech ci dobrze służy. A gdy ktoś znów zabłądzi, będę"
            + " wiedział, do kogo się zwrócić. Dziękuję.");
  }
}
