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
package org.ogema.driver.zwave.manager;

import java.util.concurrent.atomic.AtomicReference;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.zwave.Channel;
import org.slf4j.Logger;
import org.zwave4j.Manager;
import org.zwave4j.ValueId;

/**
 * 
 * @author baerthbn
 * 
 */
public class NodeValue {
	private final String channelAddress;
	private ValueId valueid;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("zwave-driver");
	private Channel channel;
	private boolean hasListener = false;
	private Manager manager;

	public NodeValue(Manager manager, ValueId valueid, String channelAddress) {
		this.manager = manager;
		this.valueid = valueid;
		this.channelAddress = channelAddress;
		logger.debug("Channel Address: " + channelAddress);
	}

	public ValueId getValueid() {
		return valueid;
	}

	/**
	 * @return COMMANDCLASSID:INSTANCEID:VALUEID
	 */
	public String getChannelAddress() {
		return channelAddress;
	}

	public boolean readOnly() {
		return manager.isValueReadOnly(valueid);
	}

	public boolean writeOnly() {
		return manager.isValueWriteOnly(valueid);
	}

	public String getValueName() {
		return manager.getValueLabel(valueid);
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void setListener(boolean b) {
		hasListener = b;
	}

	/**
	 * returns primitive Types
	 */
	public Object getValue() {
		switch (valueid.getType()) {
		case BOOL:
			AtomicReference<Boolean> b = new AtomicReference<>();
			manager.getValueAsBool(valueid, b);
			return b.get();
		case BYTE:
			AtomicReference<Byte> bb = new AtomicReference<>();
			manager.getValueAsByte(valueid, bb);
			return bb.get();
		case DECIMAL:
			AtomicReference<Float> f = new AtomicReference<>();
			manager.getValueAsFloat(valueid, f);
			return f.get();
		case INT:
			AtomicReference<Integer> i = new AtomicReference<>();
			manager.getValueAsInt(valueid, i);
			return i.get();
		case LIST:
			return null;
		case SCHEDULE:
			return null;
		case SHORT:
			AtomicReference<Short> s = new AtomicReference<>();
			manager.getValueAsShort(valueid, s);
			return s.get();
		case STRING:
			AtomicReference<String> ss = new AtomicReference<>();
			manager.getValueAsString(valueid, ss);
			return ss.get();
		case BUTTON:
			return null;
		case RAW:
			AtomicReference<byte[]> sss = new AtomicReference<>();
			manager.getValueAsRaw(valueid, sss);
			return sss.get();
		default:
			return null;
		}
	}

	/**
	 * sets primitive Types
	 */
	public void setValue(Object value) {
		switch (valueid.getType()) {
		case BOOL:
			manager.setValueAsBool(valueid, (boolean) value);
			break;
		case BYTE:
			manager.setValueAsByte(valueid, (byte) value);
			break;
		case DECIMAL:
			manager.setValueAsFloat(valueid, (float) value);
			break;
		case INT:
			manager.setValueAsInt(valueid, (int) value);
			break;
		case LIST:
			manager.setValueListSelection(valueid, (String) value);
		case SCHEDULE:
			throw new UnsupportedOperationException();
		case SHORT:
			manager.setValueAsShort(valueid, (short) value);
			break;
		case STRING:
			manager.setValueAsString(valueid, (String) value);
			break;
		case BUTTON:
			throw new UnsupportedOperationException();
		case RAW:
			manager.setValueAsRaw(valueid, (byte[]) value);
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * getting value from LL to OGEMA
	 */
	public Value getOGEMAValue() {
		switch (valueid.getType()) {
		case BOOL:
			AtomicReference<Boolean> b = new AtomicReference<>();
			manager.getValueAsBool(valueid, b);
			return new BooleanValue(b.get());
		case BYTE:
			// saving in first byte of array
			AtomicReference<Byte> bb = new AtomicReference<>();
			manager.getValueAsByte(valueid, bb);
			byte[] ba = { 0 };
			ba[0] = bb.get();
			return new ByteArrayValue(ba);
		case DECIMAL:
			AtomicReference<Float> f = new AtomicReference<>();
			manager.getValueAsFloat(valueid, f);
			return new FloatValue(f.get());
		case INT:
			AtomicReference<Integer> i = new AtomicReference<>();
			manager.getValueAsInt(valueid, i);
			return new IntegerValue(i.get());
		case LIST:
			throw new UnsupportedOperationException();
		case SCHEDULE:
			throw new UnsupportedOperationException();
		case SHORT:
			AtomicReference<Short> s = new AtomicReference<>();
			manager.getValueAsShort(valueid, s);
			return new IntegerValue(s.get());
		case STRING:
			AtomicReference<String> ss = new AtomicReference<>();
			manager.getValueAsString(valueid, ss);
			return new StringValue(ss.get());
		case BUTTON:
			throw new UnsupportedOperationException();
		case RAW:
			AtomicReference<byte[]> sss = new AtomicReference<>();
			manager.getValueAsRaw(valueid, sss);
			return new ByteArrayValue(sss.get());
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * setting value from OGEMA to LL
	 */
	public void setOGEMAValue(Value value) {
		switch (valueid.getType()) {
		case BOOL:
			manager.setValueAsBool(valueid, value.getBooleanValue());
			break;
		case BYTE:
			// only first byte is used
			manager.setValueAsByte(valueid, value.getByteArrayValue()[0]);
			break;
		case DECIMAL:
			manager.setValueAsFloat(valueid, value.getFloatValue());
			break;
		case INT:
			manager.setValueAsInt(valueid, value.getIntegerValue());
			break;
		case LIST:
			throw new UnsupportedOperationException();
		case SCHEDULE:
			throw new UnsupportedOperationException();
		case SHORT:
			manager.setValueAsShort(valueid, (short) (value.getIntegerValue() & 0xFFFF));
			break;
		case STRING:
			manager.setValueAsString(valueid, value.getStringValue());
			break;
		case BUTTON:
			throw new UnsupportedOperationException();
		case RAW:
			manager.setValueAsRaw(valueid, value.getByteArrayValue());
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * alerts channel (from LL)
	 */
	public void valueChanged() {
		if (hasListener)
			channel.updateListener();
	}

}
