package org.jdiscript.util;

import java.util.Collection;
import java.util.Iterator;

public class Utils {
	
	public static String join(Collection<String> col, String sep) {
		if(col.isEmpty()) {
			return "";
		}
		Iterator<String> iter = col.iterator();
		String first = iter.next();
		if(!iter.hasNext()) {
			return first;
		}
		
		final StringBuilder out = new StringBuilder(first);
		while(iter.hasNext()) {
			out.append(sep);
			out.append(iter.next());
		}
		return out.toString();
	}

}
