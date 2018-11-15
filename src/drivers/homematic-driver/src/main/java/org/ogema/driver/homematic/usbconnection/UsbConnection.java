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
package org.ogema.driver.homematic.usbconnection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.ogema.driver.homematic.Activator;
import org.ogema.driver.homematic.Constants;
import org.ogema.driver.homematic.tools.Converter;
import org.slf4j.Logger;
import org.usb4java.BufferUtils;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import org.usb4java.Transfer;
import org.usb4java.TransferCallback;

// FIXME event handling thread does not stop immediately on component shutdown
public class UsbConnection implements IUsbConnection {

	protected volatile Fifo<byte[]> inputFifo;
	protected volatile Object inputEventLock;
	private KeepAlive keepAlive;
	private Thread keepAliveThread;
	private Context context; // The LibUSB Driver Context
	private EventHandlingThread usbThread;
	private final TransferCallback messageReceived;
	private DeviceHandle handle;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	public UsbConnection() {

		messageReceived = new TransferCallback() {
			@Override
			public void processTransfer(Transfer transfer) {
				byte[] puffer = new byte[Constants.SIZE];
				ByteBuffer buffer = transfer.buffer();
				int i = 0;
				while (buffer.remaining() > 0) {
					puffer[i++] = buffer.get();
				}
				inputFifo.put(puffer);
				synchronized (inputEventLock) {
					inputEventLock.notify();
				}
				logger.debug("Answer from USB:");
				logger.trace(Converter.dumpHexString(puffer));
				LibUsb.freeTransfer(transfer);
				receive();
			}
		};

		context = new Context();
		inputFifo = new Fifo<byte[]>(6);
		inputEventLock = new Object();
		int result = LibUsb.init(context);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to initialize libusb.", result);
		}
	}

	// not yet used
	public Device findDevice(short vendorId, short productId) {
		// Read the USB device list
		DeviceList list = new DeviceList();
		int result = LibUsb.getDeviceList(null, list);
		if (result < 0)
			throw new LibUsbException("Unable to get device list", result);

		try {
			// Iterate over all devices and scan for the right one
			for (Device device : list) {
				DeviceDescriptor descriptor = new DeviceDescriptor();
				result = LibUsb.getDeviceDescriptor(device, descriptor);
				logger.debug(String.format("Product: %h, Vendor: %h", descriptor.idProduct(), descriptor.idVendor()));
				if (result != LibUsb.SUCCESS)
					throw new LibUsbException("Unable to read device descriptor", result);
				if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) {
					logger.debug("Homematic coordinator device found.");
					return device;
				}
			}
		} finally {
			// Ensure the allocated device list is freed
			LibUsb.freeDeviceList(list, true);
		}

		// Device not found
		logger.debug("Homematic coordinator device not found.");
		return null;
	}

	private void receive() {
		ByteBuffer buffer = BufferUtils.allocateByteBuffer(Constants.SIZE).order(ByteOrder.LITTLE_ENDIAN);
		Transfer transfer = LibUsb.allocTransfer();
		LibUsb.fillBulkTransfer(transfer, handle, Constants.ENDPOINT_IN, buffer, messageReceived, null,
				Constants.USBCOM_TIMEOUT);
		logger.debug("Receiveframe sent");
		int result = LibUsb.submitTransfer(transfer);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to submit transfer", result);
		}
	}

	private void send(byte[] data) {
		ByteBuffer buffer = BufferUtils.allocateByteBuffer(Constants.SIZE);
		buffer.put(data);
		IntBuffer transferred = BufferUtils.allocateIntBuffer();
		int result = LibUsb.bulkTransfer(handle, Constants.ENDPOINT_OUT, buffer, transferred, Constants.USBCOM_TIMEOUT);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to send data", result);
		}
		logger.debug("Sending to USB: ");
		logger.trace(Converter.dumpHexString(data));
	}

	private void initiate() {
		keepAlive = new KeepAlive(this);
		keepAliveThread = new Thread(keepAlive);
		keepAliveThread.setName("homematic-lld-keepAlive");
		keepAliveThread.start();
	}

	@Override
	public byte[] getReceivedFrame() {

		return inputFifo.get();
	}

	@Override
	public void sendFrame(byte[] frame) {
		send(frame);
	}

	public boolean connect() {
		// This is very straight through, maybe TODO: more dynamic by using of configurable properties for product and
		// vendor id's
		Device dev = findDevice(Constants.VENDOR_ID, Constants.PRODUCT_ID);
		if (dev != null) {
			handle = new DeviceHandle();
			int result = LibUsb.open(dev, handle);
			if (result != LibUsb.SUCCESS)
				throw new LibUsbException("Unable to open USB device", result);
		}
		if (handle == null) {
			logger.debug("The coordinator hardware seems not to be connected. Please plug your hardware in.");
			return false;
		}
		else {
			int r = LibUsb.detachKernelDriver(handle, Constants.INTERFACE);
			if (r != LibUsb.SUCCESS && r != LibUsb.ERROR_NOT_SUPPORTED && r != LibUsb.ERROR_NOT_FOUND)
				throw new LibUsbException("Unable to detach kernel driver", r);
			usbThread = new EventHandlingThread();
			usbThread.setName("homematic-lld-usbThread");
			usbThread.start();
			// Claim the interface
			int result = LibUsb.claimInterface(handle, Constants.INTERFACE);
			if (result != LibUsb.SUCCESS) {
				throw new LibUsbException("Unable to claim interface", result);
			}
			else {
				logger.info("Homematic device found.");
				receive();
				initiate();
				return true;
			}
		}
	}

	@Override
	public void closeConnection() {
		keepAlive.stop();
		keepAliveThread.interrupt();
		if (usbThread.isAlive()) {
			usbThread.abort();
			try {
				// FIXME this takes too long to wait for in the stop method
				System.out.println("   waiting for Homematic local device ");
				usbThread.join();
				System.out.println("        Homematic local device done");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int result = LibUsb.releaseInterface(handle, Constants.INTERFACE);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to release interface", result);
		}
		LibUsb.close(handle);
		LibUsb.exit(context);
	}

	@Override
	public Object getInputEventLock() {
		return inputEventLock;
	}

	@Override
	public boolean hasFrames() {
		return inputFifo.count > 0 ? true : false;
	}

	@Override
	public void setConnectionAddress(String address) {
		this.keepAlive.setConnectionAddress(address);
	}

	class EventHandlingThread extends Thread {
		/** If thread should abort. */
		private volatile boolean abort;

		/**
		 * Aborts the event handling thread.
		 */
		public void abort() {
			this.abort = true;
		}

		// can we stop this somehow?
		@Override
		public void run() {
			while (!this.abort && Activator.bundleIsRunning) {
				int result = LibUsb.handleEventsTimeout(null, 3000000);
				if (result != LibUsb.SUCCESS)
					logger.error("Unable to handle events %d", result);
			}
		}
	}
}
