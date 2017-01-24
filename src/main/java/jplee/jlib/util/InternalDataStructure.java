package jplee.jlib.util;

import java.util.Map;

import com.google.common.collect.Maps;

public class InternalDataStructure<T extends Object> {
	
	protected final Map<String,Property<T>> data;
	
	protected InternalDataStructure() {
		this.data = Maps.newHashMap();
	}
	
	public Object get(String key) {
		if(hasData(key))
			return data.get(key).object;
		return null;
	}
	
	public int getInt(String key) {
		return (Integer) get(key);
	}
	
	public float getFloat(String key) {
		return (Float) get(key);
	}

	public double getDouble(String key) {
		return (Double) get(key);
	}
	
	public long getLong(String key) {
		return (Long) get(key);
	}
	
	public boolean getBoolean(String key) {
		return (Boolean) get(key);
	}
	
	public String getString(String key) {
		return (String) get(key);
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
