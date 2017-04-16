package jplee.jlib.util;

import java.util.HashMap;
import java.util.Map;

public class InternalDataStructure<T extends Object> {
	
	protected final Map<String,Property<T>> data;
	
	protected InternalDataStructure() {
		this.data = new HashMap<>();
	}
	
	public Object get(String key) {
		if(hasData(key))
			return data.get(key).object;
		return null;
	}
	
	public int getInt(String key) {
		Object obj = get(key);
		if(obj instanceof Integer) {
			return (Integer) obj;
		}
		if(obj.getClass().equals(int.class)) {
			return (int) obj;
		}
		return Integer.MIN_VALUE;
	}
	
	public float getFloat(String key) {
		Object obj = get(key);
		if(obj instanceof Float) {
			return (Float) obj;
		}
		if(obj.getClass().equals(float.class)) {
			return (float) obj;
		}
		return Float.MIN_VALUE;
	}

	public double getDouble(String key) {
		Object obj = get(key);
		if(obj instanceof Double) {
			return (Double) obj;
		}
		if(obj.getClass().equals(double.class)) {
			return (double) obj;
		}
		return Double.MIN_VALUE;
	}
	
	public long getLong(String key) {
		Object obj = get(key);
		if(obj instanceof Long) {
			return (Long) obj;
		}
		if(obj.getClass().equals(long.class)) {
			return (long) obj;
		}
		return Long.MIN_VALUE;
	}
	
	public boolean getBoolean(String key) {
		Object obj = get(key);
		if(obj instanceof Boolean) {
			return (Boolean) obj;
		}
		if(obj.getClass().equals(boolean.class)) {
			return (boolean) obj;
		}
		return Boolean.FALSE;
	}
	
	public String getString(String key) {
		String str = (String)get(key);
		if(str == null)
			return "";
		return str;
	}
	
	public boolean hasData(String key) {
		return data.containsKey(key);
	}

	protected void set(String key, T data) {
		this.set(new Property<T>(key, data));
	}
	
	protected void set(Property<T> prop) {
		this.data.put(prop.key, prop);
	}
	
	@Override
	public String toString() {
		String to = "Data{";
		boolean first = true;
		for(Property<T> da : data.values()) {
			to += (first ? "" : ",") + da.toString();
			if(first)
				first = !first;
		}
		return to + "}";
	}
}
