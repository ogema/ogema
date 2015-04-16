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
package org.ogema.persistence.impl.mem;

import java.lang.reflect.Array;

import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.resourcetree.SimpleResourceData;

/**
 * 
 * @author jlapp
 */
public class DefaultSimpleResourceData implements SimpleResourceData {

	Object data;

	@Override
	public boolean getBoolean() throws InvalidResourceTypeException {
		return Boolean.valueOf(String.valueOf(data));
	}

	@Override
	public void setBoolean(boolean z) throws InvalidResourceTypeException {
		data = z;
	}

	@Override
	public float getFloat() throws InvalidResourceTypeException {
		return data == null ? Float.NaN : (float) data;
	}

	@Override
	public void setFloat(float f) throws InvalidResourceTypeException {
		data = f;
	}

	@Override
	public int getInt() throws InvalidResourceTypeException {
		return data == null ? 0 : (int) data;
	}

	@Override
	public void setInt(int i) throws InvalidResourceTypeException {
		data = i;
	}

	@Override
	public long getLong() throws InvalidResourceTypeException {
		return data == null ? 0 : (long) data;
	}

	@Override
	public void setLong(long j) throws InvalidResourceTypeException {
		data = j;
	}

	@Override
	public String getString() throws InvalidResourceTypeException {
		return data == null ? "" : data.toString();
	}

	@Override
	public void setString(String s) throws InvalidResourceTypeException {
		data = s;
	}

	@Override
	public boolean[] getBooleanArr() throws InvalidResourceTypeException {
		return (boolean[]) data;
	}

	@Override
	public void setBooleanArr(boolean[] aZ) throws InvalidResourceTypeException {
		data = aZ;
	}

	@Override
	public float[] getFloatArr() throws InvalidResourceTypeException {
		return (float[]) data;
	}

	@Override
	public void setFloatArr(float[] aF) throws InvalidResourceTypeException {
		data = aF;
	}

	@Override
	public int[] getIntArr() throws InvalidResourceTypeException {
		return (int[]) data;
	}

	@Override
	public void setIntArr(int[] aI) throws InvalidResourceTypeException {
		data = aI;
	}

	@Override
	public long[] getLongArr() throws InvalidResourceTypeException {
		return (long[]) data;
	}

	@Override
	public void setLongArr(long[] aJ) throws InvalidResourceTypeException {
		data = aJ;
	}

	@Override
	public String[] getStringArr() throws InvalidResourceTypeException {
		return (String[]) data;
	}

	@Override
	public void setStringArr(String[] aS) throws InvalidResourceTypeException {
		data = aS;
	}

	@Override
	public byte[] getByteArr() throws InvalidResourceTypeException {
		return (byte[]) data;
	}

	@Override
	public void setByteArr(byte[] aB) throws InvalidResourceTypeException {
		data = aB;
	}

	@Override
	public int getArrayLength() throws UnsupportedOperationException {
		return Array.getLength(data);
	}

}
