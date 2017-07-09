package test.n26.managers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import test.n26.components.CurrentStats;
import test.n26.components.TransactionRequest;
import test.n26.data.storage.infc.InMemory;
import test.n26.data.storage.infc.Statistics;
import test.n26.managers.infc.GeneralManager;

/**
 * 
 * @author Denys Nikolskyy
 * 
 *         Implementation class for operations with transactions and statistics
 *
 */
@Service
public class GeneralManagerImpl implements GeneralManager {

	@Autowired
	private Executor executor;

	@Autowired
	private InMemory storage;

	@Autowired
	private Statistics stats;

	/**
	 * @see test.n26.managers.infc.GeneralManager#saveTransaction()
	 */
	@Override
	public boolean saveTransaction(TransactionRequest trz) {
		long epoch = trz.getTimestamp();
		BigDecimal amount = trz.getAmount();
		boolean result;
		if ((new Date()).getTime() - (Statistics.LIFETIME) > epoch) {
			result = false;
		} else {
			result = true;
		}
		// At the end of requirements, there is such a paragraph:
		// "Endpoints have to execute in constant time and memory (O(1))"
		// That means, not only statistics gathering should be O(1), but
		// saving of transaction also. With the way I chose to save the
		// transaction and gather the statistics, only way to make adding
		// transaction a O(1) I found is to put it into separate thread and
		// return appropriate data - this time, if it is expired for statistics
		// - in the original
		executor.execute(() -> {
			storage.saveTransaction(amount, epoch, result);
		});

		return result;
	}

	/**
	 * @see test.n26.managers.infc.GeneralManager#getStats()
	 */
	@Override
	public CurrentStats getStats() {
		return stats.getStats();
	}

}
