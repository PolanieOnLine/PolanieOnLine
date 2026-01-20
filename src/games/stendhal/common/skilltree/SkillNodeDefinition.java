package games.stendhal.common.skilltree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes a single node inside a skill tree definition.
 */
public final class SkillNodeDefinition {

    private final String id;
    private final String branch;
    private final String displayName;
    private final String description;
    private final List<String> prerequisites;
    private final double damageModifierBonus;
    private final double burnDamageMultiplier;
    private final int burnDurationBonusSeconds;
    private final boolean automaticallyUnlocked;

    private SkillNodeDefinition(Builder builder) {
        this.id = builder.id;
        this.branch = builder.branch;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.prerequisites = Collections.unmodifiableList(new ArrayList<String>(builder.prerequisites));
        this.damageModifierBonus = builder.damageModifierBonus;
        this.burnDamageMultiplier = builder.burnDamageMultiplier;
        this.burnDurationBonusSeconds = builder.burnDurationBonusSeconds;
        this.automaticallyUnlocked = builder.automaticallyUnlocked;
    }

    public String getId() {
        return id;
    }

    public String getBranch() {
        return branch;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public double getDamageModifierBonus() {
        return damageModifierBonus;
    }

    public double getBurnDamageMultiplier() {
        return burnDamageMultiplier;
    }

    public int getBurnDurationBonusSeconds() {
        return burnDurationBonusSeconds;
    }

    public boolean isAutomaticallyUnlocked() {
        return automaticallyUnlocked;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link SkillNodeDefinition}. */
    public static final class Builder {
        private String id;
        private String branch;
        private String displayName;
        private String description;
        private List<String> prerequisites = new ArrayList<String>();
        private double damageModifierBonus;
        private double burnDamageMultiplier = 1.0;
        private int burnDurationBonusSeconds;
        private boolean automaticallyUnlocked;

        private Builder() {
        }

        public Builder id(String value) {
            this.id = value;
            return this;
        }

        public Builder branch(String value) {
            this.branch = value;
            return this;
        }

        public Builder displayName(String value) {
            this.displayName = value;
            return this;
        }

        public Builder description(String value) {
            this.description = value;
            return this;
        }

        public Builder addPrerequisite(String value) {
            this.prerequisites.add(value);
            return this;
        }

        public Builder prerequisites(List<String> values) {
            this.prerequisites.clear();
            this.prerequisites.addAll(values);
            return this;
        }

        public Builder damageModifierBonus(double value) {
            this.damageModifierBonus = value;
            return this;
        }

        public Builder burnDamageMultiplier(double value) {
            this.burnDamageMultiplier = value;
            return this;
        }

        public Builder burnDurationBonusSeconds(int value) {
            this.burnDurationBonusSeconds = value;
            return this;
        }

        public Builder automaticallyUnlocked(boolean value) {
            this.automaticallyUnlocked = value;
            return this;
        }

        public SkillNodeDefinition build() {
            return new SkillNodeDefinition(this);
        }
    }
}

