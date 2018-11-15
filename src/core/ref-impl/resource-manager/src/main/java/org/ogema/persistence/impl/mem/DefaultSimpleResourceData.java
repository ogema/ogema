/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
