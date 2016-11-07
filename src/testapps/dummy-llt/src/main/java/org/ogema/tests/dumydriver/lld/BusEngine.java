package org.ogema.tests.dumydriver.lld;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tests.dumydriver.hld.TimeStampContainer;

public class BusEngine implements Runnable {

	private Fifo<Object> fifo;
	private boolean running = true;
	private DummyDriver driver;

	BusEngine(DummyDriver drv) {
		this.driver = drv;
		this.fifo = new Fifo<Object>(12);
	}

	@Override
	public void run() {
		while (running) {
			List<?> l = (List<?>) fifo.get();
			if (l == null) {
				Thread.yield();
				continue;
			}
			Object o = l.get(0);
			if (o instanceof SampledValueContainer) {
				@SuppressWarnings("unchecked")
				List<SampledValueContainer> lsvc = (List<SampledValueContainer>) l;
				read(lsvc);
			}
			else {
				@SuppressWarnings("unchecked")
				List<ValueContainer> lvc = (List<ValueContainer>) l;
				write(lvc);
			}
		}
	}

	private void write(List<ValueContainer> o) {
		for (ValueContainer container : o) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}

		synchronized (o) {
			o.notify();
		}
		for (ValueContainer container : o) {
			ChannelLocator channelLocator = container.getChannelLocator();

			SampledValueContainer contBack = new SampledValueContainer(channelLocator);
			long millis = System.currentTimeMillis();
			Value ov = container.getValue();
			TimeStampContainer tsc = (TimeStampContainer) ov.getObjectValue();
			tsc.TS2 = millis;
			contBack.setSampledValue(new SampledValue(ov, millis, Quality.GOOD));
			ArrayList<SampledValueContainer> writeBack = new ArrayList<>();
			writeBack.add(contBack);
			driver.listener.channelsUpdated(writeBack);
		}

	}

	private void read(List<SampledValueContainer> lsvc) {
		for (SampledValueContainer container : lsvc) {

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			// read data
			container.setSampledValue(new SampledValue(new LongValue(System.currentTimeMillis()),
					System.currentTimeMillis(), Quality.GOOD));
		}
		synchronized (lsvc) {
			lsvc.notify();
		}
	}

	public void stop() {
		running = false;
		this.notifyAll();
	}

	public void putRequest(List<?> channels) {
		fifo.put(channels);
	}

}
