/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * provides a single point where to define support message templates
 * @author madmetzger
 *
 */
public class SupportMessageTemplatesFactory {

	private static final String TEMPLATE_PREFIX = "$";

	private final Map<String, String> messageTemplates;
	/**
	 * creates a new instance and initializes the templates
	 */
	public SupportMessageTemplatesFactory() {
		this.messageTemplates = new HashMap<String, String>();
		registerTemplates();
	}

	/**
	 * registers the available templates. use %s to personalize a template with the name of the asking player in the greeting. but you can only use %s once.
	 */
	private void registerTemplates() {
		addTemplate("$atlas", "Witaj %s. Zdarza się, że od czasu do czasu ktoś się zgubi. Możesz skorzystać z mapy Faiumoni wywołując ją komendą #/atlas");
		addTemplate("$banprivate", "Witaj %s przykro mi, ale nie mogę podać powodu zablokowania inny graczy ze względu na poufność.");
		addTemplate("$bugstracker","Cześć %s. Wygląda na to, że znalazłeś nowy błąd. Gdybyś mógł zostawić wiadomośc ze szczegółowym opisem jak do tego doszło na #http://www.polskagra.net/bugzilla to byłbym wdzięczny - bardzo dziękuję.");
		addTemplate("$faq", "Cześć. Odpowiedź na swoje pytanie znajdziesz w FAQ, który jest bardzo pomocny więc przeczytaj go uważnie! #http://www.polskagra.net/faq. Dziękujemy, że jesteś z nami!");
		addTemplate("$faqpvp","Witaj %s i przykro słyszeć o tym. Przeczytaj uważnie regulamin dostępny na http://www.polskagra.net/regulamin-gry-mmorpg - powodzenia w przyszłości.");
		addTemplate("$faqsocial", "Hi %s, sorry to hear about that. Please read #http://stendhalgame.org/wiki/StendhalFAQ#Player_social_problems which covers some common problems.");
		addTemplate("$firewallserver", "Hi %s, I am sorry but we cannot help you with the configuration of your router or firewall. It is rather dangerous to modify those settings without knowing exactly what you are doing. So this should only be done by an experienced network administrator who will find instructions in the manual that came with the hardware router or operating system.");
		addTemplate("$ignore","Cześć %s. Przykro nam, że masz nie przyjemności ze strony innego wojownika. Spróbuj go zignorować używając #/ignore #wojownik, aby zablokować jego wiadomości pochodzące od niego.");
		addTemplate("$knownbug","Cześć %s. Dziękuję, że powiedziałeś nam o tym bugu. Jest już zgłoszony i pracujemy nad jego usunięciem. Dziękuję!");
		addTemplate("$notsupport","Cześć %s. Nie możemy pomóc Tobie w tym problemie. Skorzystaj z #http://www.polskagra.net/bugzilla.");
		addTemplate("$password","Witaj %s możesz zmienić hasło wysyłając e-mail z adresu podanego przy rejestracji konta na adres haslo@polskagra.net w tytule wpisując Zmiana hasła, a w treści podając nazwę postaci oraz nowe hasło.");
		addTemplate("$rules","Cześć %s. Przeczytaj uważnie Regulamin PolskaGRA wpisując komendę #/rules - dziękuję.");
		addTemplate("$spam","Witaj %s. Powtarzanie tych samych słów w kółko i w kółko jest uznane jako spamowanie i jest to sprzeczne z zasadami gry. Nie spamuj i przeczytaj regulamin wpisując komendę #'/rules'. Dziękuję.");
		addTemplate("$thief","Witaj %s. Zapytaj Dagobert o #handel, aby dowiedzieć się jak bezpiecznie handlować. Wsparcie nie może odzyskać utraconych przedmiotów z wyniku nieuwagi.");
	}

	/**
	 * registers a template name with the corresponding text
	 *
	 * @param templateName
	 * @param templateText
	 */
	private void addTemplate(String templateName, String templateText) {
		StringBuilder nameBuilder = new StringBuilder();
		if(!templateName.startsWith(TEMPLATE_PREFIX)) {
			nameBuilder.append(TEMPLATE_PREFIX);
		}
		nameBuilder.append(templateName);
		messageTemplates.put(nameBuilder.toString(), templateText);
	}

	/**
	 * returns the map of templates
	 *
	 * @return a map of the template names as key and template text as value
	 */
	public Map<String, String> getTemplates() {
		return messageTemplates;
	}

}
