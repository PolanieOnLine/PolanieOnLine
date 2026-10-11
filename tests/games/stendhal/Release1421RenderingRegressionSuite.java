/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import games.stendhal.client.CameraSmootherTest;
import games.stendhal.client.PixelArtViewportTest;
import games.stendhal.client.PlayerFrameSynchronizationTest;
import games.stendhal.client.gui.j2d.entity.Entity2DViewTest;
import games.stendhal.client.gui.settings.ScreenScalingPreferenceTest;
import games.stendhal.client.sprite.TextSpriteTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({CameraSmootherTest.class, PixelArtViewportTest.class,
	Entity2DViewTest.class, ScreenScalingPreferenceTest.class, TextSpriteTest.class,
	PlayerFrameSynchronizationTest.class})
public class Release1421RenderingRegressionSuite {
	// JUnit suite marker.
}
