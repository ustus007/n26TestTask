package test.n26.data.storage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import test.n26.components.CurrentStats;
import test.n26.data.storage.infc.Statistics;

/**
 * 
 * @author Denys Nikolskyy
 * 
 *         Class to operate statistics information. Main task logic is
 *         implemented here
 *
 */
/*
 * General explanation of approach:
 * 
 * We have to search for minnimum and maximum on every statistics re-calculation
 * among all transaction values that did not expire, so it's impossible to make
 * it O(1) if statistics is calculated on request. So I'm calculating most of it
 * on including and expiring of the transaction in the separate thread. That's
 * making the request time O(1).
 * 
 * It's hard for me to figure the storage O. I'm using one record for each
 * transaction in two sets, and I'm scheduling it's expiration - all of this is
 * giving O(n) on storage. But I'm getting O(n) on storage with storing
 * transactions in memory anyway (and tha's cannot be reduced), so there is no
 * increase in O.
 * 
 */
@Component
public class StatisticsImpl implements Statistics {

	@Autowired
	private TaskScheduler taskScheduler;

	/**
	 * Lock, using to separate updating and reading the statistics
	 */
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	// Storage for statistics information that is not counted on request

	private Long count = 0L;
	// Using BigDecimal to avoid errors that are happening when counting with
	// double values
	private BigDecimal sum = BigDecimal.ZERO;
	private Double min = Double.MAX_VALUE;
	private Double max = Double.MIN_VALUE;

	/**
	 * Set for storing transactions, minimal amounts first
	 */
	private SortedSet<Transaction> mins = new TreeSet<Transaction>((x, y) -> {
		return x.getAmount().compareTo(y.getAmount());
	});
	/**
	 * Set for storing transactions, maximal amounts first
	 */
	private SortedSet<Transaction> maxs = new TreeSet<Transaction>((x, y) -> {
		return y.getAmount().compareTo(x.getAmount());
	});

	/**
	 * Method for counting minimal value and preparing for re-counting on
	 * transaction expiration
	 * 
	 * @param t
	 *            Transaction to include
	 * @param value
	 *            Current minimum value
	 * @param candidate
	 *            Transaction value to consider
	 * @return new minimum value
	 */
	private double manageMin(Transaction t, double value, double candidate) {
		double prevMin = value;
		double c = candidate;
		if (c <= prevMin) {
			prevMin = c;
		}
		mins.add(t); // We need to include all the transactions, because, on
						// removal of value, we can't be sure it won't be new
						// minimum
		return prevMin;
	}

	/**
	 * Method for counting maximal value and preparing for re-counting on
	 * transaction expiration
	 * 
	 * @param t
	 *            Transaction to include
	 * @param value
	 *            Current maximum value
	 * @param candidate
	 *            Transaction value to consider
	 * @return new maximum value
	 */
	private double manageMax(Transaction t, double value, double candidate) {
		double prevMax = value;
		double c = candidate;
		if (c >= prevMax) {
			prevMax = c;
		}
		maxs.add(t); // We need to include all the transactions, because, on
						// removal of value, we can't be sure it won't be new
						// maximum
		return prevMax;
	}

	/**
	 * @see test.n26.data.storage.infc.Statistics#saveStats(Transaction)
	 */
	@Override
	public void saveStats(Transaction t) {
		lock.writeLock().lock();
		try {
			sum = sum.add(t.getAmount());
			count++;
			min = manageMin(t, min, t.getAmount().doubleValue());
			max = manageMax(t, max, t.getAmount().doubleValue());

			Instant ti = Instant.ofEpochMilli(t.getEpoch() + LIFETIME);
			taskScheduler.schedule(() -> {// Scheduling the removal of the
											// expired transacion from
											// statistics
				lock.writeLock().lock();
				try {
					sum = sum.subtract(t.getAmount());
					count--;
					if (t.getAmount().equals(BigDecimal.valueOf(min))) {
						mins.remove(t);
						if (mins.size() > 0) {
							min = mins.first().getAmount().doubleValue();// Easy
																			// counting
																			// of
																			// the
																			// new
																			// minimum
																			// -
																			// only
																			// reason
																			// for
																			// using
																			// SortedSet
																			// for
																			// storage;
						} else {
							min = Double.MAX_VALUE;
						}
					} else {
						mins.remove(t);
					}
					if (t.getAmount().equals(BigDecimal.valueOf(max))) {
						maxs.remove(t);
						if (maxs.size() > 0) {
							max = maxs.first().getAmount().doubleValue();// Easy
																			// counting
																			// of
																			// the
																			// new
																			// maximum
																			// -
																			// only
																			// reason
																			// for
																			// using
																			// SortedSet
																			// for
																			// storage;
						} else {
							max = Double.MIN_VALUE;
						}
					} else {
						maxs.remove(t);
					}
				} finally {
					lock.writeLock().unlock();
				}
			}, Date.from(ti));

		} finally {
			lock.writeLock().unlock();
		}

	}

	/**
	 * @see test.n26.data.storage.infc.Statistics#getStats()
	 */
	@Override
	public CurrentStats getStats() {
		lock.readLock().lock();
		try {
			CurrentStats result = new CurrentStats();
			result.setCount(count);
			result.setSum(sum.doubleValue());
			Double avg = sum.equals(BigDecimal.ZERO) ? 0
					: (count == 0 ? 0 : sum.divide(BigDecimal.valueOf(count)).doubleValue());
			result.setAvg(avg);
			result.setMin(min == Double.MAX_VALUE ? 0 : min);
			result.setMax(max == Double.MIN_VALUE ? 0 : max);
			return result;
		} finally {
			lock.readLock().unlock();
		}

	}

}
