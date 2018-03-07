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
package org.ogema.resourcemanager.tests;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.model.locations.Building;
import org.ogema.model.locations.BuildingPropertyUnit;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

@ExamReactorStrategy(PerClass.class)
public class PersistenceRestartTest extends TestBase {

    private static Bundle findPersistence(final BundleContext ctx) {
        for (Bundle b : ctx.getBundles()) {
            if ("org.ogema.ref-impl.persistence".equals(b.getSymbolicName())) {
                return b;
            }
        }
        Assert.fail("Persistence not found");
        return null;
    }

    private void restartPersistence() throws BundleException {
        final Bundle persistence = findPersistence(ctx);
        System.out.printf("restarting bundle %s%n", persistence);
        persistence.stop();
        //Assert.assertNotEquals(Bundle.ACTIVE, persistence.getState());
        Assert.assertEquals(Bundle.RESOLVED, persistence.getState());
        persistence.start();
        Assert.assertEquals(Bundle.ACTIVE, persistence.getState());
        System.out.printf("restart complete%n");
    }

    private ResourceList<TemperatureSensor> createList() {
        @SuppressWarnings("unchecked")
        final ResourceList<TemperatureSensor> list = getApplicationManager().getResourceManagement().createResource(newResourceName(), ResourceList.class);
        list.setElementType(TemperatureSensor.class);
        return list;
    }

    // contrary to the tests below, here the list is not an optional element of some parent resource 
    @Test
    public void listTypeRemainsSetAfterRestart() throws InterruptedException, BundleException {
        final ResourceList<TemperatureSensor> list = createList();
        final String path = list.getPath();
        Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
        restartPersistence();
        final ResourceList<?> list2 = waitForAppManager().getResourceAccess().getResource(path);
        Assert.assertNotNull("Resource list is gone", list2);
        Assert.assertNotNull("Resource list has lost its element type", list2.getElementType());
        Assert.assertEquals("Resource list element type has changed", TemperatureSensor.class, list2.getElementType());
        list2.delete();
    }

    // here the list does not have any subresources
    @Test
    public void listTypeRemainsSetAfterRestart2() throws InterruptedException, BundleException {
        final Building building = getApplicationManager().getResourceManagement().createResource(newResourceName(), Building.class);
        final String path = building.getPath();
        building.buildingPropertyUnits().create();
        Assert.assertEquals("Resource list element type unexpected", BuildingPropertyUnit.class, building.buildingPropertyUnits().getElementType());
        Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
        restartPersistence();
        final Building building2 = waitForAppManager().getResourceAccess().getResource(path);
        Assert.assertNotNull(building2);
        Assert.assertTrue(building2.buildingPropertyUnits().exists());
        Assert.assertNotNull("Resource list has lost its element type", building2.buildingPropertyUnits().getElementType());
        Assert.assertEquals("Resource list element type has changed", BuildingPropertyUnit.class, building2.buildingPropertyUnits().getElementType());
        building2.delete();
    }

    // here the list does not have any subresources, before the restart
    @Test
    public void listTypeRemainsSetAfterRestart3() throws InterruptedException, BundleException {
        final Building building = getApplicationManager().getResourceManagement().createResource(newResourceName(), Building.class);
        final String path = building.getPath();
        building.buildingPropertyUnits().create();
        Assert.assertEquals("Resource list element type unexpected", BuildingPropertyUnit.class, building.buildingPropertyUnits().getElementType());
        Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
        restartPersistence();
        final Building building2 = waitForAppManager().getResourceAccess().getResource(path);
        Assert.assertNotNull(building2);
        Assert.assertTrue(building2.buildingPropertyUnits().exists());
        final Resource r = building2.buildingPropertyUnits().add();
        Assert.assertEquals("Unexpected resource type", BuildingPropertyUnit.class, r.getResourceType());
        building2.delete();
    }

    // contrary to the above, here the list contains elements
    @Test
    public void listTypeRemainsSetAfterRestart4() throws InterruptedException, BundleException {
        final Building building = getApplicationManager().getResourceManagement().createResource(newResourceName(), Building.class);
        final String path = building.getPath();
        building.buildingPropertyUnits().<ResourceList<?>>create().add();
        Assert.assertEquals("Resource list element type unexpected", BuildingPropertyUnit.class, building.buildingPropertyUnits().getElementType());
        Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
        restartPersistence();
        final Building building2 = waitForAppManager().getResourceAccess().getResource(path);
        Assert.assertNotNull(building2);
        Assert.assertTrue(building2.buildingPropertyUnits().exists());
        Assert.assertNotNull("Resource list has lost its element type", building2.buildingPropertyUnits().getElementType());
        Assert.assertEquals("Resource list element type has changed", BuildingPropertyUnit.class, building2.buildingPropertyUnits().getElementType());
        building2.delete();
    }

    // here the list contains subresources of a different type than the element type
    @Test
    public void listTypeRemainsSetAfterRestart5() throws InterruptedException, BundleException {
        final Building building = getApplicationManager().getResourceManagement().createResource(newResourceName(), Building.class);
        final String path = building.getPath();
        building.buildingPropertyUnits().create();
        building.buildingPropertyUnits().addDecorator("testDec", TemperatureSensor.class);
        Assert.assertEquals("Resource list element type unexpected", BuildingPropertyUnit.class, building.buildingPropertyUnits().getElementType());
        Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
        restartPersistence();
        final Building building2 = waitForAppManager().getResourceAccess().getResource(path);
        Assert.assertNotNull(building2);
        Assert.assertTrue(building2.buildingPropertyUnits().exists());
        Assert.assertNotNull("Resource list has lost its element type", building2.buildingPropertyUnits().getElementType());
        Assert.assertEquals("Resource list element type has changed", BuildingPropertyUnit.class, building2.buildingPropertyUnits().getElementType());
        building2.delete();
    }

}
