package games.stendhal.server.core.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

public class ProducersXMLLoader extends DefaultHandler {
	private final static Logger logger = Logger.getLogger(ProducersXMLLoader.class);

	private static boolean initialized = false;

	private Map<String, ProducerBehaviour> behaviours;
	private List<ProducerConfigurator> configurators;

	private String currentNPCName;
	private String currentSlot;
	private String currentCompleteQuest;
	private ProducerBehaviour currentBehaviour;

	private String currentMessage;

	private String currentItem;
	private int productionPerCycle;

	private Map<String, Integer> requiredResources = new HashMap<String, Integer>();
	private List<String> currentActivities = new LinkedList<String>();

	private int minProductionUnits;
	private int timeToRestart;
	private int productionTime;
	
	private boolean remind;
	private boolean bound;

	private boolean productionTag = false;

	private static ProducersXMLLoader instance;

	/**
	 * Singleton access method.
	 *
	 * @return
	 *     The static instance.
	 */
	@Deprecated
	public static ProducersXMLLoader get() {
		if (instance == null) {
			instance = new ProducersXMLLoader();
		}

		return instance;
	}

	@Deprecated
	public void init() {
		if (initialized) {
			logger.warn("Tried to re-initialize productions loader");
			return;
		}

		final String xml = "/data/conf/productions.xml";
		final InputStream in = getClass().getResourceAsStream(xml);

		if (in == null) {
			logger.info("Productions config (" + xml + ") not found, not loading");
			return;
		}

		try {
			load(new URI(xml));
			in.close();
			initialized = true;
		} catch (final SAXException | URISyntaxException | IOException e) {
			logger.error(e);
		}
	}

	public void load(final URI uri) throws SAXException {
		try {
			// reset data
			behaviours = new HashMap<>();
			configurators = new ArrayList<>();


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
		if (qName.equals("production")) {
			currentSlot = attrs.getValue("name");
			currentCompleteQuest = null;
			if (attrs.getValue("complete") != null) {
				currentCompleteQuest = attrs.getValue("complete");
			}
			remind = false;
			if (attrs.getValue("remind") != null) {
				remind = Boolean.parseBoolean(attrs.getValue("remind"));
			}

			//currentProduction = null;
			currentBehaviour = null;
			requiredResources = new LinkedHashMap<String, Integer>();
			currentActivities = new LinkedList<String>();
			currentItem = null;
			minProductionUnits = 1;
			timeToRestart = 0;
			currentNPCName = null;
			currentMessage = null;
		} else if (qName.equals("item")) {
			productionTag = true;

			currentItem = attrs.getValue("name");
			String _quantity = attrs.getValue("quantity");
			productionPerCycle = Integer.parseInt(_quantity != null ? _quantity : "1"); // The amount that the NPC can produce. Default: 1
			String _minProduction = attrs.getValue("pertime");
			minProductionUnits = Integer.parseInt(_minProduction != null ? _minProduction : "0"); // The amount that the NPC can produce at one time. Default: 0
			String _produceCooldown = attrs.getValue("wait");
			timeToRestart = Integer.parseInt(_produceCooldown != null ? _produceCooldown : "0"); // Cooldown before next production. Default: 0
			productionTime = 60 * Integer.parseInt(attrs.getValue("minutes")); // Time in minutes.
			String _isBound = attrs.getValue("bound");
			bound = Boolean.parseBoolean(_isBound != null ? _isBound : "false");
		} else if (productionTag) {
			if (qName.equals("resource")) {
				int amount = 1;
				if (attrs.getValue("amount") != null) {
					amount = Integer.parseInt(attrs.getValue("amount"));
				}
				requiredResources.put(attrs.getValue("name"), amount);
			}
		} else if (qName.equals("producer")) {
			final ProducerConfigurator pc = new ProducerConfigurator();
			currentNPCName = attrs.getValue("name");
			currentMessage = attrs.getValue("message");

			final String[] activities = attrs.getValue("activities").split(",");
			for (String activ : activities) {
				currentActivities.add(activ);
			}

			currentBehaviour = new ProducerBehaviour(currentSlot, currentActivities, currentItem, productionPerCycle, requiredResources, productionTime, bound);

			pc.npcName = currentNPCName;
			pc.behaviour = currentBehaviour;
			pc.questComplete = currentCompleteQuest;
			pc.welcomeMessage = currentMessage;
			pc.minProductionUnits = minProductionUnits;
			pc.timeToRestart = timeToRestart;
			pc.remind = remind;

			configurators.add(pc);
		}
	}

	@Override
	public void endElement(final String namespaceURI, final String sName, final String qName) {
		if (qName.equals("production")) {
			if (currentBehaviour.getProductionActivity() == currentActivities) {
				SingletonRepository.getCachedActionManager().register(new Runnable() {
					private final String _npcName = currentNPCName;
					private final ProducerBehaviour _behaviour = currentBehaviour;

					public void run() {
						if (!behaviours.containsKey(_npcName)) {
							behaviours.put(_npcName, _behaviour);
						}
					}
				});
			}
		} else if (qName.equals("item")) {
			productionTag = false;
		}
	}

	public Map<String, ProducerBehaviour> getBehaviours() {
		return behaviours;
	}

	public List<ProducerConfigurator> getConfigurators() {
		return configurators;
	}
}