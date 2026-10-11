package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

public class ChallengeArenaManagerTest {
	@After
	public void tearDown() {
		ChallengeArenaManager.clearForTests();
	}

	@Test
	public void playersCanReserveIndependentArenasUntilRelease() {
		assertTrue(ChallengeArenaManager.reserve("Alice"));
		assertTrue(ChallengeArenaManager.isReserved());
		assertTrue(ChallengeArenaManager.isReservedBy("Alice"));
		assertTrue(ChallengeArenaManager.reserve("Bob"));

		ChallengeArenaManager.release("Alice");

		assertFalse(ChallengeArenaManager.isReservedBy("Alice"));
		assertTrue(ChallengeArenaManager.isReservedBy("Bob"));
		ChallengeArenaManager.release("Bob");
		assertFalse(ChallengeArenaManager.isReserved());
	}

	@Test
	public void samePlayerMayReenterReservationPathSafely() {
		assertTrue(ChallengeArenaManager.reserve("Alice"));
		assertTrue(ChallengeArenaManager.reserve("Alice"));
		assertTrue(ChallengeArenaManager.isReservedBy("Alice"));
	}
}
