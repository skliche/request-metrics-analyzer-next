package de.ibm.issw.requestmetrics.util;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Naive implementation of a StringPool that uses weak references for key and value
 * 
 * @author skliche
 *
 */
public class StringPool {
	private static final WeakHashMap<String, WeakReference<String>> pool = new WeakHashMap<String, WeakReference<String>>();
	
	/**
	 * Performs a lookup in the string pool for the supplied string. If the string
	 * is in the pool the reference is returned, otherwise the supplied string reference
	 * is added to the pool and returned as well. 
	 * @param str string to be replaced by a reference from the pool
	 * @return the reference from the pool
	 */
	public static String intern(final String str) {
		final WeakReference<String> cached = pool.get(str);
		if(cached != null){
			final String value = cached.get();
			if(value != null)
				return value;
		}
		pool.put(str, new WeakReference<String>(str));
		return str;
	}
}
