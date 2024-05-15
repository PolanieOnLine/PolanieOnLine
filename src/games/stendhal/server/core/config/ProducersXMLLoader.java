package games.stendhal.server.core.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import games.stendhal.server.core.config.ProducerGroupsXMLLoader.ProducerConfigurator;

public class ProducersXMLLoader extends DefaultHandler {
	private final static Logger logger = Logger.getLogger(ProducersXMLLoader.class);

	private static ProducersXMLLoader instance;
	private List<ProducerConfigurator> configurators;

	private String npcName;
	private String questSlot;
	private String questComplete;

	private String welcome;

	private String itemName;
	private int productsPerUnit;

	private Map<String, Integer> resources = new HashMap<String, Integer>();
	private List<String> activity = new LinkedList<String>();

	private int unitsPerTime;
	private int waiting;
	private int time;
	
	private boolean remind;
	private boolean bound;

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

	public void init() {
		final String xml = "/data/conf/producers.xml";
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
			questComplete = null;
			if (attrs.getValue("complete") != null) {
				questComplete = attrs.getValue("complete");
			}
			resources = new LinkedHashMap<String, Integer>();
			activity = new LinkedList<String>();
			itemName = null;
			unitsPerTime = 0;
			waiting = 0;
			welcome = null;
			remind = false;
			if (attrs.getValue("remind") != null) {
				remind = Boolean.parseBoolean(attrs.getValue("remind"));
			}
		} else if (qName.equals("welcome")) {
			welcome = attrs.getValue("text");
		} else if (qName.equals("item")) {
			productionTag = true;

			itemName = attrs.getValue("name");
			productsPerUnit = 1;
			if (attrs.getValue("quantity") != null) {
				productsPerUnit = Integer.parseInt(attrs.getValue("quantity"));
			}
			if (attrs.getValue("pertime") != null) {
				unitsPerTime = Integer.parseInt(attrs.getValue("pertime"));
			}
			if (attrs.getValue("wait") != null) {
				waiting = Integer.parseInt(attrs.getValue("wait"));
			}
			// Time in minutes
			time = 60 * Integer.parseInt(attrs.getValue("minutes"));

			bound = false;
			if (attrs.getValue("bound") != null) {
				bound = Boolean.parseBoolean(attrs.getValue("bound"));
			}
		} else if (productionTag) {
			if (qName.equals("resource")) {
				int amount = 1;
				if (attrs.getValue("amount") != null) {
					amount = Integer.parseInt(attrs.getValue("amount"));
				}
				resources.put(attrs.getValue("name"), amount);
			} else if (qName.equals("activity")) {
				final String[] activities = attrs.getValue("type").split(",");
				for (String activ : activities) {
					activity.add(activ);
				}
			}
		}
	}

	@Override
	public void endElement(final String namespaceURI, final String sName, final String qName) {
		if (qName.equals("producer")) {
			final ProducerConfigurator pc = new ProducerConfigurator();
			pc.npc = npcName;
			pc.questComplete = questComplete;
			pc.welcomeMessage = welcome;
			pc.unitsPerTime = unitsPerTime;
			pc.waitingTime = waiting;
			pc.remind = remind;

			pc.questSlot = questSlot;
			pc.produceItem = itemName;
			pc.produceResources = resources;
			pc.produceActivity = activity;
			pc.producePerUnit = productsPerUnit;
			pc.produceTime = time;
			pc.itemBound = bound;

			configurators.add(pc);
		} else if (qName.equals("item")) {
			productionTag = false;
		}
	}

	public List<ProducerConfigurator> getConfigurators() {
		return configurators;
	}
}
