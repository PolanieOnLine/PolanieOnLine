package games.stendhal.client.entity;
/**
 * Client side representation of a pushable, solid block
 *
 * @author madmetzger
 */
public class Block extends StatefulEntity {

	@Override
	public boolean isObstacle(IEntity entity) {
		if ("questprop".equals(getEntityClass()) || "questuseable".equals(getEntityClass())) {
			// Player-private quest props are perceived only by their owner.
			// They must therefore participate in client-side prediction only
			// for the local user, never for other moving entities rendered by
			// the owner's client.
			return entity.isUser() && super.isObstacle(entity);
		}
		return true;
	}

}
