package ch.ethz.ruediste.roofline.dom;

public class HashBase {
	protected String value;

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		HashBase other = (HashBase) obj;
		if (value == null) {
			return other.value == null;
		}
		return value.equals(other.value);
	}

	@Override
	public int hashCode() {
		if (value == null) {
			return 0;
		}
		return value.hashCode();
	}
}