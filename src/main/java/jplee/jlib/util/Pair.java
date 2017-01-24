package jplee.jlib.util;

public class Pair<J extends Object, K extends Object> implements Comparable<Pair<J,K>> {
	J key;
	K item;
	
	public Pair(J key, K item) {
		this.key = key;
		this.item = item;
	}
	
	public J getKey() {
		return key;
	}
	
	public K getItem() {
		return item;
	}
	
	public void setKey(J key) {
		this.key = key;
	}
	
	public void setItem(K item) {
		this.item = item;
	}

	@Override
	public int hashCode() {
		return key.hashCode() * 31 + item.hashCode();
	}
	
	@Override
	public int compareTo(Pair<J,K> arg0) {
		return Math.min(1, Math.max(-1, arg0.hashCode() - this.hashCode()));
	}
}
