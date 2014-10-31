/**
 * 
 */
package de.guruz.p300.eventstream;

/**
 * Events implementing this interface are purgable by age, i.e. they know by their timestamp if they are old.
 * @author tomcat
 *
 */
public interface PurgableByAge {

	/**
	 * Check if the event is too old and should be purged
	 * @return True if the event is old; False otherwise
	 */
	public boolean isOld();
	
}
