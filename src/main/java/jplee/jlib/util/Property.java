package jplee.jlib.util;

public class Property<T extends Object> {

	public final T object;
	public final String key;

	public Property(String key, T object) {
		this.object = object;
		this.key = key;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Property) {
			Property<?> prop = (Property<?>) obj;
			return prop.key.equals(key) && prop.object.equals(object);
		}
		return false;
	}

	@Override
	public String toString() {
		return key + "=" + (object != null ? object.toString() : "");
	}
}