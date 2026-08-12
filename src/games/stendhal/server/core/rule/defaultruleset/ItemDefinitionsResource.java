/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.rule.defaultruleset;

import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import marauroa.common.resource.ReloadableResource;
import marauroa.common.resource.ResourceProvider;

/**
 * Safe runtime reload adapter for item definitions.
 *
 * Loading and XML parsing happen on the caller thread chosen by the admin
 * adapter. Validation prevents runtime changes to the set of item names. The
 * RP safe point performs only the final definition-map swap and cache reset.
 */
final class ItemDefinitionsResource implements ReloadableResource<Map<String, DefaultItem>> {
	static final String ID = "item-definitions";

	/**
	 * One stable resource object is registered for the application singleton.
	 * DefaultEntityManager may be constructed recursively while item
	 * implementation classes initialize, so registration must not depend on a
	 * particular constructor instance.
	 */
	private static final ItemDefinitionsResource SERVER_RESOURCE = new ItemDefinitionsResource(null);

	private final DefaultEntityManager entityManager;

	ItemDefinitionsResource(final DefaultEntityManager entityManager) {
		this.entityManager = entityManager;
	}

	static ItemDefinitionsResource getServerResource() {
		return SERVER_RESOURCE;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Map<String, DefaultItem> load(final ResourceProvider provider) throws Exception {
		return getEntityManager().loadItemDefinitions(provider);
	}

	@Override
	public void validate(final Map<String, DefaultItem> candidate) {
		getEntityManager().validateItemDefinitions(candidate);
	}

	@Override
	public void apply(final Map<String, DefaultItem> candidate) {
		getEntityManager().applyItemDefinitions(candidate);
	}

	private DefaultEntityManager getEntityManager() {
		if (entityManager != null) {
			return entityManager;
		}

		final EntityManager current = SingletonRepository.getEntityManager();
		if (!(current instanceof DefaultEntityManager)) {
			throw new IllegalStateException("item-definitions requires DefaultEntityManager");
		}
		return (DefaultEntityManager) current;
	}
}
