/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.accesscontrol;

import java.security.Permission;
import java.util.StringTokenizer;

public final class AdminPermission extends Permission {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1107518442777804495L;

	public static final String APP = "app";
	public static final String USER = "user";
	public static final String SYSTEM = "system";
	public static final String ALLACTIONS = "app,user,system";
	static final int CANONICAL_ACTIONS_LENGTH = ALLACTIONS.length();

	private static final int _APP = 1 << 0;
	private static final int _USER = 1 << 1;
	private static final int _SYSTEM = 1 << 2;
	public static final int _ALLACTIONS = _APP | _USER | _SYSTEM;
	public static final int _NOACTIONS = 0;

	private String actions;

	int actionsAsMask;
	private Object owner;
	private boolean wced;

	public AdminPermission(String actions) {
		super("*");
		try {
			parseActions((actions == null) ? "*" : actions);
			parseFilter("*");
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}

	public AdminPermission(String filter, String actions) {
		super((filter == null) ? filter = "*" : filter);
		if (actions == null) {
			actions = filter;
			filter = "*";
		}
		try {
			parseActions((actions == null) ? "*" : actions);
			parseFilter(filter);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}

	private int parseActions(String actStr) {
		// Helper variable to detect if the actions string starts with comma
		boolean comma = false;
		int bitMask = _NOACTIONS;
		if (actStr == null) {
			actions = "";
			return _NOACTIONS;
		}
		// Get the chars of actions into a char array and parse it by iteration
		// over the array elements
		char[] chArr = actStr.toCharArray();
		int index = chArr.length;
		if (index == 0) {
			actions = "";
			return _NOACTIONS;
		}
		// Begin at the last element of the array
		index--;
		while (index >= 0) {
			char tmp = chArr[index];
			// Skip all white spaces at the end of the string
			while ((index >= 0) && (tmp == ' ') && (tmp == '\t') && (tmp == '\n') && (tmp == '\r') && (tmp == '\f')) {
				index--;
				tmp = chArr[index];
			}
			// scan actions strings for expected values
			int charsToSkip;
			// check for APP/app
			if (index >= 2 && ((chArr[index - 2] == 'a') || (chArr[index - 2] == 'A'))
					&& ((chArr[index - 1] == 'p') || (chArr[index - 1] == 'P'))
					&& ((chArr[index] == 'p') || (chArr[index] == 'P'))) {
				charsToSkip = 3;
				bitMask |= _APP;
			}
			// check for USER/user
			else if (index >= 3 && ((chArr[index - 3] == 'u') || (chArr[index - 3] == 'U'))
					&& ((chArr[index - 2] == 's') || (chArr[index - 2] == 'S'))
					&& ((chArr[index - 1] == 'e') || (chArr[index - 1] == 'E'))
					&& ((chArr[index] == 'r') || (chArr[index] == 'R'))) {
				charsToSkip = 4;
				bitMask |= _USER;
			}
			// check for SYSTEM/system
			else if (index >= 5 && ((chArr[index - 5] == 's') || (chArr[index - 5] == 'S'))
					&& ((chArr[index - 4] == 'y') || (chArr[index - 4] == 'Y'))
					&& ((chArr[index - 3] == 's') || (chArr[index - 3] == 'S'))
					&& ((chArr[index - 2] == 't') || (chArr[index - 2] == 'T'))
					&& ((chArr[index - 1] == 'e') || (chArr[index - 1] == 'E'))
					&& ((chArr[index] == 'm') || (chArr[index] == 'M'))) {
				charsToSkip = 6;
				bitMask |= _SYSTEM;
			}
			// check for wildcard
			else if (index >= 0 && ((chArr[index] == '*'))) {
				charsToSkip = 1;
				bitMask |= _ALLACTIONS;
			}
			else
				throw new IllegalArgumentException(actStr);

			// Now skip all white spaces up to the comma
			comma = false;
			while ((index >= charsToSkip && !comma)) {
				switch (chArr[index - charsToSkip]) {
				case ',':
					comma = true;
				case ' ':
				case '\t':
				case '\n':
				case '\r':
				case '\f':
					break;
				default:
					throw new IllegalArgumentException(actStr);
				}
				index--;
			}
			index -= charsToSkip;
		}
		if (comma)
			throw new IllegalArgumentException("actions string start with comma: " + actStr);

		actionsAsMask = bitMask;
		// Build actions string
		if (bitMask == _ALLACTIONS) {
			actions = ALLACTIONS;
		}
		else {
			StringBuilder sb = new StringBuilder(CANONICAL_ACTIONS_LENGTH);
			if ((actionsAsMask & _APP) != 0) {
				sb.append(APP);
			}
			if ((actionsAsMask & _USER) != 0) {
				if (sb.length() > 0)
					sb.append(',');
				sb.append(USER);
			}
			if ((actionsAsMask & _SYSTEM) != 0) {
				if (sb.length() > 0)
					sb.append(',');
				sb.append(SYSTEM);
			}
			actions = sb.toString();
		}
		return bitMask;
	}

	private void parseFilter(String filter) {
		/*
		 * Check is the filter consists a wildcard, that would mean unrestricted resource permissions
		 */
		if (filter.equals("*")) {
			this.wced = true;
			this.owner = null;
			return;
		}
		else if (filter.indexOf('=') == -1)
			throw new IllegalArgumentException("Invalid filter string: " + filter);
		// to get the tokens (path.., type.., count.., recursive..)
		StringTokenizer st1 = new StringTokenizer(filter, ",");
		while (st1.hasMoreTokens()) {
			String token = st1.nextToken();
			/* to get the keys */
			StringTokenizer st2 = new StringTokenizer(token, "=");
			String key = st2.nextToken();
			String value = st2.nextToken();
			if (key == null || value == null)
				throw new IllegalArgumentException("Invalid filter string: " + filter);
			key = key.trim();
			value = value.trim();
			/* do the action */
			switch (key) {
			case "owner":
				this.owner = value;
				break;
			default:
				throw new IllegalArgumentException("invalid filter string" + filter);
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof AdminPermission))
			return false;
		AdminPermission perm = (AdminPermission) obj;
		if ((actionsAsMask == perm.actionsAsMask)
				&& ((owner == null && perm.owner == null) || (owner != null && perm.owner != null && owner
						.equals(perm.owner))) && wced == perm.wced)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 31 * 17 + getName().hashCode();
		hash = (hash << 5) - hash + actions.hashCode();
		return hash;
	}

	@Override
	public String getActions() {
		return actions;
	}

	@Override
	public boolean implies(Permission p) {
		if (!(p instanceof AdminPermission))
			return false;
		AdminPermission qp = (AdminPermission) p;
		/*
		 * Check the owner: - If this.owner >= qp.owner -> ok otherwise fail
		 */
		if (((this.owner != null) && (qp.owner == null))
				|| ((this.owner != null) && (qp.owner != null) && (this.owner != qp.owner)))
			return false;
		/*
		 * Check the actions
		 */
		int queriedActions = qp.actionsAsMask;
		if ((queriedActions & this.actionsAsMask) != queriedActions) {
			return false;
		}

		/*
		 * Check the path
		 */

		return true;
	}
}
