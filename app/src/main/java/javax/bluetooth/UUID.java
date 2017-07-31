package javax.bluetooth;

public class UUID {
	public UUID(long uuidValue) {
		if (uuidValue < 0 || uuidValue > 0xffffffffl) {
			throw new IllegalArgumentException("uuidValue is not in the range [0, 2^32 -1]");
		}
	}

	public UUID(String uuidValue, boolean shortUUID) {
		if (uuidValue == null) {
			throw new NullPointerException("uuidValue is null");
		}
	}

	public String toString() {
		return null;
	}

	public boolean equals(Object value) {
		if (value == null || !(value instanceof UUID)) {
			return false;
		}
		return false;
	}

	public int hashCode() {
		return 0;
	}
}
