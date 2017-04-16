package jplee.worldmanager.manager;

import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class ManagerUtil {

	public static final int ANY_DIMENSION = -93578231;

	public static <M extends Object,K extends Object,J extends Object> boolean addToMap(Map<M,Map<K,J>> map, M key1, K key2,
		J value) {
		Map<K,J> innerMap = null;
		if((innerMap = map.get(key1)) == null) {
			map.put(key1, Maps.<K,J>newHashMap());
			innerMap = map.get(key1);
		}
		return innerMap.put(key2, value) != null;
	}

	public static <M extends Object,K extends Object,J extends Object> boolean addToMultimap(Map<M,Multimap<K,J>> map, M key1,
		K key2, J value) {
		Multimap<K,J> innerMap = null;
		if((innerMap = map.get(key1)) == null) {
			map.put(key1, HashMultimap.<K,J>create());
			innerMap = map.get(key1);
		}
		return innerMap.put(key2, value);
	}

}
