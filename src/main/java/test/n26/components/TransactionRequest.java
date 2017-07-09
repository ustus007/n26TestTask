package test.n26.components;

import java.math.BigDecimal;

/**
 * 
 * @author Denys Nikolskyy
 * 
 * POJO class for receiving transaction info with json parsing
 *
 */
public class TransactionRequest {

	private BigDecimal amount;
	private Long timestamp;
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
}
