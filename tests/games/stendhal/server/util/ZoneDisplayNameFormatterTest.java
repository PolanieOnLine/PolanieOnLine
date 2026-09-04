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
