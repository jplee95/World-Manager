package jplee.jlib.util;

public class DataStructure<T extends Object> extends InternalDataStructure<T> {

	public DataStructure() {
		super();
	}

	@Override
	public void set(String key, T data) {
		super.set(key, data);
	}

	@Override
	public void set(Property<T> prop) {
		super.set(prop);
	}
	
}
