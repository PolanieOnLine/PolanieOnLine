/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;


import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import games.stendhal.client.ZoneInfo;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.Creature;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.j2d.Blend;
import games.stendhal.client.gui.styled.cursor.StendhalCursor;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/**
 * The 2D view of a creature.
 */
class Creature2DView extends RPEntity2DView<Creature> {
	private static final String WAWEL_DRAGON = "Smok Wawelski";

	private static final Color PHASE_75_COLOR = new Color(255, 179, 179);
	private static final Color PHASE_50_COLOR = new Color(255, 128, 128);
	private static final Color PHASE_25_COLOR = new Color(255, 92, 92);
	private static final Color WAWEL_PHASE_75_COLOR = new Color(255, 196, 164);
	private static final Color WAWEL_PHASE_50_COLOR = new Color(255, 160, 128);
	private static final Color WAWEL_PHASE_25_COLOR = new Color(255, 128, 96);

	private int currentTintPhase = -1;

	/**
	 * Populate named state sprites.
	 *
	 * @param map
	 *            The map to populate.
	 * @param tiles
	 *            The master sprite.
	 * @param width
	 *            The image width (in pixels).
	 * @param height
	 *            The image height (in pixels).
	 */
	@Override
	protected void buildSprites(final Map<Object, Sprite> map,
			final Sprite tiles, final int width, final int height) {
		this.width = width;
		this.height = height;

		super.buildSprites(map, tiles, width, height);
	}

	/**
	 * Get the full directional animation tile set for this entity.
	 *
	 * @return A tile sprite containing all animation images.
	 */
	@Override
	protected Sprite getAnimationSprite() {
		String resource = entity.getMetamorphosis();

		if (resource == null) {
			resource = getClassResourcePath();
		}

		final ZoneInfo info = ZoneInfo.get();
		final String spriteRef = translate(resource);
		final Color tintColor = getPhaseTintColor();

		if (tintColor != null) {
			if (WAWEL_DRAGON.equals(entity.getName())) {
				return addShadow(SpriteStore.get().getModifiedSprite(spriteRef, tintColor, Blend.SoftLight));
			}
			return addShadow(SpriteStore.get().getModifiedSprite(spriteRef, tintColor, Blend.Multiply));
		}

		return addShadow(SpriteStore.get().getModifiedSprite(spriteRef, info.getZoneColor(), info.getColorMethod()));
	}

	//
	// Entity2DView
	//



	/**
	 * Reorder the actions list (if needed). Please use as last resort.
	 *
	 * @param list
	 *            The list to reorder.
	 */
	@Override
	protected void reorderActions(final List<String> list) {
		if (list.remove(ActionType.ATTACK.getRepresentation())) {
			list.add(0, ActionType.ATTACK.getRepresentation());
		}
	}

	/**
	 * Translate a resource name into it's sprite image path.
	 *
	 * @param name
	 *            The resource name.
	 *
	 * @return The full resource name.
	 */
	@Override
	protected String translate(final String name) {
		return "data/sprites/monsters/" + name + ".png";
	}

	@Override
	void entityChanged(final Object property) {
		super.entityChanged(property);

		if (property == IEntity.PROP_CLASS) {
			representationChanged = true;
		} else if (property == Creature.PROP_METAMORPHOSIS) {
			representationChanged = true;
		} else if (property == Creature.PROP_BOSS_PHASE_THRESHOLD) {
			currentTintPhase = calculateTintPhase();
			representationChanged = true;
		}
	}

	private Color getPhaseTintColor() {
		currentTintPhase = calculateTintPhase();
		final boolean isWawelDragon = WAWEL_DRAGON.equals(entity.getName());

		if (currentTintPhase == 75) {
			return isWawelDragon ? WAWEL_PHASE_75_COLOR : PHASE_75_COLOR;
		} else if (currentTintPhase == 50) {
			return isWawelDragon ? WAWEL_PHASE_50_COLOR : PHASE_50_COLOR;
		} else if (currentTintPhase == 25) {
			return isWawelDragon ? WAWEL_PHASE_25_COLOR : PHASE_25_COLOR;
		}

		return null;
	}

	private int calculateTintPhase() {
		return entity.getBossPhaseThreshold();
	}

	//
	// EntityView
	//

	/**
	 * Perform the default action.
	 */
	@Override
	public void onAction() {
		onAction(ActionType.ATTACK);
	}

	@Override
	public StendhalCursor getCursor() {
		return StendhalCursor.ATTACK;
	}
}
