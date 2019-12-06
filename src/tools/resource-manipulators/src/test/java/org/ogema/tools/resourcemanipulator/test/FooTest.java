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
package org.ogema.tools.resourcemanipulator.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.StructureTestListener;
import org.ogema.model.locations.Room;
import org.ogema.model.locations.WorkPlace;
import org.ogema.tools.resourcemanipulator.model.CommonConfigurationNode;
import org.ogema.tools.resourcemanipulator.model.ProgramEnforcerModel;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class FooTest extends OsgiAppTestBase {
    
    Room cfg;
    
    
    
   
    @Test public void foo1() {
        Assert.assertNotNull("no app man", getApplicationManager());
        Assert.assertNotNull("no res man", getApplicationManager().getResourceManagement());
        
        cfg = getApplicationManager().getResourceManagement().createResource("cfg", Room.class);
        WorkPlace w1 = cfg.workPlaces().add();
        w1.create();
        StructureTestListener stl = new StructureTestListener();
        w1.occupancySensor().addStructureListener(stl);
        w1.occupancySensor().create();
        w1.delete();
    }
    
    @Test public void foo2() {
        cfg = getApplicationManager().getResourceManagement().createResource("cfg", Room.class);
        WorkPlace w1 = cfg.workPlaces().add();
        w1.create();
        StructureTestListener stl = new StructureTestListener();
        w1.occupancySensor().addStructureListener(stl);
        w1.occupancySensor().create();
        w1.delete();
    }
    
    @Test public void foo3() {
        cfg = getApplicationManager().getResourceManagement().createResource("cfg", Room.class);
        WorkPlace w1 = cfg.workPlaces().add();
        w1.create();
        StructureTestListener stl = new StructureTestListener();
        w1.occupancySensor().addStructureListener(stl);
        w1.occupancySensor().create();
        w1.delete();
    }

}
