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
package games.stendhal.server.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;

public class ZoneDisplayNameFormatterTest {

	@Test
	public void formatsSimpleWorldDirectionAfterLocationName() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("0_semos_plains_n"),
				equalTo("Równiny Semos N"));
	}

	@Test
	public void preservesNumberedDirectionCode() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("0_semos_plains_n2e"),
				equalTo("Równiny Semos N2E"));
	}

	@Test
	public void preservesRepeatedDirectionLetters() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("0_semos_plains_nww"),
				equalTo("Równiny Semos NWW"));
	}

	@Test
	public void joinsSplitDirectionPartsUsedByExistingMaps() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("0_semos_mountain_n2_w3"),
				equalTo("Góry Semos N2W3"));
	}

	@Test
	public void removesTechnicalInteriorFloorZero() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("int_zakopane_bank_0"),
				equalTo("Bank Zakopane"));
	}

	@Test
	public void keepsMeaningfulInteriorFloorWithoutSeparators() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("int_zakopane_bank_2"),
				equalTo("Bank Zakopane Poziom 2"));
	}

	@Test
	public void formatsChallengeArenaAsPlayerFacingName() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("int_tarnow_challenge_arena"),
				equalTo("Arena Wyzwań"));
	}

	@Test
	public void translatesDragonRegionAndItsUndergroundLocations() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("0_dragon_land_nw"),
				equalTo("Smocza Kraina NW"));
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("alt_0_dragon_land_n"),
				equalTo("Smocza Kraina N Świat alternatywny"));
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("-4_dragon_cave"),
				equalTo("Jaskinia Smoczej Krainy, poziom -4"));
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("int_dragon_knights_dungeons"),
				equalTo("Lochy Smoczych Rycerzy"));
	}

	@Test
	public void translatesCompoundUndergroundNamesAndKeepsSectorAndDepth() {
		final String[][] locations = {
				{"-1_wieliczka_salt_mines_w", "Kopalnia soli Wieliczka W, poziom -1"},
				{"-1_zakopane_underground", "Podziemia Zakopane, poziom -1"},
				{"-1_semos_catacombs", "Katakumby Semos, poziom -1"},
				{"-1_koscielisko_coalmine", "Kopalnia węgla Kościelisko, poziom -1"},
				{"-1_krakow_canals", "Kanały Kraków, poziom -1"},
				{"-1_gdansk_underground_forest", "Podziemny las Gdańsk, poziom -1"},
				{"-1_fado_great_cave_n_w2", "Wielka jaskinia Fado NW2, poziom -1"},
				{"-3_deniran_lost_caves_n2_w", "Zaginione jaskinie Deniran N2W, poziom -3"},
				{"-1_nalwor_drows_tunnel", "Tunel drowów Nalwor, poziom -1"},
				{"-1_orril_dwarf_mine", "Kopalnia krasnoludów Orril, poziom -1"},
				{"-1_athor_sea_floor", "Dno morza Athor, poziom -1"}
		};
		for (final String[] location : locations) {
			assertThat(location[0], ZoneDisplayNameFormatter.formatTechnicalName(location[0]),
					equalTo(location[1]));
		}
	}

	@Test
	public void keepsCustomPrivateZoneName() {
		final StendhalRPZone zone = new StendhalRPZone("instance_secret_house_0123456789");
		zone.getAttributes().put("readable_name", "Kryjówka Witomira");

		assertThat(ZoneDisplayNameFormatter.format(zone), equalTo("Kryjówka Witomira"));
	}

	@Test
	public void doesNotExposePrivateInstanceIdWithoutReadableName() {
		final StendhalRPZone zone = new StendhalRPZone("instance_secret_house_0123456789");

		assertThat(ZoneDisplayNameFormatter.format(zone), equalTo("Prywatna lokacja"));
	}

	@Test
	public void unknownLocationWordsStillProduceReadableFallback() {
		assertThat(ZoneDisplayNameFormatter.formatTechnicalName("0_fado_arcane_quarter_n2e"),
				equalTo("Fado Arcane Quarter N2E"));
	}
}
