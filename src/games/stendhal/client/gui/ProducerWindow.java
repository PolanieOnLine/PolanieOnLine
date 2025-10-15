/***************************************************************************
*                 (C) Copyright 2024 - PolanieOnLine                 *
***************************************************************************
***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.GameObjects;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.NPC;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import marauroa.common.game.RPAction;

class ProducerWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ProducerWindow.class);
	private static final int MAX_DISTANCE = 5;
	private static final int MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;
	private static final int PADDING = 8;
	private static final String SLOT_IMAGE = "data/gui/slot.png";
	private static final Pattern DIACRITIC_PATTERN = Pattern.compile("\\p{M}+");
	private static final Pattern NON_LETTER_OR_DIGIT = Pattern.compile("[^\\p{L}\\p{Nd}]");

	private final JComponent content;
	private final ProducerData data = new ProducerData();

	ProducerWindow() {
		super("producer", "Produkcja");
		content = SBoxLayout.createContainer(SBoxLayout.VERTICAL, PADDING);
		setContent(content);
		setHideOnClose(true);
		setMinimizable(true);
		setMovable(true);
		setVisible(false);
	}

	void showForNearestProducer() {
		showForProducer(null, null);
	}

	void showForProducer(final String npcName, final String npcTitle) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				NPC npc = findProducer(npcName, npcTitle);
				ProducerDefinition definition = null;
				if (npc != null) {
					definition = data.getDefinition(npc);
				}
				if (definition == null) {
					if ((npcName != null) && !npcName.isEmpty()) {
						definition = data.getDefinition(npcName);
					}
					if ((definition == null) && (npcTitle != null) && !npcTitle.isEmpty()) {
						definition = data.getDefinition(npcTitle);
					}
				}
				if (definition == null) {
					if (npc != null) {
						LOGGER.warn("No production data available for NPC: " + npc.getTitle());
					}
					String name = npcTitle != null ? npcTitle : npcName;
					if (name == null) {
						displayMessage("Brak producentów w pobliżu.");
					} else {
						displayMessage("Brak danych produkcji dla " + name + ".");
					}
					return;
				}

				populate(definition);
				setVisible(true);
				raise();
			}
		});
	}

	private static String normalizeWord(String text) {
		if (text == null) {
			return "";
		}

		String trimmed = text.trim();
		if (trimmed.isEmpty()) {
			return "";
		}

		String lower = trimmed.toLowerCase(Locale.ROOT);
		String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
		String withoutDiacritics = DIACRITIC_PATTERN.matcher(decomposed).replaceAll("");
		return NON_LETTER_OR_DIGIT.matcher(withoutDiacritics).replaceAll("");
	}


        private void populate(ProducerDefinition definition) {
                boolean centerWindow = !isVisible();

                content.removeAll();
                setTitle("Produkcja - " + definition.getDisplayName());

                List<ProducerProduct> products = definition.getProducts();
                if (products.isEmpty()) {
                        displayMessage("Brak produktów do wytworzenia.");
                        return;
                }

                for (ProducerProduct product : products) {
                        content.add(createProductRow(definition, product));
                }

                refreshLayout(centerWindow);
        }

        private void displayMessage(String message) {
                boolean centerWindow = !isVisible();

                content.removeAll();
                JLabel label = new JLabel(message, SwingConstants.CENTER);
                content.add(label);

                refreshLayout(centerWindow);
                setVisible(true);
                raise();
        }

        private void refreshLayout(boolean centerWindow) {
                content.revalidate();
                content.repaint();

                Dimension preferred = getPreferredSize();
                if ((preferred.width <= 0) || (preferred.height <= 0)) {
                        preferred = content.getPreferredSize();
                }

                if ((preferred.width > 0) && (preferred.height > 0)) {
                        setSize(preferred);
                }

                if (centerWindow && (getParent() != null)) {
                        center();
                }
        }

	private NPC findNearestProducer() {
		if (User.isNull()) {
			return null;
		}

		User user = User.get();
		GameObjects gameObjects = GameObjects.getInstance();
		NPC closest = null;
		double best = Double.MAX_VALUE;

		for (IEntity entity : gameObjects) {
			if (!(entity instanceof NPC)) {
				continue;
			}

			NPC npc = (NPC) entity;
			if (!isProducer(npc)) {
				continue;
			}

			double dx = npc.getX() - user.getX();
			double dy = npc.getY() - user.getY();
			double distSq = dx * dx + dy * dy;
			if (distSq <= MAX_DISTANCE_SQ && distSq < best) {
				best = distSq;
				closest = npc;
			}
		}

		return closest;
	}

	private NPC findProducer(String npcName, String npcTitle) {
		boolean hasName = npcName != null && !npcName.isEmpty();
		boolean hasTitle = npcTitle != null && !npcTitle.isEmpty();
		if (!hasName && !hasTitle) {
			return findNearestProducer();
		}

		GameObjects gameObjects = GameObjects.getInstance();
		for (IEntity entity : gameObjects) {
			if (!(entity instanceof NPC)) {
				continue;
			}

			NPC npc = (NPC) entity;
			if (!isProducer(npc)) {
				continue;
			}

			if (hasTitle) {
				String title = npc.getTitle();
				if ((title != null) && title.equalsIgnoreCase(npcTitle)) {
					return npc;
				}
			}

			if (hasName) {
				if (npc.getName().equalsIgnoreCase(npcName)) {
					return npc;
				}
			}
		}

		return null;
	}

	private boolean isProducer(NPC npc) {
		return data.getDefinition(npc) != null;
	}

	private JComponent createProductRow(ProducerDefinition definition, ProducerProduct product) {
		JComponent row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, 10);
		row.setAlignmentY(Component.CENTER_ALIGNMENT);

		JComponent resourcesPanel = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, 4);
		resourcesPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		List<ProducerResource> resources = product.getResources();
		if (resources.isEmpty()) {
			resourcesPanel.add(createSlot("Brak", 0, null));
		} else {
			for (ProducerResource resource : resources) {
				Sprite sprite = data.getSprite(resource.getName());
				resourcesPanel.add(createSlot(resource.getName(), resource.getAmount(), sprite));
			}
		}
		row.add(resourcesPanel);

		JLabel arrow = new JLabel("⇒", SwingConstants.CENTER);
		Font font = arrow.getFont();
		arrow.setFont(font.deriveFont(Font.BOLD, font.getSize() + 6.0f));
		arrow.setAlignmentY(Component.CENTER_ALIGNMENT);
		row.add(arrow);

		JComponent productPanel = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 4);
		productPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		Sprite productSprite = data.getSprite(product.getName());
		productPanel.add(createSlot(product.getName(), product.getQuantity(), productSprite));
		if (product.getMinutes() > 0) {
			productPanel.add(new JLabel("Czas: " + product.getMinutes() + " min"));
		}
		row.add(productPanel);

		row.add(createActionPanel(definition, product));

		return row;
	}

	private JComponent createSlot(String name, int amount, Sprite sprite) {
		JComponent container = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 2);
		container.setAlignmentY(Component.CENTER_ALIGNMENT);

		SlotComponent slot = new SlotComponent(sprite, amount);
		if (name != null) {
			String tooltip = name;
			if (amount > 1) {
				tooltip = name + " x" + amount;
			}
			slot.setToolTipText(tooltip);
		}
		container.add(slot);

		if (name != null) {
			JLabel label = new JLabel(name, SwingConstants.CENTER);
			container.add(label);
		}

		return container;
	}

	private JComponent createActionPanel(ProducerDefinition definition, ProducerProduct product) {
		JComponent panel = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 4);
		panel.setAlignmentY(Component.CENTER_ALIGNMENT);

		int baseAmount = Math.max(1, product.getQuantity());
		SpinnerNumberModel model = new SpinnerNumberModel(baseAmount, baseAmount, 1000, baseAmount);
		JSpinner spinner = new JSpinner(model);
		spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(spinner);

		List<String> activities = definition.getActivities();
		String[] activityArray = activities.toArray(new String[activities.size()]);
		JComboBox<String> activitySelect = new JComboBox<String>(activityArray);
		activitySelect.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(activitySelect);

		String defaultLabel = activities.isEmpty() ? "Produkuj" : activities.get(0);
		JButton button = new JButton(defaultLabel);
		button.setEnabled(!activities.isEmpty());
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(button);

		activitySelect.addActionListener(event -> {
			Object selected = activitySelect.getSelectedItem();
			if (selected != null) {
				button.setText(selected.toString());
			}
		});

		button.addActionListener(event -> {
			Object selected = activitySelect.getSelectedItem();
			String activity = selected != null ? selected.toString() : defaultLabel;
			if (activity == null || activity.isEmpty()) {
				return;
			}

			int amount = ((Number) spinner.getValue()).intValue();
			sendProductionRequest(activity, product.getName(), amount);
		});

		return panel;
	}

	private void sendProductionRequest(String activity, String product, int amount) {
		StringBuilder text = new StringBuilder();
		text.append(activity);
		if (amount > 0) {
			text.append(' ').append(amount);
		}
		if (product != null && !product.isEmpty()) {
			text.append(' ').append(product);
		}

		RPAction action = new RPAction();
		action.put("type", "chat");
		action.put("text", text.toString());
		ClientSingletonRepository.getClientFramework().send(action);
	}

	private static final class ProducerData {
		private static final String PRODUCTIONS = "data/conf/productions.xml";
		private static final Map<String, ProducerDefinition> DEFINITIONS = new HashMap<String, ProducerDefinition>();
		private static final ItemSpriteLookup SPRITES = new ItemSpriteLookup();
		private static boolean loaded;

		ProducerData() {
			synchronized (ProducerData.class) {
				if (!loaded) {
					load();
					loaded = true;
				}
			}
		}

		ProducerDefinition getDefinition(String name) {
			if (name == null) {
				return null;
			}
			return DEFINITIONS.get(normalize(name));
		}

		ProducerDefinition getDefinition(NPC npc) {
			if (npc == null) {
				return null;
			}

			ProducerDefinition definition = getDefinition(npc.getTitle());
			if (definition != null) {
				return definition;
			}

			return getDefinition(npc.getName());
		}

		boolean matchesActivity(String normalizedWord) {
			if (normalizedWord == null || normalizedWord.isEmpty()) {
				return false;
			}

			for (ProducerDefinition definition : DEFINITIONS.values()) {
				if (definition.matchesActivity(normalizedWord)) {
					return true;
				}
			}

			return false;
		}

		Sprite getSprite(String itemName) {
			return SPRITES.getSprite(itemName);
		}

		private void load() {
			Path baseFile = Paths.get(PRODUCTIONS);
			if (!Files.exists(baseFile)) {
				LOGGER.warn("Productions configuration missing: " + baseFile);
				return;
			}

			try {
				Document doc = parse(baseFile);
				Element root = doc.getDocumentElement();
				if (root == null) {
					return;
				}
				Path baseDir = baseFile.getParent();
				for (Element element : getChildren(root, "group")) {
					String uri = element.getAttribute("uri");
					if (uri == null || uri.isEmpty()) {
						continue;
					}
					Path file = baseDir.resolve(uri);
					if (Files.exists(file)) {
						loadProductions(file);
					} else {
						LOGGER.warn("Production file missing: " + file);
					}
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				LOGGER.error("Unable to load production definitions", e);
			}
		}

		private void loadProductions(Path file) {
			try {
				Document doc = parse(file);
				Element root = doc.getDocumentElement();
				if (root == null) {
					return;
				}
				for (Element production : getChildren(root, "production")) {
					Element producerElement = getChild(production, "producer");
					Element itemElement = getChild(production, "item");
					if (producerElement == null || itemElement == null) {
						continue;
					}

					String npcName = producerElement.getAttribute("name");
					if (npcName == null || npcName.isEmpty()) {
						continue;
					}

					ProducerDefinition definition = DEFINITIONS.get(normalize(npcName));
					if (definition == null) {
						definition = new ProducerDefinition(npcName);
						DEFINITIONS.put(normalize(npcName), definition);
					}

					definition.addActivities(parseActivities(producerElement.getAttribute("activities")));
					definition.addProduct(parseProduct(itemElement));
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				LOGGER.error("Unable to parse production file: " + file, e);
			}
		}

		private ProducerProduct parseProduct(Element item) {
			String name = item.getAttribute("name");
			int quantity = parseInt(item.getAttribute("quantity"), 1);
			int minutes = parseInt(item.getAttribute("minutes"), 0);

			List<ProducerResource> resources = new ArrayList<ProducerResource>();
			for (Element resource : getChildren(item, "resource")) {
				String resourceName = resource.getAttribute("name");
				if (resourceName == null || resourceName.isEmpty()) {
					continue;
				}
				int amount = parseInt(resource.getAttribute("amount"), 1);
				resources.add(new ProducerResource(resourceName, amount));
			}

			return new ProducerProduct(name, quantity, minutes, resources);
		}

		private List<Element> getChildren(Element parent, String name) {
			if (parent == null) {
				return Collections.emptyList();
			}
			List<Element> elements = new ArrayList<Element>();
			for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child instanceof Element) {
					Element element = (Element) child;
					if (matchesLocalName(element, name)) {
						elements.add(element);
					}
				}
			}
			return elements;
		}

		private boolean matchesLocalName(Element element, String name) {
			String localName = element.getLocalName();
			if (localName != null) {
				return localName.equals(name);
			}
			return element.getTagName().equals(name);
		}

		private Document parse(Path file) throws ParserConfigurationException, SAXException, IOException {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			try (InputStream in = Files.newInputStream(file)) {
				return builder.parse(in);
			}
		}

		private Element getChild(Element parent, String name) {
			for (Element element : getChildren(parent, name)) {
				return element;
			}
			return null;
		}

		private int parseInt(String value, int defaultValue) {
			if (value == null || value.isEmpty()) {
				return defaultValue;
			}
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}

		private List<String> parseActivities(String activities) {
			if (activities == null || activities.isEmpty()) {
				return Collections.emptyList();
			}
			String[] tokens = activities.split(",");
			List<String> list = new ArrayList<String>();
			for (String token : tokens) {
				String trimmed = token.trim();
				if (!trimmed.isEmpty()) {
					list.add(trimmed);
				}
			}
			return list;
		}

		private String normalize(String text) {
			return text.toLowerCase(Locale.ROOT).trim();
		}
	}

	private static final class ItemSpriteLookup {
		private static final String ITEMS = "data/conf/items.xml";
		private static final Map<String, String> ITEM_MAP = new HashMap<String, String>();
		private static boolean loaded;

		ItemSpriteLookup() {
			synchronized (ItemSpriteLookup.class) {
				if (!loaded) {
					load();
					loaded = true;
				}
			}
		}

		Sprite getSprite(String name) {
			if (name == null) {
				return null;
			}
			String key = normalize(name);
			String path = ITEM_MAP.get(key);
			if (path == null) {
				return null;
			}

			try {
				Sprite sprite = SpriteStore.get().getSprite("/data/sprites/items/" + path + ".png");
				if (sprite.getWidth() > sprite.getHeight()) {
					sprite = SpriteStore.get().getAnimatedSprite(sprite, 100);
				}
				return sprite;
			} catch (RuntimeException e) {
				LOGGER.warn("Unable to load sprite for item: " + name + " (" + path + ")", e);
				return null;
			}
		}

		private void load() {
			Path baseFile = Paths.get(ITEMS);
			if (!Files.exists(baseFile)) {
				LOGGER.warn("Items configuration missing: " + baseFile);
				return;
			}

			try {
				Document doc = parse(baseFile);
				Element root = doc.getDocumentElement();
				if (root == null) {
					return;
				}
				Path baseDir = baseFile.getParent();
				for (Element element : getChildren(root, "group")) {
					String uri = element.getAttribute("uri");
					if (uri == null || uri.isEmpty()) {
						continue;
					}
					Path file = baseDir.resolve(uri);
					if (Files.exists(file)) {
						loadItems(file);
					}
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				LOGGER.error("Unable to load item definitions", e);
			}
		}

		private void loadItems(Path file) {
			try {
				Document doc = parse(file);
				Element root = doc.getDocumentElement();
				if (root == null) {
					return;
				}
				for (Element item : getChildren(root, "item")) {
					String name = item.getAttribute("name");
					if (name == null || name.isEmpty()) {
						continue;
					}
					Element type = getChild(item, "type");
					if (type == null) {
						continue;
					}
					String clazz = type.getAttribute("class");
					String subclass = type.getAttribute("subclass");
					if (clazz == null || clazz.isEmpty() || subclass == null || subclass.isEmpty()) {
						continue;
					}
					ITEM_MAP.put(normalize(name), clazz + "/" + subclass);
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				LOGGER.error("Unable to parse item file: " + file, e);
			}
		}

		private Document parse(Path file) throws ParserConfigurationException, SAXException, IOException {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			try (InputStream in = Files.newInputStream(file)) {
				return builder.parse(in);
			}
		}

		private List<Element> getChildren(Element parent, String name) {
			if (parent == null) {
				return Collections.emptyList();
			}
			List<Element> elements = new ArrayList<Element>();
			for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child instanceof Element) {
					Element element = (Element) child;
					if (matchesLocalName(element, name)) {
						elements.add(element);
					}
				}
			}
			return elements;
		}

		private boolean matchesLocalName(Element element, String name) {
			String localName = element.getLocalName();
			if (localName != null) {
				return localName.equals(name);
			}
			return element.getTagName().equals(name);
		}

		private Element getChild(Element parent, String name) {
			for (Element element : getChildren(parent, name)) {
				return element;
			}
			return null;
		}

		private String normalize(String text) {
			return text.toLowerCase(Locale.ROOT).trim();
		}
	}

	private static final class ProducerDefinition {
		private final String displayName;
		private final List<ProducerProduct> products = new ArrayList<ProducerProduct>();
		private final List<String> activities = new ArrayList<String>();
		private final Set<String> normalizedActivities = new LinkedHashSet<String>();

		ProducerDefinition(String displayName) {
			this.displayName = displayName;
		}

		void addProduct(ProducerProduct product) {
			if (product != null) {
				products.add(product);
			}
		}

		void addActivities(Collection<String> newActivities) {
			if (newActivities == null || newActivities.isEmpty()) {
				return;
			}

			LinkedHashSet<String> unique = new LinkedHashSet<String>(activities);
			for (String activity : newActivities) {
				if (activity == null) {
					continue;
				}
				String trimmed = activity.trim();
				if (trimmed.isEmpty()) {
					continue;
				}
				unique.add(trimmed);
			}

			activities.clear();
			activities.addAll(unique);

			for (String activity : unique) {
				String normalized = normalizeWord(activity);
				if (!normalized.isEmpty()) {
					normalizedActivities.add(normalized);
				}
			}
		}

		String getDisplayName() {
			return displayName;
		}

		List<ProducerProduct> getProducts() {
			return Collections.unmodifiableList(products);
		}

		List<String> getActivities() {
			return Collections.unmodifiableList(activities);
		}

		boolean matchesActivity(String normalizedWord) {
			return normalizedActivities.contains(normalizedWord);
		}
	}

	private static final class ProducerProduct {
		private final String name;
		private final int quantity;
		private final int minutes;
		private final List<ProducerResource> resources;

		ProducerProduct(String name, int quantity, int minutes, List<ProducerResource> resources) {
			this.name = name;
			this.quantity = quantity;
			this.minutes = minutes;
			this.resources = resources;
		}

		String getName() {
			return name;
		}

		int getQuantity() {
			return quantity;
		}

		int getMinutes() {
			return minutes;
		}

		List<ProducerResource> getResources() {
			return resources;
		}
	}

	private static final class ProducerResource {
		private final String name;
		private final int amount;

		ProducerResource(String name, int amount) {
			this.name = name;
			this.amount = amount;
		}

		String getName() {
			return name;
		}

		int getAmount() {
			return amount;
		}
	}

	private static final class SlotComponent extends JComponent {
		private static final long serialVersionUID = 1L;
		private final Sprite background = SpriteStore.get().getSprite(SLOT_IMAGE);
		private final Sprite sprite;
		private final String amountText;

		SlotComponent(Sprite sprite, int amount) {
			this.sprite = sprite;
			this.amountText = amount > 1 ? String.valueOf(amount) : null;
			int width = background.getWidth();
			int height = background.getHeight();
			Dimension size = new Dimension(width, height);
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			background.draw(g, 0, 0);
			if (sprite != null) {
				int x = (getWidth() - sprite.getWidth()) / 2;
				int y = (getHeight() - sprite.getHeight()) / 2;
				sprite.draw(g, x, y);
			}
			if (amountText != null) {
				Font old = g.getFont();
				Font derived = old.deriveFont(Font.BOLD);
				g.setFont(derived);
				FontMetrics metrics = g.getFontMetrics();
				int x = getWidth() - metrics.stringWidth(amountText) - 3;
				int y = getHeight() - metrics.getDescent();
				g.setColor(Color.WHITE);
				g.drawString(amountText, x, y);
				g.setFont(old);
			}
		}
	}
}
