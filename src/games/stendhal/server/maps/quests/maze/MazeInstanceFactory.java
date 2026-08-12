/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.maps.quests.maze;

import games.stendhal.server.core.engine.StendhalRPZone;
import marauroa.common.game.IRPZone;
import marauroa.server.game.rp.InstanceZoneDescriptor;
import marauroa.server.game.rp.InstanceZoneFactory;

/** Creates one ephemeral Maze zone for InstanceZoneManager. */
public final class MazeInstanceFactory implements InstanceZoneFactory {
	private static final String DISPLAY_NAME = "Labirynt Haizena";

	private final String returnZoneName;
	private final int returnX;
	private final int returnY;
	private final MazeSign sign;
	private MazeGenerator generator;

	public MazeInstanceFactory(final String returnZoneName, final int returnX,
			final int returnY, final MazeSign sign) {
		this.returnZoneName = returnZoneName;
		this.returnX = returnX;
		this.returnY = returnY;
		this.sign = sign;
	}

	@Override
	public IRPZone create(final InstanceZoneDescriptor descriptor) {
		generator = new MazeGenerator(descriptor.getRuntimeZoneIdString(), 128, 128);
		generator.setReturnLocation(returnZoneName, returnX, returnY);
		generator.setSign(sign);
		final StendhalRPZone zone = generator.getZone();
		zone.getAttributes().put("readable_name", DISPLAY_NAME);
		return zone;
	}

	@Override
	public void destroy(final InstanceZoneDescriptor descriptor, final IRPZone zone) {
		MazeGenerator.cleanupTransientEntities((StendhalRPZone) zone);
	}

	/** Returns the generator created by the successful first acquire. */
	public MazeGenerator getGenerator() {
		return generator;
	}
}
