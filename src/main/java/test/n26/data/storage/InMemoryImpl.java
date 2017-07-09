package test.n26.data.storage;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import test.n26.data.storage.infc.InMemory;
import test.n26.data.storage.infc.Statistics;

/**
 * 
 * @author Denys Nikolskyy
 * 
 *         Implementation class for emulating in-memoty database
 *
 */
@Component
public class InMemoryImpl implements InMemory {

	@Autowired
	private Statistics stats;

	/**
	 * Thread safe storage for transactions
	 */
	// Could have used LinkedList for O(1) adding time, but it's not good for
	// emulating database behavior and I'm using similat adding time difficulty
	// solutions when calculating statistics
	private Map<String, Transaction> storage = Collections.synchronizedSortedMap(new TreeMap<String, Transaction>());

	/**
	 * @see test.n26.data.storage.infc.InMemory#saveTransaction(BigDecimal, long, boolean)
	 */
	@Override
	public void saveTransaction(BigDecimal amount, long epoch, boolean addToStats) {
		Transaction t = new Transaction(UUID.randomUUID().toString(), amount, epoch);
		storage.put(t.getUuid(), t);

		if (addToStats) {
			stats.saveStats(t);

		}
	}

	// etc., like readTransaction.
}
