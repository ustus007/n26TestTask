package test.n26.data.storage.infc;

import test.n26.components.CurrentStats;
import test.n26.data.storage.Transaction;

/**
 * 
 * @author Denys Nikolskyy
 * 
 *         Interface for defining methods for transaction-statistics
 *         relationship
 *
 */
public interface Statistics {

	/**
	 * Time before transaction is expired
	 */
	public static final long LIFETIME = 60 * 1000L;

	/**
	 * Method to update statistics with given transaction and schedule it's
	 * removal from statistics
	 * 
	 * @param t
	 *            transaction to be included into statistics
	 */
	void saveStats(Transaction t);

	/**
	 * Method to get current statistics
	 * 
	 * @return statistics, gathered and calculated for current moment
	 */
	CurrentStats getStats();

}
