package jplee.worldmanager.util;

public class Pair<J extends Object, K extends Object> {
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
}
