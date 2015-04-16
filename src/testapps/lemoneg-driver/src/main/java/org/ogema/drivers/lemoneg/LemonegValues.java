/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.drivers.lemoneg;

/**
 * Helper class for data storage of one recieved modbus frame.
 * 
 * @author pau
 * 
 */
public class LemonegValues {
	public float ueff;
	public float ieff;
	public float p;
	public short hz;
	public long timestamp;
}
