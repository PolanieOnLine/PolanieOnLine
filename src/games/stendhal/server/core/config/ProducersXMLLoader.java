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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.ProducerRegister;

public class ProducersXMLLoader extends DefaultHandler {
	private final static Logger logger = Logger.getLogger(ProducersXMLLoader.class);

	private static ProducersXMLLoader instance;
	private final static ProducerRegister producers = ProducerRegister.get();

	private String questSlot;
	private String welcome;

	private String npcName;

	private String itemName;
	private int productsPerUnit;

	private Map<String, Integer> resources = new HashMap<String, Integer>();
	private List<String> activity = new LinkedList<String>();

	private int unitsPerTime;
	private int waiting;
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
			resources = new LinkedHashMap<String, Integer>();
			activity = new LinkedList<String>();
			itemName = null;
			unitsPerTime = 0;
			waiting = 0;
			welcome = "";
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
		} else if (productionTag) {
			if (qName.equals("resource")) {
				resources.put(attrs.getValue("name"), Integer.parseInt(attrs.getValue("amount")));
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
			final ProducerBehaviour behaviour =
					new ProducerBehaviour(questSlot, activity, itemName, productsPerUnit, resources, unitsPerTime, waiting, time);

			if (behaviour.getProductionActivity() == activity) {
				SingletonRepository.getCachedActionManager().register(new Runnable() {
					private final String _npcName = npcName;
					private final ProducerBehaviour _behaviour = behaviour;
					private final String _welcome = welcome;
	
					public void run() {
						producers.configureNPC(_npcName, _behaviour, _welcome);
					}
				});
			}
		} else if (qName.equals("item")) {
			productionTag = false;
		}
	}
}
