package test.n26.data.storage.infc;

import java.math.BigDecimal;

/**
 * 
 * @author Denys Nikolskyy
 * 
 *         Class for emulation of in-memory database
 *
 */
public interface InMemory {

	/**
	 * Method for creating and saving transaction record
	 * 
	 * @param amount
	 *            amount, transfered with given transaction
	 * @param epoch
	 *            transaction time in epoch in millis
	 * @param addToStats
	 *            if transaction is not already expired (and flag for action of
	 *            including it to statistics)
	 */
	void saveTransaction(BigDecimal amount, long epoch, boolean addToStats);

}
