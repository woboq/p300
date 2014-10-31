/**
 * 
 */
package de.guruz.p300.eventstream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.Host;

/**
 * The event manager.<br/>
 * Contains a list of events.<br/>
 * Can save to the persistent event file.<br/>
 * Gets notified on new events.<br/>
 * @author tomcat
 *
 */
public class EventManager {

	/**
	 * The Singleton instance of the EventManager
	 */
	private static EventManager instance;
	
	/**
	 * Lock object for the singleton
	 */
	private static Object lock = new Object();
	
	/**
	 * The list of events
	 */
	private List<Event> eventList;
	
	/**
	 * The persistent storage for events
	 */
	private File eventFile = new File(de.guruz.p300.Configuration.configDirFileName("events.rss"));
	
	/**
	 * Create a new, clear event list
	 */
	private void clearEventList() {
		eventList = Collections.synchronizedList(new ArrayList<Event>());
	}
	
	/**
	 * Create a new EventManager instance.<br/>
	 * Load all events from persistent storage.
	 */
	private EventManager() {
		clearEventList();
		loadEvents();
	}
	
	/**
	 * Return the Singleton instance of the EventManager.<br/>
	 * If there is none, create one.<br/>
	 * @return The EventManager instance
	 */
	public static EventManager getInstance() {
		synchronized (lock) {
			if (instance == null) {
				instance = new EventManager();
			}
			return instance;
		}
	}
	
	/**
	 * Load events from persistent storage
	 */
	private void loadEvents() {
		try {
			synchronized (eventFile) {
				FileReader fileReader = new FileReader(eventFile);
				Host h = getOwnHost();
				EventReader eventReader = new EventReader(fileReader, h);
				clearEventList();
				Event[] events = eventReader.readEvents();
				for (Event e: events) {
					eventList.add(e);
				}
			}
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * Save events to persistent storage
	 */
	private void saveEvents() {
		try {
			synchronized (eventFile) {
				FileWriter fileWriter = new FileWriter(eventFile);
				Host h = getOwnHost();
				EventWriter eventWriter = new EventWriter(fileWriter, h);
				eventWriter.writeEvents(eventList.toArray(new Event[0]));
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Add a new event to the list.<br/>
	 * Runs a purge on the event list afterwards to make sure all old events are removed.
	 * @param event The new event to add
	 */
	public void addEvent(Event event) {
		eventList.add(event);
		purgeOldEntries();
		saveEvents();
	}
	
	/**
	 * Get an array of all events
	 * @return An array of events
	 */
	public Event[] getEvents() {
		return eventList.toArray(new Event[0]);
	}
	
	/**
	 * Purge old entries from the event list.<br/>
	 * This needs to check differently for each event type
	 */
	private void purgeOldEntries() {
		Iterator<Event> i = eventList.iterator();
		while (i.hasNext()) {
			Event e = i.next();
			if (e instanceof PurgableByAge && ((PurgableByAge)e).isOld()) {
				i.remove();
			}
		}
	}
	
	/**
	 * Create a host object for the host running this p300 instance
	 * This is actually a workaround because there is no Host object for the current host.
	 * Some code copied from {@link de.guruz.p300.threads.ListenThread#getKnownLocalURLs()}
	 * @return A Host object of this p300 host
	 */
	private Host getOwnHost() {
		String hash = de.guruz.p300.Configuration.instance().getUniqueHash();
		String name = de.guruz.p300.Configuration.instance().getLocalDisplayName();
		int port = MainDialog.getCurrentHTTPPort();
		Host h = new Host(hash);
		h.setDisplayName(name);
		try {
			Enumeration<NetworkInterface> nifs = java.net.NetworkInterface.getNetworkInterfaces();
			while (nifs.hasMoreElements()) {
				NetworkInterface nif = nifs.nextElement();
				Enumeration<InetAddress> ips = nif.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress current_ip = ips.nextElement();
					if (current_ip instanceof Inet4Address)
					{
						h.addIPAndPort(current_ip.getCanonicalHostName(), port, 0);
					}
				}
			}
		} catch (SocketException exception) {
			exception.printStackTrace();
		}
		return h;
	}
	
}