/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.janosik;

import java.util.Arrays;
import java.util.List;

public interface IRAQuestConstants {
	/**
	 * related to quest part.
	 * <ul>
	 * <li> INACTIVE - quest isn't active
	 * <li> INVASION - part I (rats invasion)
	 * <li> AWAITING - part II (pied piper called)
	 * <li> OUTGOING - part III (pied piper killing rats)
	 * <li> CHILDRENS - part IV (pied piper takes childrens away)
	 * <li> FINAL - part V (return childrens back to Ados)
	 * </ul>
	 */
	public enum RA_Phase {
		RA_INACTIVE,
		RA_INVASION,
		RA_AWAITING,
		RA_OUTGOING,
		RA_FINAL
	}

	public RA_Phase INACTIVE = RA_Phase.RA_INACTIVE;
	public RA_Phase INVASION = RA_Phase.RA_INVASION;
	public RA_Phase AWAITING = RA_Phase.RA_AWAITING;
	public RA_Phase OUTGOING = RA_Phase.RA_OUTGOING;
	public RA_Phase FINAL = RA_Phase.RA_FINAL;

	final String QUEST_SLOT = "janosik";

	final String INACTIVE_TIME_MAX = "QUEST_INACTIVE_TIME_MAX";
	final String INACTIVE_TIME_MIN = "QUEST_INACTIVE_TIME_MIN";
	final String INVASION_TIME_MIN = "QUEST_INVASION_TIME_MIN";
	final String INVASION_TIME_MAX = "QUEST_INVASION_TIME_MAX";
	final String AWAITING_TIME_MIN = "QUEST_AWAITING_TIME_MIN";
	final String AWAITING_TIME_MAX = "QUEST_AWAITING_TIME_MAX";
	final String OUTGOING_TIME_MIN = "QUEST_OUTGOING_TIME_MIN";
	final String OUTGOING_TIME_MAX = "QUEST_OUTGOING_TIME_MAX";
	final String FINAL_TIME_MIN = "QUEST_FINAL_TIME_MIN";
	final String FINAL_TIME_MAX = "QUEST_FINAL_TIME_MAX";
	final String SHOUT_TIME = "QUEST_SHOUT_TIME";

	/**
	 * List of game zones, where rats will appears.
	 *
	 * TODO: add other Ados buildings here, and improve summonRats() function
	 *       to avoid placing rats inside closed areas within houses.
	 */
	public final List<String> MONSTER_ZONES = Arrays.asList(
			"int_zakopane_hospital",
			"int_zakopane_hostel",
			"int_zakopane_sewing_house_0",
			"int_zakopane_sewing_house_1",
			"int_zakopane_bakery",
			"int_zakopane_blacksmith",
			"int_zakopane_townhall",
			"int_zakopane_shop",
			"int_zakopane_chapel",
			"int_zakopane_seller_house",
			"int_zakopane_mill_0",
			"int_zakopane_church",
			"int_zakopane_stable",
			"0_zakopane_c",
			"0_zakopane_e",
			"0_zakopane_s",
			"0_zakopane_sw",
			"0_zakopane_se",
			"int_tatry_kuznice_blacksmith",
			"int_tatry_kuznice_tavern",
			"int_tatry_kuznice_chapel",
			"int_tatry_kuznice_hostel",
			"0_tatry_dolina_malej_laki",
			"0_tatry_dolina_strazyska",
			"0_tatry_kuznice");

	/**
	 * List of creatures types to create.
	 */
	public final List<String> MONSTER_TYPES = Arrays.asList(
			"zbójnik leśny",
			"zbójnik leśny oszust",
			"zbójnik leśny zwiadowca",
			"zbójnik górski",
			"zbójnik górski goniec",
			"zbójnik górski złośliwy",
			"zbójnik górski zwiadowca",
			"zbójnik górski herszt");

	/**
	 * List of reward moneys quantities for each type of killed rats.
	 */
	public final List<Integer> MONSTER_REWARDS = Arrays.asList(
			50,
			100,
			160,
			240,
			380,
			500,
			740,
			1000);

}
