package org.ogema.wago;

import org.ogema.core.model.Resource;

public interface ProcessImage {

	public AnalogIn[] addAnalogIn(int inputNumber);

	public DigitalIn[] addDigitalIn(int inputNumber);

	public AnalogOut[] addAnalogOut(int outputNumber);

	public DigitalOut[] addDigitalOut(int outputNumber);

	public void prepareChannel(IO io, int samplePeriods, Resource res);

	public void terminate();
}
