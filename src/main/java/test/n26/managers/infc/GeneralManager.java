package test.n26.managers.infc;

import test.n26.components.CurrentStats;
import test.n26.components.TransactionRequest;

/**
 * 
 * @author Denys Nikolskyy
 *
 *         Interface for defining methods to work with model
 *
 */
public interface GeneralManager {

	/**
	 * Method to save transaction
	 * 
	 * @param trz
	 *            transaction request for transaction to store
	 * @return if transaction is not expired to show in statistics
	 */
	boolean saveTransaction(TransactionRequest trz);

	/**
	 * Method to get statistics for the last 60 seconds
	 * 
	 * @return Statistics for the given time
	 */
	CurrentStats getStats();

}
