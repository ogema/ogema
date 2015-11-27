package org.ogema.accesscontrol;

import java.util.StringTokenizer;

public class FilterValue {
	/*
	 * This value is used when queried permissions created
	 */
	String[] values;
	boolean[] wcs;

	public void parse(String param) {
		StringTokenizer st = new StringTokenizer(param, " ");
		int len = st.countTokens();
		values = new String[len];

		wcs = new boolean[len];

		int i = 0;
		while (st.hasMoreTokens()) {
			String value = st.nextToken().trim();
			len = value.length();
			int wcindex = value.indexOf('*');
			// Case 3 : path is not wildcarded
			if (wcindex == -1) {
				values[i] = value;
				wcs[i] = false;
				i++;
				continue;
			}
			// Case 2 : path ends with a wildcard
			else if (wcindex == len - 1) {
				if (len > 1)
					values[i] = value.substring(0, len - 1);
				else
					values[i] = "*";
				wcs[i] = true;
				i++;
				continue;
			}
			else {
				RuntimeException e = new IllegalArgumentException("Invalid filter string: " + param);
				e.printStackTrace();
				throw e;
			}

		}
	}

	/* @formatter:off */
	/*
	 * case | granted	| query 	|									|
	 * 		| path type	| path type	| 			implies					| example
	 * ===================================================================================================
	 * 1    | 		1	| 	1	 	| 			true					| 
	 * _____|___________|___________|___________________________________|___________|____________
	 * 2    | 		1	| 	2	 	| 			true					|
	 * _____|___________|___________|___________________________________|___________|____________
	 * 3    | 		1	|  	3	 	| 			true					|   
	 * _____|___________|___________|___________________________________|___________|____________
	 * 4    |  		2	| 	1		| 			false					|	
	 * _____|___________|___________|___________________________________|___________|____________
	 * 5    |  		2	| 	2 		| queryPath.startswith(grantedPath)	|			|
	 * _____|___________|___________|___________________________________|___________|____________
	 * 6    |  		2	|  	3 		| queryPath.startswith(grantedPath)	|			|
	 * _____|___________|___________|___________________________________|___________|____________
	 * 7    |  		3	|  	1		| 			false					|			|
	 * _____|___________|___________|___________________________________|___________|____________
	 * 8    |  		3	|  	2		| 			false					|			|
	 * _____|___________|___________|___________________________________|___________|____________
	 * 9    |  		3	|  	3 		| queryPath.equals(grantedPath)		|			|
	 * 
	 * True condition is (case 1 || case 2 || case 3 || case 5 || case 6 || case 9)
	 * 
	 * Here we need the false condition as break condition which is
	 *  (! case 1 && ! case 2 && ! case 3 && ! case 5 && ! case 6 && ! case 9)
	 *  
	 */
	/* @formatter:on */

	public boolean implies(FilterValue req) {
		boolean wcOnly = Util.contains(values, "*");
		int reqIndex = 0;
		// case 1-3
		if (wcOnly)
			return true;
		for (String str : req.values) {
			int target = Util.startsWithAny(values, str);
			if (target == -1)
				return false;
			// case 5
			if (!wcs[target] || !req.wcs[reqIndex]
					|| (values[target] != null && str != null && !str.startsWith(values[target])))
				// case 6
				if (!wcs[target] || req.wcs[reqIndex]
						|| (values[target] != null && str != null && !str.startsWith(values[target])))
					// case 9
					if (wcs[target] || req.wcs[reqIndex]
							|| (values[target] != null && str != null && !str.startsWith(values[target])))
						return false;

		}
		return true;
	}
}
