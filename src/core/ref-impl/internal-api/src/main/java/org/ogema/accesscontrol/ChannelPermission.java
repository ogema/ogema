/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
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
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Permission to protect Channels that an application can obtain from OGEMA. The path information which is given as
 * argument of the constructors has the following format: "busTypeString;busIdString;addressesString;registersString".
 * As an example a ChannelPermission for a set of modbus devices can be created with the path string:
 * "modbus;*;1,2,3;11,12,13". The permission doesn't interpret the content of the path information in any way, so the
 * parts devAddrs and chAddrs within the string are to be set in dependence of the specified bus type.
 * 
 */
/**
 * @author Zekeriya Mansuroglu
 *
 */
public final class ChannelPermission extends Permission {

	FilterValue busId;
	FilterValue devAddrs;
	FilterValue chAddrs;

	private String actions;

	/**
	 * 
	 */
	private static final long serialVersionUID = 3111187926040156090L;

	public static final String READ = "read";
	public static final String WRITE = "write";
	public static final String DELETE = "delete";
	public static final String ALLACTIONS = "read,write,delete";
	static final int CANONICAL_ACTIONS_LENGTH = ALLACTIONS.length();

	/**
	 * Get action.
	 */
	public final static int _READ = 1 << 0;
	/**
	 * Set action.
	 */
	public final static int _WRITE = 1 << 1;
	public final static int _DELETE = 1 << 2;
	/**
	 * All actions (get,set)
	 */
	public final static int _ALLACTIONS = _READ | _WRITE | _DELETE;
	// the actions mask
	private transient int mask;

	private static final String WILD_STRING = "*";

	public ChannelPermission() {
		this(null, null);
	}

	public ChannelPermission(String path) {
		this(path, null);
	}

	public ChannelPermission(String path, String actions) {
		super((path == null) ? path = "*" : path);
		try {
			parseFilter(path);
			parseActions((actions == null) ? "*" : actions);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}

	public ChannelPermission(String busId, String address, String params, int actions) {
		super("busid=" + busId + ",devaddr=" + address + ",chaddr=" + params);
		this.busId = new FilterValue();
		this.busId.parse(busId);
		this.devAddrs = new FilterValue();
		this.devAddrs.parse(address);
		this.chAddrs = new FilterValue();
		this.chAddrs.parse(params);
		mask = actions & _ALLACTIONS;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof ChannelPermission))
			return false;

		ChannelPermission that = (ChannelPermission) obj;

		if (this.mask != that.mask)
			return false;
		if (!Arrays.equals(this.busId.values, that.busId.values) || !Arrays.equals(this.busId.wcs, that.busId.wcs))
			return false;
		if (!Arrays.equals(this.devAddrs.values, that.devAddrs.values)
				|| !Arrays.equals(this.devAddrs.wcs, that.devAddrs.wcs))
			return false;
		if (!Arrays.equals(this.chAddrs.values, that.chAddrs.values)
				|| !Arrays.equals(this.chAddrs.wcs, that.chAddrs.wcs))
			return false;
		return true;
	}

	private String createActions() {
		StringBuilder sb = new StringBuilder();
		boolean comma = false;

		if ((mask & _READ) == _READ) {
			comma = true;
			sb.append("read");
		}

		if ((mask & _WRITE) == _WRITE) {
			if (comma)
				sb.append(',');
			else
				comma = true;
			sb.append("write");
		}

		if ((mask & _DELETE) == _DELETE) {
			if (comma)
				sb.append(',');
			else
				comma = true;
			sb.append("delete");
		}

		return sb.toString();
	}

	@Override
	public String getActions() {
		if (actions == null)
			actions = createActions();
		return actions;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	private void parseFilter(String filter) {
		/*
		 * Check if the filter consists of a wildcard, that would mean unrestricted channel permissions
		 */
		if (filter.equals("*")) {
			this.busId = new FilterValue();
			this.busId.parse("*");
			this.devAddrs = new FilterValue();
			this.devAddrs.parse("*");
			this.chAddrs = new FilterValue();
			this.chAddrs.parse("*");
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
			case "busid":
				this.busId = new FilterValue();
				this.busId.parse(value);
				break;
			case "devaddr":
				this.devAddrs = new FilterValue();
				this.devAddrs.parse(value);
				break;
			case "chaddr":
				this.chAddrs = new FilterValue();
				this.chAddrs.parse(value);
				break;
			default:
				throw new IllegalArgumentException("invalid filter string" + filter);
			}
		}
	}

	private void parseActions(String actions) {
		// to get the tokens
		StringTokenizer st1 = new StringTokenizer(actions, ",");
		while (st1.hasMoreTokens()) {
			// do the action
			String str = st1.nextToken().trim();
			if (str.toLowerCase().equals("read"))
				this.mask |= _READ;
			else if (str.toLowerCase().equals("write"))
				this.mask |= _WRITE;
			else if (str.toLowerCase().equals("delete"))
				this.mask |= _DELETE;
			else if (str.equals(WILD_STRING))
				this.mask = _ALLACTIONS;
			else
				throw new IllegalArgumentException("invalid actions " + actions);
		}
	}

	@Override
	public boolean implies(Permission p) {
		if (!(p instanceof ChannelPermission))
			return false;

		ChannelPermission that = (ChannelPermission) p;

		if (this.mask < that.mask)
			return false;

		if (!this.busId.implies(that.busId))
			return false;
		if (!this.devAddrs.implies(that.devAddrs))
			return false;
		if (!this.chAddrs.implies(that.chAddrs))
			return false;

		return true;
	}
}
