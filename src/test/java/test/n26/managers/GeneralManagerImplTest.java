package test.n26.managers;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import test.n26.application.Application;
import test.n26.components.CurrentStats;
import test.n26.components.TransactionRequest;
import test.n26.managers.infc.GeneralManager;

/**
 * 
 * @author Denys Nikolskyy
 * 
 *         Integration test case for model part of the appliacation.
 * 
 *
 */
// Only Integration testing - did not see the point in unit testing with code
// organisation I have
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class GeneralManagerImplTest {

	// Class that's being tested
	@Autowired
	private GeneralManager generalManager;

	private Random random = new Random();

	/**
	 * Generator of random values, used for transactions amount generation
	 * 
	 * @return radnom double value, from 0 to 100
	 */
	private double getRandomAmount() {
		return random.nextDouble() * 100;
	}

	/**
	 * Integration test that runs through all points of the application's model
	 */
	@Test
	public void testAllProcess() {
		LocalDateTime now = LocalDateTime.now();// basic transaction timestamp
		LocalDateTime late = now.minusHours(1);// timestamp for transactions,
												// older than allowed for
												// statistics
		LocalDateTime part2 = now.plusSeconds(30);// Timestamp for second part
													// of the transactions, used
													// to check removal of
													// expired transactions from
													// actual statistics
		Double max = Double.MIN_VALUE;
		Double min = Double.MAX_VALUE;
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < 100; i++) {// generation of basic transactions
			Double amount = getRandomAmount();
			max = amount > max ? amount : max;
			min = amount < min ? amount : min;
			sum = sum.add(BigDecimal.valueOf(amount));
			Long epoch = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			TransactionRequest trz = new TransactionRequest();
			trz.setAmount(BigDecimal.valueOf(amount));
			trz.setTimestamp(epoch);
			boolean res = generalManager.saveTransaction(trz);
			assertTrue(res);
		}
		System.out.println("Sleeping for 20 seconds");// Because transactions
														// are saved
														// asynchroniously,
														// waiting to make sure
														// all of them processed
		try {
			Thread.sleep(20 * 1000);
		} catch (InterruptedException e) {
			fail();
		}
		CurrentStats stats = generalManager.getStats(); // getting and checking
														// all th statistics
														// information
		System.out.println("Records in statistics: " + stats.getCount());
		assertTrue(stats.getCount() == 100);
		System.out.println("Sum values: " + sum.doubleValue() + " " + stats.getSum());
		assertTrue(stats.getSum() == sum.doubleValue());
		BigDecimal average = sum.divide(BigDecimal.valueOf(stats.getCount()));
		System.out.println("Average values: " + average.doubleValue() + " " + stats.getAvg());
		assertTrue(average.doubleValue() == BigDecimal.valueOf(stats.getAvg()).doubleValue());
		System.out.println("Max values: " + stats.getMax() + " " + max);
		assertTrue(BigDecimal.valueOf(stats.getMax()).doubleValue() == BigDecimal.valueOf(max).doubleValue());
		System.out.println("Min values: " + stats.getMin() + " " + min);
		assertTrue(BigDecimal.valueOf(stats.getMin()).doubleValue() == BigDecimal.valueOf(min).doubleValue());
		for (int i = 0; i < 100; i++) { // generating expired transactions
			Double amount = getRandomAmount();
			Long epoch = late.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			TransactionRequest trz = new TransactionRequest();
			trz.setAmount(BigDecimal.valueOf(amount));
			trz.setTimestamp(epoch);
			boolean res = generalManager.saveTransaction(trz);
			assertFalse(res);
		}
		System.out.println("Sleeping for 10 seconds");
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			fail();
		}
		stats = generalManager.getStats(); // Checking that expired transactions
											// are not added to statistics
		System.out.println("Records in statistics: " + stats.getCount());
		assertTrue(stats.getCount() == 100);

		BigDecimal sumSecond = BigDecimal.ZERO;
		Double maxSecond = Double.MIN_VALUE;
		Double minSecond = Double.MAX_VALUE;

		for (int i = 0; i < 100; i++) { // generating second set of transactions
			Double amount = getRandomAmount();
			max = amount > max ? amount : max;
			min = amount < min ? amount : min;
			sum = sum.add(BigDecimal.valueOf(amount));
			maxSecond = amount > maxSecond ? amount : maxSecond;
			minSecond = amount < minSecond ? amount : minSecond;
			sumSecond = sumSecond.add(BigDecimal.valueOf(amount));
			Long epoch = part2.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			TransactionRequest trz = new TransactionRequest();
			trz.setAmount(BigDecimal.valueOf(amount));
			trz.setTimestamp(epoch);
			boolean res = generalManager.saveTransaction(trz);
			assertTrue(res);
		}
		System.out.println("Sleeping for 10 seconds");
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			fail();
		}
		stats = generalManager.getStats(); // Checking for statistics to include
											// complete information from two
											// sets
		System.out.println("Records in statistics: " + stats.getCount());
		assertTrue(stats.getCount() == 200);
		System.out.println("Sum values: " + sum.doubleValue() + " " + stats.getSum());
		assertTrue(stats.getSum() == sum.doubleValue());
		average = sum.divide(BigDecimal.valueOf(stats.getCount()));
		System.out.println("Average values: " + average.doubleValue() + " " + stats.getAvg());
		assertTrue(average.doubleValue() == BigDecimal.valueOf(stats.getAvg()).doubleValue());
		System.out.println("Max values: " + stats.getMax() + " " + max);
		assertTrue(BigDecimal.valueOf(stats.getMax()).doubleValue() == BigDecimal.valueOf(max).doubleValue());
		System.out.println("Min values: " + stats.getMin() + " " + min);
		assertTrue(BigDecimal.valueOf(stats.getMin()).doubleValue() == BigDecimal.valueOf(min).doubleValue());
		System.out.println("Sleeping for 30 seconds");
		try { // waiting for first set of transactions to expire
			Thread.sleep(30 * 1000);
		} catch (InterruptedException e) {
			fail();
		}
		stats = generalManager.getStats(); // Checking the statistics to fit
											// only second set of transactions
		System.out.println("Records in statistics: " + stats.getCount());
		assertTrue(stats.getCount() == 100);
		System.out.println("Sum values: " + sumSecond.doubleValue() + " " + stats.getSum());
		assertTrue(stats.getSum() == sumSecond.doubleValue());
		average = sumSecond.divide(BigDecimal.valueOf(stats.getCount()));
		System.out.println("Average values: " + average.doubleValue() + " " + stats.getAvg());
		assertTrue(average.doubleValue() == BigDecimal.valueOf(stats.getAvg()).doubleValue());
		System.out.println("Max values: " + stats.getMax() + " " + maxSecond);
		assertTrue(BigDecimal.valueOf(stats.getMax()).doubleValue() == BigDecimal.valueOf(maxSecond).doubleValue());
		System.out.println("Min values: " + stats.getMin() + " " + minSecond);
		assertTrue(BigDecimal.valueOf(stats.getMin()).doubleValue() == BigDecimal.valueOf(minSecond).doubleValue());
		System.out.println("Sleeping for 30 seconds");
		try { // waiting for all transactions to expire.
			Thread.sleep(30 * 1000);
		} catch (InterruptedException e) {
			fail();
		}
		stats = generalManager.getStats(); // Checking statistics to be empty
		System.out.println("Records in statistics: " + stats.getCount());
		assertTrue(stats.getCount() == 0);
		assertTrue(stats.getSum() == 0);
		assertTrue(stats.getAvg() == 0);
		assertTrue(stats.getMax() == 0);
		assertTrue(stats.getMin() == 0);
	}

}
