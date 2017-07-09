package test.n26.data.storage;

import java.math.BigDecimal;

/**
 * 
 * @author Denys Nikolskyy
 * 
 *         POJO class for saving transactions and calculating it's statistics
 *
 */
public class Transaction {

	private String uuid;
	private BigDecimal amount;
	private Long epoch;

	public Transaction() {
	}

	public Transaction(String uuid, BigDecimal amount, Long epoch) {
		super();
		this.uuid = uuid;
		this.amount = amount;
		this.epoch = epoch;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Long getEpoch() {
		return epoch;
	}

	public void setEpoch(Long epoch) {
		this.epoch = epoch;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

}
