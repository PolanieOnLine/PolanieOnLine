package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.glyph.GlyphExperienceService;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.DeathMatchCreature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/** Restore the meaning of a forgotten glyph through study, help and experience. */
public class FirstGlyph extends AbstractQuest {
    public static final String QUEST_SLOT = "glyph_knowledge_intro";
    private static final int MIN_LEVEL = 20;
    private static final List<String> SUBJECTS = Arrays.asList("glif", "glif wiedzy", "zatarty znak", "znak Starkada",
            "glyph", "knowledge glyph", "faded sign", "Starkad's sign", "glifo", "glifo del conocimiento", "signo borrado");
    private static final String[] CREATURES = {"goblin", "wilk", "dzik"};
    private static final int ALL_OBSERVATIONS = 7;
    private static final String TRAIL_HINT = "Wilki i dziki można spotkać na terenach na zachód od Semos. Zwykłe gobliny zamieszkują drugi poziom podziemi Semos. Nie trzeba zapuszczać się głębiej.";

    @Override
    public void addToWorld() {
        fillQuestInfo("Pamięć kamienia", "Starkad odnalazł zatarty glif dawnego wędrowca. Ceryl i Ilisa mogą pomóc odczytać jego historię oraz przywrócić mu moc.", false);
        addStarkadDialog();
        addLibraryDialog();
        addIlisaDialog();
    }

    private static String stage(final Player player) {
        return player.getQuest(QUEST_SLOT, 0);
    }

    private void addStarkadDialog() {
        final SpeakerNPC npc = npcs.get("Starkad");
        final List<String> triggers = new ArrayList<String>(ConversationPhrases.QUEST_MESSAGES);
        triggers.addAll(SUBJECTS);
        triggers.addAll(Arrays.asList("glify", "wiedza", "pierwszy glif", "glyphs", "knowledge", "glifos", "conocimiento", "misión", "mision"));
        npc.add(ConversationStates.ATTENDING, triggers, null, ConversationStates.ATTENDING,
                null, (player, sentence, speaker) -> request(player, speaker));
        final List<String> accept = new ArrayList<String>(ConversationPhrases.YES_MESSAGES);
        accept.addAll(Arrays.asList("sí", "si"));
        npc.add(ConversationStates.QUEST_OFFERED, accept, null,
                ConversationStates.ATTENDING, null, (player, sentence, speaker) -> {
            if (player.hasQuest(QUEST_SLOT) || player.getLevel() < MIN_LEVEL) {
                request(player, speaker);
                return;
            }
            player.setQuest(QUEST_SLOT, "archive");
            speaker.say("Oczyścić kamień spróbuje Ilisa, ale najpierw musimy zrozumieć zapis. Odnajdź Ceryla w bibliotece Semos i zapytaj o #'zatarty znak'. Pamiętam tylko słowa: nie zachowasz go, zatrzymując wyłącznie dla siebie. Ceryl może znać ich dalszy ciąg.");
        });
        npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
                ConversationStates.ATTENDING, "Rozumiem. Nie każda droga zaczyna się od razu. Wróć, gdy zechcesz poznać historię tego kamienia.", null);
    }

    private void request(final Player player, final EventRaiser speaker) {
        if (isCompleted(player)) {
            speaker.say("Pamięć kamienia należy już do ciebie. Noś glif wiedzy w miejscu użytkowym: daje o 10% więcej doświadczenia z potworów. Jego moc nie wygasa i nie wymaga ulepszania. Dbaj o niego, jak dawny wędrowiec dbał o swoich uczniów.");
        } else if ("return".equals(stage(player))) {
            award(player, speaker);
        } else if ("archive".equals(stage(player))) {
            speaker.say("Ceryl czeka w bibliotece Semos. Zapytaj o #'zatarty znak' i wysłuchaj opowieści o jego dawnym właścicielu. Samo ostrze nie odsłoni tej tajemnicy.");
        } else if ("herbs".equals(stage(player))) {
            speaker.say("O znaczeniu słów już wiesz. Odwiedź teraz Ilisę w świątyni Semos i zapytaj o #'zatarty znak'. To ona przechowuje kamień i wie, jak przywrócić czytelność jego rysom.");
        } else if ("trial".equals(stage(player))) {
            speaker.say(observations(player) == ALL_OBSERVATIONS
                    ? "Masz już trzy różne doświadczenia. Wróć do Ceryla i zapytaj o #'zatarty znak'. Pomoże ci zrozumieć, co łączy odnalezione symbole."
                    : "Wyrusz w drogę zgodnie ze wskazówkami Ilisy. Potrzebujesz doświadczenia ze spotkania z goblinem, wilkiem i dzikiem poza arenami. Nie chodzi o liczbę przeciwników, lecz o poznanie każdego z nich.");
        } else if (player.getLevel() < MIN_LEVEL) {
            speaker.say("Wróć po osiągnięciu 20 poziomu. Wtedy opowiem ci o kamieniu, który przechował więcej niż niejeden wojownik zdołał zapamiętać.");
        } else {
            speaker.say("Na dawnym szlaku odnalazłem kamień z zatartym znakiem. Należał do wędrowca, który uczył innych, a potem zniknął z kronik. Oddałem go Ilisie pod opiekę. Chcę poznać jego historię i przywrócić moc glifu. Potrzebuję kogoś, kto umie nie tylko walczyć, ale także słuchać i wyciągać wnioski. Podejmiesz się tego?");
            speaker.setCurrentState(ConversationStates.QUEST_OFFERED);
        }
    }

    private void addLibraryDialog() {
        final SpeakerNPC npc = npcs.get("Ceryl");
        npc.add(ConversationStates.ATTENDING, SUBJECTS, new QuestStartedCondition(QUEST_SLOT),
                ConversationStates.ATTENDING, null, (player, sentence, speaker) -> library(player, speaker));
        npc.add(ConversationStates.ATTENDING, Arrays.asList("historia", "historię", "historii", "wędrowiec", "uczeń", "history", "wanderer", "student"),
                new QuestStartedCondition(QUEST_SLOT), ConversationStates.ATTENDING,
                "W kronice opisano wędrowca, który wracał z wypraw z pełnymi notatnikami. Jego pierwszy uczeń pragnął zamknąć wszystkie zapiski w skrzyni, żeby nikt nie dorównał mistrzowi. Druga uczennica zaczęła uczyć mieszkańców rozpoznawania śladów i bezpiecznych dróg. Tylko przy niej znak na kamieniu pozostał jasny. W tym tkwi odpowiedź.", null);
        npc.add(ConversationStates.ATTENDING, Arrays.asList("wiedza", "wiedzą", "dzielenie się wiedzą", "knowledge", "conocimiento"),
                new QuestInStateCondition(QUEST_SLOT, 0, "archive"), ConversationStates.ATTENDING,
                null, (player, sentence, speaker) -> {
            player.setQuest(QUEST_SLOT, "herbs");
            speaker.say("Właśnie wiedza. Zamknięta w skrzyni przestaje służyć komukolwiek. Odwiedź Ilisę w świątyni i zapytaj o #'zatarty znak'. Kamień jest u niej. Powiedz, że odnaleźliśmy sens słów, lecz musimy jeszcze odczytać obrazy.");
        });
        npc.add(ConversationStates.ATTENDING, Arrays.asList("złoto", "siła", "pamięć", "gold", "strength", "memory", "oro", "fuerza", "memoria"),
                new QuestInStateCondition(QUEST_SLOT, 0, "archive"), ConversationStates.ATTENDING,
                "Pomyśl o uczniach wędrowca. Jeden zachował wszystko dla siebie, druga pomagała innym. Co można przekazać komuś, samemu tego nie tracąc? Jeśli potrzebujesz wskazówki, zapytaj o #historię.", null);
    }

    private void library(final Player player, final EventRaiser speaker) {
        if ("archive".equals(stage(player))) {
            speaker.say("Znak Starkada... Widziałem go w starej kronice. Otaczały go słowa: im więcej oddajesz innym, tym więcej pozostaje w krainie. Co to jest? Możesz najpierw zapytać o #historię dawnego wędrowca, a potem podać odpowiedź.");
        } else if ("herbs".equals(stage(player))) {
            speaker.say("Ilisa przechowuje kamień w świątyni Semos. Zapytaj ją o #'zatarty znak'. Odczytane słowa to dopiero początek, potrzebujemy jeszcze obrazów pod osadem.");
        } else if ("trial".equals(stage(player)) && observations(player) == ALL_OBSERVATIONS) {
            player.setQuest(QUEST_SLOT, "return");
            speaker.say("Łapa wilka, ślad dzika i nóż goblina. Trzy różne spotkania, z których wynosi się różne nauki. Wędrowiec nie zamknął w glifie siły swoich przeciwników, lecz zdolność uczenia się z kolejnych wypraw. Teraz rozumiesz jego dar. Wróć do Starkada i powiedz #'glif wiedzy'.");
        } else if ("trial".equals(stage(player))) {
            speaker.say("Brakuje jeszcze części doświadczeń opisanych przez Ilisę. Sprawdź dziennik zadania i poznaj każde z trzech stworzeń poza arenami, a potem wróć do mnie.");
        } else {
            speaker.say("Znaczenie zapisu jest już jasne. Glif pomaga uczyć się z wypraw, ale wiedzy nie powinno się zatrzymywać tylko dla siebie. Jeśli jeszcze nie odebrałeś daru, czeka u Starkada.");
        }
    }

    private void addIlisaDialog() {
        final SpeakerNPC npc = npcs.get("Ilisa");
        npc.add(ConversationStates.ATTENDING, Arrays.asList("tropy", "tracks", "huellas"),
                new QuestInStateCondition(QUEST_SLOT, 0, "trial"), ConversationStates.ATTENDING, TRAIL_HINT, null);
        npc.add(ConversationStates.ATTENDING, SUBJECTS, new QuestStartedCondition(QUEST_SLOT),
                ConversationStates.ATTENDING, null, (player, sentence, speaker) -> {
            if ("archive".equals(stage(player))) {
                speaker.say("Starkad zostawił mi kamień do oczyszczenia. Najpierw porozmawiaj z Cerylem w bibliotece Semos o #'zatarty znak'. Musimy wiedzieć, czego szukamy pod osadem.");
            } else if ("herbs".equals(stage(player))) {
                speaker.say("Ceryl odnalazł słowa? Dobrze. Na kamieniu zachowały się też trzy obrazy, ale zakrywa je osad. Przygotuję łagodny napar, który go rozpuści. Przynieś 3 arandule, a potem powiedz #'przygotuj napar'. Uważaj, żeby zbierać tylko właściwe zioła.");
            } else if ("trial".equals(stage(player))) {
                speaker.say("Na oczyszczonym kamieniu widać łapę wilka, ślad dzika i krzywy nóż goblina. Poznaj każde z tych stworzeń podczas zwykłej wyprawy. Po pokonaniu goblina, wilka i dzika poza arenami wróć do Ceryla. Dziennik zachowa twoje obserwacje.");
            } else {
                speaker.say("Kamień odzyskał swój znak. Starkad pomoże ci skorzystać z jego mocy. Pamiętaj, że nawet najprostsze spotkanie może czegoś nauczyć.");
            }
        });
        npc.add(ConversationStates.ATTENDING, Arrays.asList("przygotuj napar", "napar", "prepare the infusion", "infusion", "prepara la infusión", "infusión"),
                new QuestInStateCondition(QUEST_SLOT, 0, "herbs"), ConversationStates.ATTENDING,
                null, (player, sentence, speaker) -> {
            if (!player.dropSubmittable("arandula", 3)) {
                speaker.say("Do naparu potrzebuję trzech arandul. Przynieś brakujące zioła i ponownie powiedz #'przygotuj napar'.");
                return;
            }
            player.setQuest(QUEST_SLOT, "trial;0");
            speaker.say("Napar powoli zmywa osad. Spójrz: łapa wilka, ślad dzika i nóż goblina. Tych znaków nie wystarczy nazwać. Wyrusz w drogę i pokonaj poza arenami goblina, wilka oraz dzika. Wystarczy jedno zaliczone spotkanie z każdym, także podczas wspólnej walki. Potem wróć do Ceryla i opowiedz mu, czego cię nauczyły.");
        });
    }

    private void award(final Player player, final EventRaiser speaker) {
        final Item reward = SingletonRepository.getEntityManager().getItem(
                GlyphExperienceService.ITEM_NAME, ItemCreationContext.quest());
        if (reward == null) {
            speaker.say("Nie mogę teraz przygotować glifu. Wróć później, twój postęp pozostaje zapisany.");
            return;
        }
        reward.setBoundTo(player.getName());
        if (!player.equip("bag", reward)) {
            speaker.say("Zrób miejsce w plecaku i wróć po glif wiedzy. Będzie tu na ciebie czekał.");
            return;
        }
        player.setQuest(QUEST_SLOT, "done");
        player.incObtainedForItem(reward.getName(), 1);
        player.notifyWorldAboutChanges();
        speaker.say("Ceryl opowiedział mi, jak odczytałeś zapis. Kamień od Ilisy znów nosi wyraźny znak. Oddaję go tobie jako glif wiedzy. Załóż go w miejscu użytkowym, a z potworów otrzymasz o 10% więcej doświadczenia. Jego moc nie wygasa i nie ma poziomów. Jest przypisany do ciebie i pozostanie z tobą po śmierci. Nie zwiększa nagród za zadania ani treningu ataku i obrony. Korzystaj z niego i dziel się tym, czego się nauczysz.");
    }

    /** One personal credit per creature kind, only after the inscription is read. */
    public static void recordKill(final Player player, final RPEntity victim) {
        if (player == null || !"trial".equals(stage(player)) || !(victim instanceof Creature)
                || victim instanceof ArenaCreature || victim instanceof DeathMatchCreature
                || victim.getZone() == null) return;
        final String zone = victim.getZone().getName();
        if ("int_fado_battle_pit".equals(zone) || "int_fado_battle_arena".equals(zone)) return;
        for (int i = 0; i < CREATURES.length; i++) {
            if (CREATURES[i].equals(victim.getName())) {
                player.setQuest(QUEST_SLOT, "trial;" + (observations(player) | (1 << i)));
                return;
            }
        }
    }

    private static int observations(final Player player) {
        try {
            final int value = Integer.parseInt(player.getQuest(QUEST_SLOT, 1));
            return value >= 0 && value <= ALL_OBSERVATIONS ? value : 0;
        } catch (final NumberFormatException invalid) {
            return 0;
        }
    }

    @Override
    public List<String> getHistory(final Player player) {
        final List<String> history = new ArrayList<String>();
        if (!player.hasQuest(QUEST_SLOT)) return history;
        history.add("Starkad odnalazł zatarty glif dawnego wędrowca. Trzeba zrozumieć jego historię i przywrócić moc znaku.");
        if ("archive".equals(stage(player))) {
            history.add("Ceryl w bibliotece Semos może znać historię kamienia. Zapytam go o zatarty znak i odczytam sens starej zagadki.");
        } else {
            history.add("Ceryl wyjaśnił, że wiedza wzrasta, kiedy dzielimy się nią z innymi.");
            if ("herbs".equals(stage(player))) {
                history.add("Ilisa w świątyni Semos przechowuje kamień. Do naparu potrzebne są 3 arandule. Po dostarczeniu ziół trzeba powiedzieć: przygotuj napar.");
            } else {
                history.add("Napar Ilisy odsłonił trzy symbole. Do zadania liczą się spotkania poza arenami, po odczytaniu znaków.");
                history.add(TRAIL_HINT);
                final int seen = "trial".equals(stage(player)) ? observations(player) : ALL_OBSERVATIONS;
                if ((seen & 1) != 0) history.add("Poznany przeciwnik: goblin.");
                else history.add("Do poznania: goblin.");
                if ((seen & 2) != 0) history.add("Poznany przeciwnik: wilk.");
                else history.add("Do poznania: wilk.");
                if ((seen & 4) != 0) history.add("Poznany przeciwnik: dzik.");
                else history.add("Do poznania: dzik.");
                if ("trial".equals(stage(player)) && seen == ALL_OBSERVATIONS) {
                    history.add("Wszystkie trzy doświadczenia zebrane. Czas wrócić do Ceryla w bibliotece i zapytać o zatarty znak.");
                } else if ("return".equals(stage(player))) {
                    history.add("Ceryl odczytał znaczenie wszystkich symboli. Starkad czeka z glifem wiedzy. Potrzebne jest wolne miejsce w plecaku.");
                } else if (isCompleted(player)) {
                    history.add("Glif wiedzy jest mój. Założony w miejscu użytkowym zwiększa doświadczenie z potworów o 10%.");
                }
            }
        }
        return history;
    }

    @Override public String getSlotName() { return QUEST_SLOT; }
    @Override public String getName() { return "Pamięć kamienia"; }
    @Override public String getNPCName() { return "Starkad"; }
    @Override public int getMinLevel() { return MIN_LEVEL; }
    @Override public String getRegion() { return Region.SEMOS_SURROUNDS; }
}
