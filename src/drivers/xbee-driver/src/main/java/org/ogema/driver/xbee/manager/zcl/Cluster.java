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
package org.ogema.driver.xbee.manager.zcl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.driver.xbee.Constants;
import org.ogema.driver.xbee.frames.ExplicitAddressingCommandFrame;
import org.ogema.driver.xbee.manager.Endpoint;

/**
 * This is the general Cluster class which is extended by concrete Clusters from the ZigBee Cluster Library or
 * respective Application Profiles.
 * 
 * @author puschas
 * 
 */
public class Cluster {
	private String name = "unknown";
	protected Endpoint endpoint;
	protected short clusterId;

	public Map<Byte, ClusterCommand> clusterCommands;
	public Map<Short, ClusterAttribute> clusterAttributes;

	public Cluster(short clusterId, Endpoint endpoint) {
		this.clusterId = clusterId;
		clusterCommands = new HashMap<Byte, ClusterCommand>();
		clusterAttributes = new HashMap<Short, ClusterAttribute>();
		this.endpoint = endpoint;
	}

	public short getClusterId() {
		return clusterId;
	}

	/**
	 * This method checks the command in the payload and handles it accordingly.
	 * 
	 * @param payloadBuffer
	 *            the payload part of a message. This method is called by the MessageHandler. The position of the buffer
	 *            has to be at the command byte after the sequence byte.
	 * @param command
	 */
	public void readMessage(ByteBuffer payloadBuffer, byte command) {
		if (command == Constants.READ_ATTRIBUTES_RESPONSE) {
			readAttributesResponse(payloadBuffer);
			// TODO check if successful or not and tell channel
		}
		else if (command == Constants.WRITE_ATTRIBUTES_RESPONSE) {
			// TODO check if successful or not and tell channel
		}
	}

	/**
	 * Reads the attributes, converts them and puts them into the clusterAttribute
	 * 
	 * Conversions: General data -> ByteArrayValue * Boolean -> BooleanValue * Bitmap -> IntegerValue/LongValue
	 * (depending on size) * Unsigned Integer -> IntegerValue/LongValue (depending on size) * Signed Integer ->
	 * IntegerValue/LongValue (depending on size) * Enumeration -> IntegerValue * Semi-precision float -> FloatValue *
	 * Single precision float -> FloatValue * Double precision float -> DoubleValue * String -> StringValue * Array ->
	 * TODO * Structure -> TODO * Set -> TODO * Bag -> TODO * Time -> IntegerValue * Identifier -> IntegerValue * IEEE
	 * address -> LongValue * 128-bit security key -> ByteArrayValue
	 * 
	 * @param payloadBuffer
	 * @see ZigBee Cluster Library Page 83 - 99
	 */
	private void readAttributesResponse(ByteBuffer payloadBuffer) {
		while (payloadBuffer.position() + 4 < payloadBuffer.limit()) { // 4 =
																		// attribute(2)
																		// +
																		// status(1)
																		// +
																		// datatype(1)
			short attributeIdentifier = payloadBuffer.getShort();
			attributeIdentifier = Short.reverseBytes(attributeIdentifier);
			ClusterAttribute clusterAttribute = clusterAttributes.get(attributeIdentifier);
			byte status = payloadBuffer.get();
			if (Constants.STATUS_SUCCESS == status) { // This means it was
														// successful
				byte dataType = payloadBuffer.get();
				switch (dataType) {
				// NULL
				case (Constants.UNKOWN):
				case (Constants.NO_DATA):
					clusterAttribute.setValue(null);
					break;
				// General data
				case (Constants.EIGHT_BIT_DATA):
					clusterAttribute.setValue(new ByteArrayValue(new byte[] { payloadBuffer.get() }));
					break;
				case (Constants.SIXTEEN_BIT_DATA):
					byte[] sixteenBitDataArray = new byte[2];
					payloadBuffer.get(sixteenBitDataArray, 0, 2);
					Constants.reverseByteArray(sixteenBitDataArray);
					clusterAttribute.setValue(new ByteArrayValue(sixteenBitDataArray));
					break;
				case (Constants.TWENTYFOUR_BIT_DATA):
					byte[] twentyFourBitDataArray = new byte[3];
					payloadBuffer.get(twentyFourBitDataArray, 0, 3);
					Constants.reverseByteArray(twentyFourBitDataArray);
					clusterAttribute.setValue(new ByteArrayValue(twentyFourBitDataArray));
					break;
				case (Constants.THIRTYTWO_BIT_DATA):
					byte[] thirtyTwoBitDataArray = new byte[4];
					payloadBuffer.get(thirtyTwoBitDataArray, 0, 4);
					Constants.reverseByteArray(thirtyTwoBitDataArray);
					clusterAttribute.setValue(new ByteArrayValue(thirtyTwoBitDataArray));
					break;
				case (Constants.FORTY_BIT_DATA):
					byte[] fortyBitDataArray = new byte[5];
					payloadBuffer.get(fortyBitDataArray, 0, 5);
					Constants.reverseByteArray(fortyBitDataArray);
					clusterAttribute.setValue(new ByteArrayValue(fortyBitDataArray));
					break;
				case (Constants.FORTYEIGHT_BIT_DATA):
					byte[] fortyEightBitDataArray = new byte[6];
					payloadBuffer.get(fortyEightBitDataArray, 0, 6);
					Constants.reverseByteArray(fortyEightBitDataArray);
					clusterAttribute.setValue(new ByteArrayValue(fortyEightBitDataArray));
					break;
				case (Constants.FIFTYSIX_BIT_DATA):
					byte[] fiftySixBitDataArray = new byte[7];
					payloadBuffer.get(fiftySixBitDataArray, 0, 7);
					Constants.reverseByteArray(fiftySixBitDataArray);
					clusterAttribute.setValue(new ByteArrayValue(fiftySixBitDataArray));
					break;
				case (Constants.SIXTYFOUR_BIT_DATA):
					byte[] sixtyFourBitDataArray = new byte[8];
					payloadBuffer.get(sixtyFourBitDataArray, 0, 8);
					Constants.reverseByteArray(sixtyFourBitDataArray);
					clusterAttribute.setValue(new ByteArrayValue(sixtyFourBitDataArray));
					break;
				// Logical
				case (Constants.BOOLEAN):
					switch (payloadBuffer.get()) {
					case 0x00:
						clusterAttribute.setValue(new BooleanValue(false));
						break;
					case 0x01:
						clusterAttribute.setValue(new BooleanValue(true));
						break;
					}
					break;
				// Bitmap, Unsigned, Enum
				case (Constants.EIGHT_BIT_BITMAP):
				case (Constants.EIGHT_BIT_ENUMERATION):
				case (Constants.UNSIGNED_EIGHT_BIT_INTEGER):
					clusterAttribute.setValue(new IntegerValue(payloadBuffer.get() & 0xff));
					break;
				case (Constants.SIXTEEN_BIT_BITMAP):
				case (Constants.SIXTEEN_BIT_ENUMERATION):
				case (Constants.CLUSTER_ID):
				case (Constants.ATTRIBUTE_ID):
				case (Constants.UNSIGNED_SIXTEEN_BIT_INTEGER):
					clusterAttribute.setValue(new IntegerValue(Short.reverseBytes(payloadBuffer.getShort()) & 0xffff));
					break;
				case (Constants.TWENTYFOUR_BIT_BITMAP):
				case (Constants.UNSIGNED_TWENTYFOUR_BIT_INTEGER):
					byte[] u24Bit = new byte[3];
					payloadBuffer.get(u24Bit, 0, 3);
					int uInt24Bit = (((((int) u24Bit[0]) << 0) & 0x000000ff))
							| (((((int) u24Bit[1]) << 8) & 0x0000ff00)) | (((((int) u24Bit[2]) << 16) & 0x00ff0000));
					clusterAttribute.setValue(new IntegerValue(uInt24Bit));
					break;
				case (Constants.THIRTYTWO_BIT_BITMAP):
				case (Constants.UTCTIME):
				case (Constants.TIME_OF_DAY):
				case (Constants.DATE):
				case (Constants.UNSIGNED_THIRTYTWO_BIT_INTEGER):
				case (Constants.SIGNED_THIRTYTWO_BIT_INTEGER):
					clusterAttribute.setValue(new IntegerValue(Integer.reverseBytes((payloadBuffer.getInt()))));
					break;
				case (Constants.FORTY_BIT_BITMAP):
				case (Constants.UNSIGNED_FORTY_BIT_INTEGER):
					byte[] u40Bit = new byte[5];
					payloadBuffer.get(u40Bit, 0, 5);
					long uLong40Bit = (((((long) u40Bit[0]) << 0) & 0x00000000000000ffL))
							| (((((long) u40Bit[1]) << 8) & 0x000000000000ff00L))
							| (((((long) u40Bit[2]) << 16) & 0x0000000000ff0000L))
							| (((((long) u40Bit[3]) << 24) & 0x00000000ff000000L))
							| (((((long) u40Bit[4]) << 32) & 0x000000ff00000000L));
					clusterAttribute.setValue(new LongValue(uLong40Bit));
					break;
				case (Constants.FORTYEIGHT_BIT_BITMAP):
				case (Constants.UNSIGNED_FORTYEIGHT_BIT_INTEGER):
					byte[] u48Bit = new byte[6];
					payloadBuffer.get(u48Bit, 0, 6);
					long uLong48Bit = (((((long) u48Bit[0]) << 0) & 0x00000000000000ffL))
							| (((((long) u48Bit[1]) << 8) & 0x000000000000ff00L))
							| (((((long) u48Bit[2]) << 16) & 0x0000000000ff0000L))
							| (((((long) u48Bit[3]) << 24) & 0x00000000ff000000L))
							| (((((long) u48Bit[4]) << 32) & 0x000000ff00000000L))
							| (((((long) u48Bit[5]) << 40) & 0x0000ff0000000000L));
					clusterAttribute.setValue(new LongValue(uLong48Bit));
					break;
				case (Constants.FIFTYSIX_BIT_BITMAP):
				case (Constants.UNSIGNED_FIFTYSIX_BIT_INTEGER):
					byte[] u56Bit = new byte[7];
					payloadBuffer.get(u56Bit, 0, 7);
					long uLong56Bit = (((((long) u56Bit[0]) << 0) & 0x00000000000000ffL))
							| (((((long) u56Bit[1]) << 8) & 0x000000000000ff00L))
							| (((((long) u56Bit[2]) << 16) & 0x0000000000ff0000L))
							| (((((long) u56Bit[3]) << 24) & 0x00000000ff000000L))
							| (((((long) u56Bit[4]) << 32) & 0x000000ff00000000L))
							| (((((long) u56Bit[5]) << 40) & 0x0000ff0000000000L))
							| (((((long) u56Bit[6]) << 48) & 0x00ff000000000000L));
					clusterAttribute.setValue(new LongValue(uLong56Bit));
					break;
				case (Constants.SIXTYFOUR_BIT_BITMAP):
				case (Constants.IEEE_ADDRESS):
				case (Constants.UNSIGNED_SIXTYFOUR_BIT_INTEGER):
				case (Constants.SIGNED_SIXTYFOUR_BIT_INTEGER):
					clusterAttribute.setValue(new LongValue(Long.reverseBytes(payloadBuffer.getLong())));
					break;
				// Signed
				case (Constants.SIGNED_EIGHT_BIT_INTEGER):
					clusterAttribute.setValue(new IntegerValue(payloadBuffer.get()));
					break;
				case (Constants.SIGNED_SIXTEEN_BIT_INTEGER):
					clusterAttribute.setValue(new IntegerValue(Short.reverseBytes(payloadBuffer.getShort())));
					break;
				case (Constants.SIGNED_TWENTYFOUR_BIT_INTEGER):
					byte[] s24Bit = new byte[3];
					payloadBuffer.get(s24Bit, 0, 3);
					int sInt24Bit = ((((int) s24Bit[0] << 0) & 0x000000ff) | (((int) s24Bit[1] << 8) & 0x0000ff00) | (((int) s24Bit[2] << 16)));
					clusterAttribute.setValue(new IntegerValue(sInt24Bit));
					break;
				case (Constants.SIGNED_FORTY_BIT_INTEGER):
					byte[] s40Bit = new byte[5];
					payloadBuffer.get(s40Bit, 0, 5);
					long sLong40Bit = (((((long) s40Bit[0]) << 0) & 0x00000000000000ffL))
							| (((((long) s40Bit[1]) << 8) & 0x000000000000ff00L))
							| (((((long) s40Bit[2]) << 16) & 0x0000000000ff0000L))
							| (((((long) s40Bit[3]) << 24) & 0x00000000ff000000L)) | (((((long) s40Bit[4]) << 32)));
					clusterAttribute.setValue(new LongValue(sLong40Bit));
					break;
				case (Constants.SIGNED_FORTYEIGHT_BIT_INTEGER):
					byte[] s48Bit = new byte[6];
					payloadBuffer.get(s48Bit, 0, 6);
					long sLong48Bit = (((((long) s48Bit[0]) << 0) & 0x00000000000000ffL))
							| (((((long) s48Bit[1]) << 8) & 0x000000000000ff00L))
							| (((((long) s48Bit[2]) << 16) & 0x0000000000ff0000L))
							| (((((long) s48Bit[3]) << 24) & 0x00000000ff000000L))
							| (((((long) s48Bit[4]) << 32) & 0x000000ff00000000L)) | (((((long) s48Bit[5]) << 40)));
					clusterAttribute.setValue(new LongValue(sLong48Bit));
					break;
				case (Constants.SIGNED_FIFTYSIX_BIT_INTEGER):
					byte[] s56Bit = new byte[6];
					payloadBuffer.get(s56Bit, 0, 6);
					long sLong56Bit = (((((long) s56Bit[0]) << 0) & 0x00000000000000ffL))
							| (((((long) s56Bit[1]) << 8) & 0x000000000000ff00L))
							| (((((long) s56Bit[2]) << 16) & 0x0000000000ff0000L))
							| (((((long) s56Bit[3]) << 24) & 0x00000000ff000000L))
							| (((((long) s56Bit[4]) << 32) & 0x000000ff00000000L))
							| (((((long) s56Bit[5]) << 40) & 0x0000ff0000000000L)) | (((((long) s56Bit[6]) << 48)));
					clusterAttribute.setValue(new LongValue(sLong56Bit));
					break;
				// Floating Point
				case (Constants.SEMI_PRECISION_FLOAT): // TODO test if this
														// works
					byte[] semiPrecisionFloatArray = new byte[2];
					payloadBuffer.get(semiPrecisionFloatArray, 0, 2);
					Constants.reverseByteArray(semiPrecisionFloatArray);

					byte[] pseudoSinglePrecisionFloatArray = { 0x00, 0x00, 0x00, 0x00 };
					pseudoSinglePrecisionFloatArray[3] = semiPrecisionFloatArray[1]; // Mantissa
					pseudoSinglePrecisionFloatArray[2] = (byte) (semiPrecisionFloatArray[0] & 0b00000011); // Mantissa
					pseudoSinglePrecisionFloatArray[1] = (byte) ((semiPrecisionFloatArray[0] & 0b00000100) << 5); // Mantissa
																													// +
																													// Exponent
					pseudoSinglePrecisionFloatArray[0] = (byte) ((semiPrecisionFloatArray[0] & 0b01111000) >>> 3); // Exponent
					pseudoSinglePrecisionFloatArray[0] |= (byte) (semiPrecisionFloatArray[0] & 0b10000000); // Signed
																											// Bit

					if ((semiPrecisionFloatArray[0] & 0b01111100) == 0b0111110) { // NaN,
																					// +-Infinity
						pseudoSinglePrecisionFloatArray[0] |= 0b01110000; // Fill
																			// up
																			// the
																			// Exponent
																			// with
																			// 1s
					}

					ByteBuffer pseudoFloatBuffer = ByteBuffer.wrap(pseudoSinglePrecisionFloatArray);
					clusterAttribute.setValue(new FloatValue(pseudoFloatBuffer.getFloat()));
					break;
				case (Constants.SINGLE_PRECISION_FLOAT):
					byte[] floatArray = new byte[4];
					payloadBuffer.get(floatArray, 0, 4);
					Constants.reverseByteArray(floatArray);
					ByteBuffer floatBuffer = ByteBuffer.wrap(floatArray);
					clusterAttribute.setValue(new FloatValue(floatBuffer.getFloat()));
					break;
				case (Constants.DOUBLE_PRECISION_FLOAT):
					byte[] doubleArray = new byte[8];
					payloadBuffer.get(doubleArray, 0, 8);
					Constants.reverseByteArray(doubleArray);
					ByteBuffer doubleBuffer = ByteBuffer.wrap(doubleArray);
					clusterAttribute.setValue(new DoubleValue(doubleBuffer.getLong()));
					break;
				// String
				case (Constants.OCTET_STRING):
				case (Constants.CHARACTER_STRING): // Assuming ASCII // defined
													// in complex descriptor
													// TODO
					short length = (short) (payloadBuffer.get() & 0xff);
					String characterString = new String(payloadBuffer.array(), payloadBuffer.position(), length);
					clusterAttribute.setValue(new StringValue(characterString));
					break;
				case (Constants.LONG_OCTET_STRING):
				case (Constants.LONG_CHARACTER_STRING): // Assuming ASCII //
														// defined in complex
														// descriptor TODO
					short longLength = (short) (payloadBuffer.get() & 0xffff);
					String longCharacterValue = new String(payloadBuffer.array(), payloadBuffer.position(), longLength);
					clusterAttribute.setValue(new StringValue(longCharacterValue));
					break;
				// Ordered Sequence
				// Array
				// TODO
				// Structure
				// TODO
				// Collection
				// Set
				// TODO
				// Bag
				// TODO
				// BACNet OID
				// TODO
				case (Constants.HUNDREDTWENTYEIGHT_BIT_SECURITY_KEY):
					byte[] securityKey128Bit = new byte[16];
					payloadBuffer.get(securityKey128Bit, 0, 16);
					Constants.reverseByteArray(securityKey128Bit);
					clusterAttribute.setValue(new ByteArrayValue(securityKey128Bit));
					break;
				}
			}
			else if (Constants.STATUS_UNSUPPORTED_ATTRIBUTE == status) {
				clusterAttribute.unsupportedAttribute();
			}
		}
	}

	/**
	 * TODO Checks the values that are to be sent to another device and converts them if necessary. The
	 * attributeIdentifier and variables are expected to be in Little Endian byte order.
	 * 
	 * This method is not used at the moment.
	 * 
	 * @param values
	 */
	// private void writeAttributesRequestValues(byte[] values) { // TODO need other format, maybe Array of
	// // ByteArrayObjects or sth
	// // like that
	// ByteBuffer payloadBuffer = ByteBuffer.wrap(values);
	// while (payloadBuffer.position() + 3 < payloadBuffer.limit()) { // 4 = attribute(2) + datatype(1) + data(x)
	// short attributeIdentifier = payloadBuffer.getShort();
	// attributeIdentifier = Short.reverseBytes(attributeIdentifier);
	// ClusterAttribute clusterAttribute = clusterAttributes.get(attributeIdentifier);
	//
	// byte dataType = payloadBuffer.get();
	// switch (dataType) {
	// // General data
	// case (Constants.EIGHT_BIT_DATA):
	// break;
	// }
	// }
	//
	// }

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void readMessage(ByteBuffer payloadBuffer, byte command, Object lockObject) {
		readMessage(payloadBuffer, command);
		synchronized (lockObject) {
			lockObject.notify();
		}
	}

	public byte[] getManufacturerSpecificAttributeMessage(byte command, short manufacturerId, short attributeId,
			byte[] messagePayload) {
		ByteBuffer dataPayload = ByteBuffer.allocate(7 + messagePayload.length);
		dataPayload.put((byte) 0x04); // Frame control
		dataPayload.putShort(Short.reverseBytes(manufacturerId));
		dataPayload.put((byte) 0x00); // Placeholder sequence number
		dataPayload.put(command); // attribute command
		dataPayload.putShort(Short.reverseBytes(attributeId));
		dataPayload.put(messagePayload);
		ExplicitAddressingCommandFrame frame = new ExplicitAddressingCommandFrame((byte) 0x01, this.endpoint
				.getDevice().getAddress64Bit(), this.endpoint.getDevice().getAddress16Bit(), (byte) 0x00, this.endpoint
				.getEndpointId(), this.getClusterId(), this.endpoint.getProfileId(), (byte) 0x00, (byte) 0x00,
				dataPayload.array());
		return frame.getMessage();
	}

	public byte[] getManufacturerSpecificCommandMessage(short manufacturerId, byte attributeId, byte[] messagePayload) {
		ByteBuffer dataPayload = ByteBuffer.allocate(5 + messagePayload.length);
		dataPayload.put((byte) 0x04); // frame control
		dataPayload.putShort(manufacturerId);
		dataPayload.put((byte) 0x00); // placeholder for sequence number
		dataPayload.put(attributeId);
		dataPayload.put(messagePayload);
		ExplicitAddressingCommandFrame frame = new ExplicitAddressingCommandFrame((byte) 0x01, this.endpoint
				.getDevice().getAddress64Bit(), this.endpoint.getDevice().getAddress16Bit(), (byte) 0x00, this.endpoint
				.getEndpointId(), this.getClusterId(), this.endpoint.getProfileId(), (byte) 0x00, (byte) 0x00,
				dataPayload.array());
		return frame.getMessage();
	}
}
