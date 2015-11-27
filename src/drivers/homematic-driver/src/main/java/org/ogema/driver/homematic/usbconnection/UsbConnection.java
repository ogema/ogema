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

public class UsbConnection implements IUsbConnection {

	protected volatile Fifo<byte[]> inputFifo;
	protected volatile Object inputEventLock;
	private KeepAlive keepAlive;
	private Thread keepAliveThread;
	private Context context; // The LibUSB Driver Context
	private EventHandlingThread usbThread = new EventHandlingThread();
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
				logger.debug(Converter.dumpHexString(puffer));
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
				if (result != LibUsb.SUCCESS)
					throw new LibUsbException("Unable to read device descriptor", result);
				if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId)
					return device;
			}
		} finally {
			// Ensure the allocated device list is freed
			LibUsb.freeDeviceList(list, true);
		}

		// Device not found
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
		logger.debug(Converter.dumpHexString(data));

		// long timeStamp = System.currentTimeMillis();
		// System.out.print("send: ");
		// System.out.println(timeStamp);
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
		// Open test device
		// This is very straight through, maybe TODO: more dynamic
		handle = LibUsb.openDeviceWithVidPid(context, Constants.VENDOR_ID, Constants.PRODUCT_ID);
		if (handle == null) {
			logger.debug("The coordinator hardware seems not to be connected. Please plug your hardware in.");
			return false;
		}
		else {
			int r = LibUsb.detachKernelDriver(handle, Constants.INTERFACE);
			if (r != LibUsb.SUCCESS && r != LibUsb.ERROR_NOT_SUPPORTED && r != LibUsb.ERROR_NOT_FOUND)
				throw new LibUsbException("Unable to detach kernel driver", r);
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
		keepAliveThread.interrupt();
		keepAlive.stop();
		if (usbThread.isAlive()) {
			usbThread.abort();
			try {
				usbThread.join();
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

		@Override
		public void run() {
			while (!this.abort && Activator.bundleIsRunning) {
				int result = LibUsb.handleEventsTimeout(null, 250000000);
				if (result != LibUsb.SUCCESS)
					logger.error("Unable to handle events %d", result);
			}
		}
	}
}
