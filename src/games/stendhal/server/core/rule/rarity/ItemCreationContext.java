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
package games.stendhal.server.core.rule.rarity;

import java.util.Objects;

import games.stendhal.common.constants.ItemRarity;

/**
 * Describes why an item instance is being created and any rarity overrides.
 */
public final class ItemCreationContext {
	public enum Source {
		DEFAULT,
		DROP,
		QUEST,
		ADMIN,
		RESTORE
	}

	private static final String DEFAULT_PROFILE = "default";

	private final Source source;
	private final ItemRarity forcedRarity;
	private final ItemRarity questRarity;
	private final ItemRarity factoryRarity;
	private final ItemRarityModifiers modifiers;
	private final boolean randomizeModifiers;
	private final String profile;

	private ItemCreationContext(final Builder builder) {
		this.source = builder.source;
		this.forcedRarity = builder.forcedRarity;
		this.questRarity = builder.questRarity;
		this.factoryRarity = builder.factoryRarity;
		this.modifiers = builder.modifiers;
		this.randomizeModifiers = builder.randomizeModifiers;
		this.profile = builder.profile;
	}

	public static ItemCreationContext defaultCreation() {
		return builder(Source.DEFAULT).build();
	}

	public static ItemCreationContext drop() {
		return builder(Source.DROP).build();
	}

	public static ItemCreationContext quest() {
		return builder(Source.QUEST).withQuestRarity(ItemRarity.COMMON).build();
	}

	public static ItemCreationContext admin() {
		return builder(Source.ADMIN).build();
	}

	public static ItemCreationContext restore() {
		return builder(Source.RESTORE).build();
	}

	public static Builder builder(final Source source) {
		return new Builder(source);
	}

	public Source getSource() {
		return source;
	}

	/**
	 * @return resolved rarity using forced &gt; quest &gt; factory priority, or
	 *     {@code null} when the normal distribution should be used
	 */
	public ItemRarity getRarity() {
		return getResolvedRarity();
	}

	public ItemRarity getResolvedRarity() {
		if (forcedRarity != null) {
			return forcedRarity;
		}
		if (questRarity != null) {
			return questRarity;
		}
		return factoryRarity;
	}

	public ItemRarity getForcedRarity() {
		return forcedRarity;
	}

	public ItemRarity getQuestRarity() {
		return questRarity;
	}

	public ItemRarity getFactoryRarity() {
		return factoryRarity;
	}

	/** @return fixed modifiers, or {@code null} when none were supplied */
	public ItemRarityModifiers getModifiers() {
		return modifiers;
	}

	public boolean isRandomizeModifiers() {
		return randomizeModifiers;
	}

	public String getProfile() {
		return profile;
	}

	public boolean isRestore() {
		return source == Source.RESTORE;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ItemCreationContext)) {
			return false;
		}
		final ItemCreationContext other = (ItemCreationContext) obj;
		return source == other.source && forcedRarity == other.forcedRarity
				&& questRarity == other.questRarity
				&& factoryRarity == other.factoryRarity
				&& randomizeModifiers == other.randomizeModifiers
				&& Objects.equals(modifiers, other.modifiers)
				&& profile.equals(other.profile);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, forcedRarity, questRarity, factoryRarity, modifiers,
				Boolean.valueOf(randomizeModifiers), profile);
	}

	@Override
	public String toString() {
		return "ItemCreationContext[source=" + source + ", forcedRarity="
				+ forcedRarity + ", questRarity=" + questRarity
				+ ", factoryRarity=" + factoryRarity
				+ ", modifiers=" + modifiers + ", randomizeModifiers="
				+ randomizeModifiers + ", profile=" + profile + "]";
	}

	public static final class Builder {
		private final Source source;
		private ItemRarity forcedRarity;
		private ItemRarity questRarity;
		private ItemRarity factoryRarity;
		private ItemRarityModifiers modifiers;
		private boolean randomizeModifiers;
		private String profile = DEFAULT_PROFILE;

		private Builder(final Source source) {
			if (source == null) {
				throw new IllegalArgumentException("Item creation source must not be null");
			}
			this.source = source;
			this.randomizeModifiers = source != Source.QUEST && source != Source.RESTORE;
		}

		public Builder withRarity(final ItemRarity rarity) {
			if (source == Source.ADMIN) {
				return withForcedRarity(rarity);
			}
			if (source == Source.QUEST) {
				return withQuestRarity(rarity);
			}
			return withFactoryRarity(rarity);
		}

		public Builder withForcedRarity(final ItemRarity rarity) {
			if (rarity == null) {
				throw new IllegalArgumentException("Forced rarity must not be null");
			}
			this.forcedRarity = rarity;
			return this;
		}

		public Builder withQuestRarity(final ItemRarity rarity) {
			if (rarity == null) {
				throw new IllegalArgumentException("Quest rarity must not be null");
			}
			this.questRarity = rarity;
			return this;
		}

		public Builder withFactoryRarity(final ItemRarity rarity) {
			if (rarity == null) {
				throw new IllegalArgumentException("Factory rarity must not be null");
			}
			this.factoryRarity = rarity;
			return this;
		}

		public Builder withModifiers(final ItemRarityModifiers modifiers) {
			if (modifiers == null) {
				throw new IllegalArgumentException("Modifiers must not be null");
			}
			this.modifiers = modifiers;
			return this;
		}

		public Builder randomizeModifiers(final boolean randomizeModifiers) {
			this.randomizeModifiers = randomizeModifiers;
			return this;
		}

		public Builder withProfile(final String profile) {
			if (profile == null || profile.trim().length() == 0) {
				throw new IllegalArgumentException("Rarity profile must not be empty");
			}
			this.profile = profile.trim();
			return this;
		}

		public ItemCreationContext build() {
			return new ItemCreationContext(this);
		}
	}
}
