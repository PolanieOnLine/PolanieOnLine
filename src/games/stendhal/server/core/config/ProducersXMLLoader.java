package games.stendhal.server.core.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.ProducerRegister;
import marauroa.common.Pair;

public class ProducersXMLLoader extends DefaultHandler {
	private final static Logger logger = Logger.getLogger(ProducersXMLLoader.class);

	private static ProducersXMLLoader instance;
	private final static ProducerRegister producers = ProducerRegister.get();

	private String questSlot;
	private String welcome;

	private String npcName;

	private String itemName;
	private Map<String, Integer> items = new TreeMap<String, Integer>();
	private Map<String, Integer> resources = new TreeMap<String, Integer>();

	private List<String> activity;
	private int time;

	private boolean productionTag = false;

	/**
	 * Singleton access method.
	 *
	 * @return
	 *     The static instance.
	 */
	public static ProducersXMLLoader get() {
		if (instance == null) {
			instance = new ProducersXMLLoader();
		}

		return instance;
	}

	/**
	 * Private singleton constructor.
	 */
	private ProducersXMLLoader() {}

	public void init() {
		final String xml = "/data/conf/productions.xml";
		final InputStream in = getClass().getResourceAsStream(xml);

		if (in == null) {
			logger.info("Productions config (" + xml + ") not found, not loading");
			return;
		}

		try {
			load(new URI(xml));
			in.close();
		} catch (final SAXException | URISyntaxException | IOException e) {
			logger.error(e);
		}
	}

	public void load(final URI uri) throws SAXException {
		try {
			// parse the input
			final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			final InputStream is = ProducersXMLLoader.class.getResourceAsStream(uri.getPath());

			if (is == null) {
				throw new FileNotFoundException("cannot find resource '" + uri + "' in classpath");
			}

			try {
				saxParser.parse(is, this);
			} finally {
				is.close();
			}
		} catch (final ParserConfigurationException e) {
			logger.error(e);
		} catch (final IOException e) {
			logger.error(e);
			throw new SAXException(e);
		}
	}
	
	@Override
	public void startElement(final String namespaceURI, final String lName, final String qName, final Attributes attrs) {
		if (qName.equals("producer")) {
			npcName = attrs.getValue("npc");
			questSlot = attrs.getValue("slot");
			items = new LinkedHashMap<>();
			resources = new LinkedHashMap<>();
			itemName = "";
			welcome = "";
			activity = new LinkedList<String>();
			time = 0;
		} else if (qName.equals("welcome")) {
			welcome = attrs.getValue("text");
		} else if (qName.equals("activity")) {
			final String[] activities = attrs.getValue("type").split(",");
			for (String activ : activities) {
				activity.add(activ);
			}
		} else if (qName.equals("time")) {
			time = Integer.parseInt(attrs.getValue("value"));
		} else if (qName.equals("item")) {
			itemName = attrs.getValue("name");
			items.put(attrs.getValue("name"), Integer.parseInt(attrs.getValue("quantity")));
			productionTag = true;
		} else if (productionTag) {
			if (qName.equals("resource")) {
				resources.put(attrs.getValue("name"), Integer.parseInt(attrs.getValue("amount")));
			}
		}
	}

	@Override
	public void endElement(final String namespaceURI, final String sName, final String qName) {
		if (qName.equals("producer") && questSlot != null) {
			final ProducerBehaviour behaviour = new ProducerBehaviour(questSlot, activity, itemName, resources, time);
			if (npcName != null && !resources.isEmpty()) {
				for (final Pair<String, ProducerBehaviour> producer : producers.getProducers()) {
					if (producer.first().contains(npcName)) {
						logger.warn("Tried to re-add duplicate production to an existing producer " + npcName);
						return;
					}
				}

				producers.add(npcName, behaviour);
				SingletonRepository.getCachedActionManager().register(new Runnable() {
					public void run() {
						producers.configureNPC(npcName, behaviour, welcome);
					}
				});
			}
		}
	}
}
