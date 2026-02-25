package games.stendhal.client.gui.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MasteryPresentationTest {

	@Test
	public void lockedStateShowsRequirementsAndProgress() {
		MasteryProgress progress = new MasteryProgress(false, 0, 0, 0, 5, 597, 2000, 2, 320);
		MasteryPresentation presentation = MasteryPresentation.from(progress);

		assertFalse(presentation.unlocked);
		assertTrue(presentation.headline.contains("5"));
		assertTrue(presentation.detail.contains("2/5"));
		assertTrue(presentation.detail.contains("320/597"));
	}

	@Test
	public void unlockedStateShowsProgressToNextLevel() {
		MasteryProgress progress = new MasteryProgress(true, 133, 1500, 3200, 5, 597, 2000, 5, 597);
		MasteryPresentation presentation = MasteryPresentation.from(progress);

		assertTrue(presentation.unlocked);
		assertTrue(presentation.headline.contains("133"));
		assertTrue(presentation.detail.contains("3200"));
		assertEquals(4700, presentation.progressMax);
		assertEquals(1500, presentation.progressValue);
	}

	@Test
	public void cappedStateShowsMaxWithoutNextExp() {
		MasteryProgress progress = new MasteryProgress(true, 2000, 999999, 0, 5, 597, 2000, 5, 597);
		MasteryPresentation presentation = MasteryPresentation.from(progress);

		assertTrue(presentation.unlocked);
		assertTrue(presentation.headline.contains("2000"));
		assertTrue(presentation.detail.contains("MAX 2000"));
		assertEquals(1, presentation.progressMax);
		assertEquals(1, presentation.progressValue);
	}
}
