/***************************************************************************
 *                   (C) Copyright 2024 - Polanie OnLine                   *
 ***************************************************************************/
package games.stendhal.client.gui.npc;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JMenuItem;

import games.stendhal.client.entity.NPC;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.StatusID;
import games.stendhal.client.gui.j2d.entity.EntityView;
import games.stendhal.client.gui.wt.EntityViewCommandList;
import games.stendhal.client.gui.wt.core.WtPopupMenu;
import marauroa.common.game.RPObject;

/**
 * Context popup dedicated to NPC conversations.
 */
public class NPCInteractionMenu extends WtPopupMenu {
	private static final long serialVersionUID = 1L;

	private final NPC npc;
	private final EntityView<?> view;
	private final String[] defaultActions;
	private final Component anchor;
	private final Point popupLocation;
	private final NPCInteractionManager manager;
	private final QuestNpcRegistry questRegistry;

	public NPCInteractionMenu(final NPC npc, final EntityView<?> view, final String[] defaultActions,
				final Component anchor, final Point popupLocation) {
		super((npc != null) ? npc.getTitle() : "npc");
		this.npc = npc;
		this.view = view;
		this.defaultActions = defaultActions;
		this.anchor = anchor;
		this.popupLocation = popupLocation;
		this.manager = NPCInteractionManager.get();
		this.questRegistry = QuestNpcRegistry.get();

		buildMenu();
	}

	private void buildMenu() {
		removeAll();

		if (!manager.isInteracting(npc)) {
			add(createMenuItem("Przywitaj się", () -> manager.startInteraction(npc)));
		} else {
			addConversationItems();
			add(createMenuItem("Zakończ rozmowę", () -> manager.endInteraction(npc)));
		}

		add(createMenuItem("Anuluj", () -> setVisible(false)));
		addDefaultActions();
	}

	private void addConversationItems() {
		if (hasJobOption()) {
			add(createMenuItem("Praca", () -> manager.requestJob(npc)));
		}
		if (hasOfferOption()) {
			add(createMenuItem("Oferta", () -> manager.requestOffer(npc)));
		}
		add(createMenuItem("Pomoc", () -> manager.requestHelp(npc)));
		if (hasQuestOption()) {
			add(createMenuItem("Zadanie", () -> manager.requestQuest(npc)));
		}
	}

	private JMenuItem createMenuItem(final String label, final Runnable action) {
		final JMenuItem item = createItem(label, null);
		item.addActionListener(e -> {
			setVisible(false);
			action.run();
		});
		return item;
	}

	private void addDefaultActions() {
		if ((defaultActions == null) || (defaultActions.length == 0) || (anchor == null) || (popupLocation == null)) {
			return;
		}

		addSeparator();
		add(createMenuItem("Pozostałe akcje…", this::showDefaultMenu));
	}

	private void showDefaultMenu() {
		final EntityViewCommandList fallback = new EntityViewCommandList(npc.getType(), defaultActions, view);
		fallback.show(anchor, popupLocation.x, popupLocation.y);
	}

	private boolean hasJobOption() {
		if (!(npc instanceof RPEntity)) {
			return false;
		}
		final RPEntity entity = (RPEntity) npc;
		if (entity.hasStatus(StatusID.HEALER) || entity.hasStatus(StatusID.PRODUCER)) {
			return true;
		}
		final RPObject object = npc.getRPObject();
		return (object != null) && (object.has("job") || object.has("jobinfo"));
	}

	private boolean hasOfferOption() {
		if (!(npc instanceof RPEntity)) {
			return false;
		}
		final RPEntity entity = (RPEntity) npc;
		if (entity.hasStatus(StatusID.MERCHANT)) {
			return true;
		}
		final RPObject object = npc.getRPObject();
		return (object != null) && object.has("shop");
	}

	private boolean hasQuestOption() {
		return questRegistry.hasQuestFor(npc);
	}
}
