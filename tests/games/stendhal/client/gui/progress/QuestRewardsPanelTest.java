package games.stendhal.client.gui.progress;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;
import games.stendhal.client.entity.User;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.junit.Test;
import games.stendhal.common.QuestRewardPreview;
import games.stendhal.server.events.ProgressStatusEvent;
import utilities.RPClass.ItemTestHelper;

public class QuestRewardsPanelTest {
	@Test
	public void rewardSlotUsesEquipmentTooltipWithStatisticsInsteadOfDescription()
			throws Exception {
		ItemTestHelper.generateRPClasses();
		QuestRewardPreview rewards = new QuestRewardPreview(0, 0, false,
				Collections.singletonList(new QuestRewardPreview.Item(
						"płaszcz chaosu", "cloak", "chaos_cloak", 1, "rare",
						"Separate description", false)),
				Collections.emptyList());
		ProgressStatusEvent event = new ProgressStatusEvent("Zadania główne",
				Collections.emptyList());
		rewards.write(event, false);
		event.put("reward_stats", Collections.singletonList(
				"{\"category\":\"armour\",\"def\":\"42\",\"min_level\":\"100\"}"));
		SwingUtilities.invokeAndWait(() -> {
			QuestRewardsPanel panel = new QuestRewardsPanel(
					QuestRewardPreview.read(event));
			int found = 0;
			for (Component row : panel.getComponents()) {
				if (!(row instanceof Container)) {
					continue;
				}
				for (Component component : ((Container) row).getComponents()) {
					if (component instanceof JComponent) {
						String tooltip = ((JComponent) component).getToolTipText();
						if (tooltip != null) {
							assertTrue("Reward must handle actual mouse hover",
									Arrays.asList(component.getMouseListeners()).contains(
											ToolTipManager.sharedInstance()));
							assertTrue(tooltip, tooltip.contains("42 pkt."));
							assertTrue(tooltip, tooltip.contains("100"));
							assertFalse(
									tooltip.contains("Separate description"));
							found++;
						}
					}
				}
			}
			assertEquals("Icon and caption share the statistics tooltip", 2,
					found);
		});
	}

	@Test
	public void comparisonUsesCurrentEquipmentWithoutReopeningTheJournal()
			throws Exception {
		ItemTestHelper.generateRPClasses();
		Field proxy = User.class.getDeclaredField("userProxy");
		proxy.setAccessible(true);
		Object previous = proxy.get(null);
		try {
			SwingUtilities.invokeAndWait(() -> {
				Map<String, String> stats = new HashMap<>();
				stats.put("category", "armour");
				stats.put("equipment_slots", "cloak");
				stats.put("def", "22");
				QuestRewardPreview.Item item = new QuestRewardPreview.Item(
						"płaszcz chaosu", "cloak", "chaos_cloak", 1, "rare", "",
						false, stats);
				QuestRewardsPanel panel = new QuestRewardsPanel(
						new QuestRewardPreview(0, 0, false,
								Collections.singletonList(item),
								Collections.emptyList()));
				JComponent slot = (JComponent) ((Container) panel.getComponent(
						1)).getComponent(0);
				slot.getToolTipText();
				RPObject player = new RPObject();
				RPSlot cloak = new RPSlot("cloak");
				player.addSlot(cloak);
				RPObject equipped = ItemTestHelper.createItem("old cloak");
				equipped.put("class", "cloak");
				equipped.put("tooltip_stats", "category", "armour");
				equipped.put("tooltip_stats", "def", "30");
				cloak.add(equipped);
				new User() {
					@Override
					public RPObject getRPObject() {
						return player;
					}
				};
				assertTrue(slot.getToolTipText(),
						slot.getToolTipText().contains("old cloak"));
				equipped.put("name", "new cloak");
				assertTrue(slot.getToolTipText(),
						slot.getToolTipText().contains("new cloak"));
				assertFalse(slot.getToolTipText().contains("old cloak"));
			});
		} finally {
			proxy.set(null, previous);
		}
	}

}
