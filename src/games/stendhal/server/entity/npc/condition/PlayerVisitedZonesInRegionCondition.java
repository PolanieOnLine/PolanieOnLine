/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.condition;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Checks if player has visited certain zones in a region
 *
 * @author madmetzger
 */
@Dev(category=Category.IGNORE, label="Zone?")
public class PlayerVisitedZonesInRegionCondition implements ChatCondition {

	private enum VisitPreference {
		ANY(null),
		REQUIRE_TRUE(Boolean.TRUE),
		REQUIRE_FALSE(Boolean.FALSE);

		private final Boolean booleanValue;

		VisitPreference(final Boolean booleanValue) {
			this.booleanValue = booleanValue;
		}

		static VisitPreference fromNullable(final Boolean value) {
			if (value == null) {
				return ANY;
			}
			return value.booleanValue() ? REQUIRE_TRUE : REQUIRE_FALSE;
		}

		Boolean toNullableBoolean() {
			return booleanValue;
		}
	}

	private final String region;

	private final VisitPreference exterior;

	private final VisitPreference aboveGround;

	private final VisitPreference accessible;

	/**
	 * Creates a new PlayerVisitedZonesCondition
	 *
	 * @param region the name of the region to consider
	 * @param exterior	the exterior filter preference
	 * @param aboveGround	the above-ground filter preference
	 * @param accessible	the accessibility filter preference
	 */
	public PlayerVisitedZonesInRegionCondition(String region, VisitPreference exterior,
			VisitPreference aboveGround, VisitPreference accessible) {
		this.region = checkNotNull(region);
		this.exterior = checkNotNull(exterior);
		this.aboveGround = checkNotNull(aboveGround);
		this.accessible = checkNotNull(accessible);
	}

	/**
	 * Creates a new PlayerVisitedZonesCondition
	 *
	 * @param region the name of the region to consider
	 * @param exterior	outside zones? true, false, null
	 * @param aboveGround	above ground level? true, false, null
	 */
	public PlayerVisitedZonesInRegionCondition(String region, VisitPreference exterior,
			VisitPreference aboveGround) {
		this(region, exterior, aboveGround, VisitPreference.REQUIRE_TRUE);
	}

	public PlayerVisitedZonesInRegionCondition(String region, Boolean exterior,
			Boolean aboveGround, Boolean accessible) {
		this(region, VisitPreference.fromNullable(exterior), VisitPreference.fromNullable(aboveGround),
			VisitPreference.fromNullable(accessible));
	}

	public PlayerVisitedZonesInRegionCondition(String region, Boolean exterior,
			Boolean aboveGround) {
		this(region, exterior, aboveGround, Boolean.TRUE);
	}

	@Override
	public boolean fire(Player player, Sentence sentence, Entity npc) {
		Collection<StendhalRPZone> zones = SingletonRepository.getRPWorld().getAllZonesFromRegion(region,
				exterior.toNullableBoolean(), aboveGround.toNullableBoolean(), accessible.toNullableBoolean());
		for (StendhalRPZone zone : zones) {
			if (!player.hasVisitedZone(zone)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 45763 * region.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PlayerVisitedZonesInRegionCondition)) {
			return false;
		}
		PlayerVisitedZonesInRegionCondition other = (PlayerVisitedZonesInRegionCondition) obj;
		return region.equals(other.region)
				&& exterior == other.exterior
				&& aboveGround == other.aboveGround
				&& accessible == other.accessible;
	}

	@Override
	public String toString() {
		return "player visited <region: " + region + ", exterior: " + exterior + ", above ground: " + aboveGround
				+ ", accessible: " + accessible + ">";
	}

}
