package games.stendhal.client.fx.model;

import java.util.Objects;

/**
 * Lightweight representation of an account character.
 */
public final class CharacterSlot {
        private final String name;
        private final int level;
        private final String profession;

        public CharacterSlot(String name, int level, String profession) {
                        this.name = Objects.requireNonNull(name, "name");
                        this.level = level;
                        this.profession = (profession == null) ? "" : profession;
        }

        public String getName() {
                return name;
        }

        public int getLevel() {
                return level;
        }

        public String getProfession() {
                return profession;
        }

        @Override
        public String toString() {
                return name;
        }
}
