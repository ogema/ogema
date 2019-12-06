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
package org.ogema.drivers.modbus.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.drivers.modbus.ModbusDriver;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.communication.ModbusAddress;
import org.ogema.model.communication.ModbusCommunicationInformation;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.net.ModbusTCPListener;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;

@Ignore("incomplete")
public class ModbusTest extends OsgiAppTestBase {

	private ResourceManagement resourceManagement;


	@Inject
	private ModbusDriver modbusDriver;
	
	public ModbusTest() {
		super(false);
	}

	@AfterClass
	@BeforeClass
	public static void delete() {
		// deleteData();
		try {
			FileUtils.deleteDirectory(new File("data"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}
	
	@Override
	@Configuration
	public Option[] config() {
		return new Option[] {
				CoreOptions.mavenBundle("org.ogema.drivers", "modbus-tcp-resource",	ogemaVersion).start(),
//				CoreOptions.wrappedBundle("mvn:net.wimpi/jamod/1.2"),
				CoreOptions.wrappedBundle("mvn:com.ghgande/j2mod/2.1.2"),
				CoreOptions.composite(super.config()) };
	}

	@Before
	public void init() {
		resourceManagement = getApplicationManager().getResourceManagement();
	}

	@Test
	public void testModbusCommResourceCreation() {

		ByteBuffer bb = ByteBuffer.allocate(2);

		bb.putShort((short) 205);

		SimpleProcessImage spi = new SimpleProcessImage();

		SimpleInputRegister reg = new SimpleInputRegister(205);

		spi.addInputRegister(reg);

		SimpleInputRegister writeReg = new SimpleInputRegister();
		spi.addInputRegister(writeReg);

		ModbusTCPListener listener = new ModbusTCPListener(1);

		listener.setPort(Modbus.DEFAULT_PORT);

		InetAddress addr;
		try {
			addr = InetAddress.getByName("0.0.0.0");
			// TODO
//			listener.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TemperatureSensor test = resourceManagement.createResource("test",
				TemperatureSensor.class);

		TemperatureResource sensor = test.reading().create();

		createReadConnection(sensor);
		
		test.activate(true);

		sleep(2000);

		assertThat(test.reading().getValue(), is(20.5f));
		
		listener.stop();
	}

	private void createReadConnection(TemperatureResource sensor) {

		ModbusCommunicationInformation connection = sensor.addDecorator(
				"connection", ModbusCommunicationInformation.class);

		((TimeResource) connection.pollingConfiguration().pollingInterval()
				.create()).setValue(50l);

		ModbusAddress comAdress = connection.comAddress().create();

		((BooleanResource) comAdress.readable().create()).setValue(true);

		((BooleanResource) comAdress.writeable().create()).setValue(false);

		((StringResource) comAdress.host().create()).setValue("127.0.0.1");

		((IntegerResource) comAdress.port().create()).setValue(502);

		((FloatResource) connection.offset().create()).setValue(0.0f);

		((FloatResource) connection.factor().create()).setValue(0.1f);

		((IntegerResource) comAdress.register().create()).setValue(0);

		((IntegerResource) comAdress.unitId().create()).setValue(0);

		((IntegerResource) comAdress.count().create()).setValue(1);

		((StringResource) comAdress.dataType().create()).setValue("SHORT");

		((StringResource) comAdress.registerType().create())
				.setValue("INPUT_REGISTERS");
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}