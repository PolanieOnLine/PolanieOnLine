package games.stendhal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import games.stendhal.server.maps.quests.HuntingTest;
import games.stendhal.server.entity.item.HuntingRewardChestTest;
import games.stendhal.server.maps.quests.hunting.HuntingBalanceTest;

/** Shared by master and 1.42.1; tests always run against an in-memory database. */
@RunWith(Suite.class)
@Suite.SuiteClasses({HuntingTest.class, HuntingRewardChestTest.class, HuntingBalanceTest.class})
public class Release1421HuntingRegressionSuite { }
