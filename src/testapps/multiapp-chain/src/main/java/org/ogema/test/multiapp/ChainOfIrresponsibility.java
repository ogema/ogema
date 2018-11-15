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
package org.ogema.test.multiapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Component;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
@Component
public class ChainOfIrresponsibility implements Application{
    
    /**
     * System property ({@value}) for setting the number of test apps created.
     */
    public static final String NUMBER_OF_APPS = "chainapps.count";
    
    static CountDownLatch appCount;
    static List<ServiceRegistration<?>> regs = new ArrayList<>();
    static int total = Integer.getInteger(NUMBER_OF_APPS, 20);
    static String resBaseName = "chain_element";
    static long start = -1;
    static Logger logger = LoggerFactory.getLogger(ChainOfIrresponsibility.class);

    final int number;
    ResourceListener listener;
    TimeResource res;
    
    public ChainOfIrresponsibility(){
        number = -1;
    }
    
    ChainOfIrresponsibility(int number){
        this.number = number;
    }
    
    protected void activate(ComponentContext ctx){
        appCount = new CountDownLatch(total);
        for (int i = 0; i < total; i++){
            regs.add(ctx.getBundleContext().registerService(Application.class, new ChainOfIrresponsibility(i), null));
        }
        logger.info("registered {} apps", total);
    }
    
    protected void deactivate(ComponentContext ctx){
        for (ServiceRegistration<?> r: regs){
            r.unregister();
        }
    }

    @Override
    public void start(final ApplicationManager appManager) {
        
        res = appManager.getResourceManagement().createResource(resBaseName+number, TimeResource.class);
        final String next = resBaseName + ((number+1)%total);
        
        final FloatResource perf = appManager.getResourceManagement().createResource(resBaseName + "_perf", FloatResource.class);
        
        listener = new ResourceListener() {

            @Override
            public void resourceChanged(Resource resource) {
                logger.trace("{} is now {}", res.getName(), res.getValue());
                TimeResource r = (TimeResource) appManager.getResourceAccess().getResource(next);
                r.setValue(res.getValue()+1);
                if (number == 0){
                    perf.setValue(((float)res.getValue())/(System.currentTimeMillis()-start));
                }
            }

            @Override
            public String toString() {
                return "Chain listener #" + number;
            }

        };
        
        res.addResourceListener(listener, true);
        res.activate(true);
        appCount.countDown();
        
        if (number == 0){
            setupStartThread();
            
            TimerListener tl = new TimerListener() {

                @Override
                public void timerElapsed(Timer timer) {
                    logger.debug("{} chain apps, {} apps processed per ms", total, perf.getValue());
                }
            };
            appManager.createTimer(30000, tl);
        }
    }

    @Override
    public void stop(AppStopReason reason) {
        res.removeResourceListener(listener);
    }
    
    void setupStartThread(){
        Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (appCount.await(60, TimeUnit.SECONDS)){
                            logger.info("{} apps started", total);
                            start = System.currentTimeMillis();
                            res.setValue(0);
                        } else {
                            logger.error("timeout while waiting for application start.");
                        }
                    } catch (InterruptedException ex) {
                        logger.error("interrupted!", ex);
                    }
                }
            };
            new Thread(run).start();
    }
    
}
