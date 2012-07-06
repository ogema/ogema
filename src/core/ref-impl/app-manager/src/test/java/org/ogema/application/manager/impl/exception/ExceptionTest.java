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
package org.ogema.application.manager.impl.exception;

import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.application.ExceptionListener;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.model.locations.Room;
import org.ogema.model.actors.OnOffSwitch;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * FIXME these tests probably do not do what they are supposed to.
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ExceptionTest extends OsgiTestBase {

	public static final String RESNAME = "ResourceTestResource";
	Throwable lastException = null;

	String resourcePath = null;

	//CyclicBarrier cbBarrier = new CyclicBarrier(2);
	@Before
	public void doBefore() {
		getApplicationManager().addExceptionListener(listener);
		Resource resource = resMan.createResource(RESNAME + "42", Room.class);

		resourcePath = resource.getPath("/");
	}

	String lastError;
	private int exceptionCounter = 0;
	private final ExceptionListener listener = new ExceptionListener() {

		@Override
		public void exceptionOccured(Throwable exception) {
			System.out.println("Received Exception " + exception.getClass().getCanonicalName());
			exceptionCounter += 1;
			lastError = exception.getClass().toString();
			lastException = exception;
		}
	};

	@Test
	@Ignore
	public void addOptionalElementFailsTest() throws Exception {
		int oldCount = exceptionCounter;

		OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
		try {
			res.addOptionalElement("ANONEXITSINTGMANEwr");
			fail("missing exception");
		} catch (NoSuchResourceException nsre) {
		}
		//cbBarrier.await(5, TimeUnit.SECONDS);
		//cbBarrier.reset();
		Assert.assertTrue(exceptionCounter == oldCount + 1);
		Assert.assertEquals(lastError, NoSuchResourceException.class.toString());

	}

	public void startFails() {
		// TODO
	}

	@Test
	public void getResourceTest() {

		Resource resource2 = resAcc.getResource(resourcePath);
		System.out.println(resource2.getPath());
	}

	@Test
	@Ignore
	public void addDecoratorFailsTest() throws Exception {
		Resource resource = resMan.createResource(RESNAME + "42", Room.class);
		int oldCount = exceptionCounter;
		try {
			resource.addDecorator("23", IntegerResource.class);
			fail("missing exception");
		} catch (NoSuchResourceException nsre) {
		}
		//cbBarrier.await(5, TimeUnit.SECONDS);
		//cbBarrier.reset();
		Assert.assertTrue(exceptionCounter == oldCount + 1);
		Assert.assertEquals(lastError, NoSuchResourceException.class.toString());
	}

	// FIXME Currently throws a ResourceException, not an InvalidResourceException
	@Test
	@Ignore
	public void setOptionalElementFailsTest() {

		int oldCount = exceptionCounter;
		Resource resource = resMan.createResource(RESNAME + "42", Room.class);
		Resource resourceChidl = resMan.createResource(RESNAME + "child", Room.class);
		try {
			resource.setOptionalElement("occSens", resourceChidl);
			fail("missing exception");
		} catch (InvalidResourceTypeException re) {
			// proper exception received
		}
		Assert.assertTrue(exceptionCounter == oldCount + 1);
		Assert.assertEquals(lastError, ResourceException.class.toString());
	}

	@Test
	public void createResourcesWithSameNameAndDifferentTypes() {

		// Resource resource = resAcc.getResource("/test");
		// resource.addDecorator(name, resourceType)
		// virtual Resource Exception
		// Resource resource = resMan.createResource(RESNAME + "42",
		// Room.class);
		// Resource resource2 = resource.addOptionalElement("type");
		// IntegerResource createResource = resMan.createResource(RESNAME +
		// "42",
		// IntegerResource.class);
		// FIXME an other Error is fired
	}

}
