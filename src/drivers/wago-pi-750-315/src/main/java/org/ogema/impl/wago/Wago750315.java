package org.ogema.impl.wago;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.model.Resource;
import org.ogema.wago.AnalogIn;
import org.ogema.wago.AnalogOut;
import org.ogema.wago.DigitalIn;
import org.ogema.wago.DigitalOut;
import org.ogema.wago.IO;
import org.ogema.wago.ProcessImage;

/**
 * This class represents the process image of the WAGO 750-315 bus coupler
 * 
 * */

/**
 * @author mns
 * 
 */

public class Wago750315 implements ProcessImage, BusCoupler {

	/** use the modbus-rtu driver */
	private static final String DRIVER_ID = "modbus-rtu";

	static final int WORD = 16;
	static final int BIT = 1;
	private int currOutWordCount = 0;
	private int currInWordCount = 0;
	private int currDInOffset = 8;
	private int currDOutOffset = 8;
	private int currDOutCount = 0;
	private int currDInCount = 0;
	DigitalInImpl[] currentDIns;
	DigitalOutImpl[] currentDOuts;

	private String iface;

	private String devAddr;

	String deviceParameters;

	private ChannelAccess channelAccess;
	int[] outWords;

	public Wago750315(ChannelAccess channelAccess, String ifaceName, String devAddr, String baud) {
		currOutWordCount = 0;
		currInWordCount = 0;
		currDInOffset = 8;
		currDOutOffset = 8;
		currentDIns = new DigitalInImpl[16];
		currentDOuts = new DigitalOutImpl[16];
		this.iface = ifaceName;
		this.devAddr = devAddr;
		this.deviceParameters = baud + ":8:none:1:none:none:0:500";
		this.channelAccess = channelAccess;
	}

	@Override
	public AnalogIn[] addAnalogIn(int inputNumber) {
		AnalogIn[] ins = new AnalogIn[inputNumber];
		for (int i = 0; i < inputNumber; i++) {
			AnalogInImpl in = new AnalogInImpl();
			in.dataWidth = WORD;
			in.wordOffset = currInWordCount++;
			ins[i] = in;
		}
		return ins;
	}

	@Override
	public DigitalIn[] addDigitalIn(int inputNumber) {
		DigitalIn[] ins = new DigitalIn[inputNumber];
		for (int i = 0; i < inputNumber; i++) {
			DigitalInImpl in = new DigitalInImpl();
			in.bitOffset = currDInOffset;
			in.pmask = (short) (1 << currDInOffset);
			in.nmask = (short) ~((int) in.pmask);

			ins[i] = in;
			currentDIns[currDInOffset++] = in;
			currDInCount++;
			if (currDInCount == 7) {
				currDInOffset = 0;
			}
			else if (currDInCount == 15) {
				currDInOffset = 8;
				currDInCount = 0;
				for (DigitalInImpl tmpIn : currentDIns) {
					tmpIn.wordOffset = currInWordCount;
				}
				currInWordCount++;
			}
		}
		return ins;
	}

	@Override
	public AnalogOut[] addAnalogOut(int outputNumber) {
		AnalogOut[] outs = new AnalogOut[outputNumber];
		for (int i = 0; i < outputNumber; i++) {
			AnalogOutImpl out = new AnalogOutImpl();
			out.dataWidth = WORD;
			out.wordOffset = currOutWordCount++;
			outs[i] = out;
		}
		return outs;
	}

	@Override
	public DigitalOut[] addDigitalOut(int outputNumber) {
		DigitalOut[] outs = new DigitalOut[outputNumber];
		for (int i = 0; i < outputNumber; i++) {
			DigitalOutImpl out = new DigitalOutImpl();
			out.bitOffset = currDOutOffset;
			out.pmask = (short) (1 << currDOutOffset);
			out.nmask = (short) ~((int) out.pmask);
			out.pi = this;
			outs[i] = out;
			currentDOuts[currDOutOffset++] = out;
			currDOutCount++;
			if (currDOutCount == 7) {
				currDOutOffset = 0;
			}
			else if (currDOutCount == 15) {
				currDOutOffset = 8;
				currDOutCount = 0;
				for (DigitalOutImpl tmpOut : currentDOuts) {
					tmpOut.wordOffset = currOutWordCount;
				}
				currOutWordCount++;
			}
		}
		return outs;
	}

	@Override
	public void prepareChannel(IO io, int period, Resource res) {
		DeviceLocator deviceLocator;

		deviceLocator = new DeviceLocator(DRIVER_ID, iface, devAddr, deviceParameters);
		int regAddr = io.getWordOffset();
		String channelAddr = "reg:" + regAddr;
		ChannelLocator cl = new ChannelLocator(channelAddr, deviceLocator);
		ChannelConfiguration cfg = null;
		try {
			cfg = channelAccess.addChannel(cl, Direction.DIRECTION_INOUT, -1);
		} catch (ChannelAccessException e) {
			Activator.logger.info(e.getMessage());
		}
		io.setChannel(channelAccess, cfg, res);
	}

	@Override
	public int getOutWord(int index) {
		return this.outWords[index];
	}

	@Override
	public void setOutWord(int index, int val) {
		this.outWords[index] = val;
	}

	@Override
	public void terminate() {
		outWords = new int[currOutWordCount + 1];
	}

	public void shutdown() {
		for (DigitalInImpl din : currentDIns) {
			if (din != null)
				din.shutdown();
		}
		for (DigitalOutImpl dout : currentDOuts) {
			if (dout != null)
				dout.shutdown();
		}
	}
}
