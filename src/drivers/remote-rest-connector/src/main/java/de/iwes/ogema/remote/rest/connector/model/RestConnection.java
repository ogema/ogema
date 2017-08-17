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
package de.iwes.ogema.remote.rest.connector.model;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;

import de.iwes.ogema.remote.rest.connector.RemoteRestConnector;

/**
 * Resource describing a connection to a remote OGEMA resource via REST,
 * should be added as decorator to the resource which shall be transmitted to a remote
 * OGEMA instance (push) or that shall be updated from the remote OGEMA instance (pull).<br>
 * 
 * RestConnection resources are used by {@link RemoteRestConnector}.<br>
 * In order to pull a resource from remote, add the {@link #pullConfig()}
 * subresource. In order to push to remote, add the {@link #pushConfig()}
 * resource. If not the whole target resource shall be synchronized, use
 * the configurations for individual subresources, {@link #individualPullConfigs()}
 * and {@link #individualPushConfigs()}. This way, it is even possible to
 * configure some subresources for pulling, and others for pushing.<br>
 * 
 * If you need to pull a resource from remote which may not exists initially,
 * set {@link RestPullConfig#pushOnInit()} to true.
 * 
 * @author jlapp
 */
public interface RestConnection extends Resource {
    
    /**
     * REST URL of the remote resource. This has to be the full URL that would
     * would be used to access the remote OGEMA instance's REST interface directly.
     * 
     * @return REST URL of the resource that shall be mapped to this resource's parent.
     */
    StringResource remotePath();
    
    /**
     * @return push local changes to the remote resource?
     * @deprecated set global push configuration instead: {@link #pushConfig()}.
     */
    @Deprecated
    BooleanResource push();
    
    /**
     * Create this subresource in order to pull the entire target resource. The periodicity is determined by {@link #pollingInterval()}.
     * Use {@link #individualPullConfigs()} instead, in order to pull only selected subresources.
     * 
     * @return
     */
    RestPullConfig pullConfig();
    
    /**
     * Create this subresource in order to push the entire target resource. If {@link #pushInterval()} is
     * not set, a recursive structure listener will be used on the target resource to identify changes and push them to the server,
     * if <tt>pushInterval</tt> is set, a push will be done periodically.  <br>
     * Use {@link #individualPushConfigs()} instead to push only selected subresources.
     * @return
     */
    RestPushConfig pushConfig();
    
    /**
     * Pull individual subresources of {@link #remotePath()} from the server.
     * Note: if {@link #pullConfig()} is set, then this configuration will be ignored (all subresources will be pulled anyway).
     * 
     * @return
     */
    ResourceList<RestPullConfig> individualPullConfigs();
    
    /**
     * Push individual subresources of {@link #remotePath()} from the server.
     * Note: if {@link #pushConfig()} is set, then this configuration will be ignored (all subresources will be pushled anyway).
     * 
     * @return
     */
    ResourceList<RestPushConfig> individualPushConfigs();
    
    /**
     * @return push when a value of a subresource changes? This is only evaluated if {@link #push()} is true.
     * 
     * @deprecated moved to {@link RestPushConfig}
     */
    @Deprecated
    BooleanResource pushOnSubresourceChanged();
    
    /**
     * Update interval for pull configurations.  This configuration is not
     * part of the {@link RestPullConfig}, because it can only be set globally for the whole configuration.
     * @return interval at which to update the parent resource from the remote resource.
     * Time unit: ms
     * @deprecated will be replaced by a TimeResource
     */
    @Deprecated
    IntegerResource pollingInterval();
    
    /**
     * If this resource is present/active no listener on the resources is registered, but pushing is done
     * like pulling with a fixed update rate. This should be specified in case a lot of changes occur in the
     * resource and sending all changes at once would cause too much traffic. This configuration is not
     * part of the {@link RestPushConfig}, because it can only be set globally for the whole configuration.
     */
    TimeResource pushInterval();
    
    /**
     * If push is done based on a fixed interval additional pushs can be triggered by setting run to true
      */
    BooleanResource triggerPush();
    /**
     * If pull is done based on a fixed interval additional pulls can be triggered by setting run to true
      */
    BooleanResource triggerPull();
    
    /** 
     * Call actions registered here before performing a push (allows applications to update resources). For
     * compatibility reasons we just give references to the stateControl resources here.
     *
     * @deprecated register your own timer instead, and periodically set {@link #run()} (which might get renamed) to true 
     */
    @Deprecated 
    ResourceList<BooleanResource> callBeforePush();
    
    /**
     * Write new value even if it does not differ from old one? 
     * (which may trigger {@see ResourceValueListener} callbacks)
     * @return
     * 
     * @deprecated use {@link RestPullConfig#forceWrite()} instead
     */
    @Deprecated
    BooleanResource forceWrite();
    
    /**
     * remote REST user
     * @return
     */
    StringResource remoteUser();
    
    /**
     * Remote REST user pw
     * @return
     */
    // TODO do not store in visible resource
    StringResource remotePw();
    
    /** 
     * see consecutiveErrorCounter, but reverse
     */
    IntegerResource consecutiveSuccessfulPushCounter();
    IntegerResource consecutiveSuccessfulPullCounter();
    
    /**
     * This counter is reset when a successful transfer is performed, afterwards the counter
     * is increased for each unsuccesful conntection/transfer trial
     */
    IntegerResource consecutiveErrorPushCounter();
    IntegerResource consecutiveErrorPullCounter();
    
    /**
     * If this resource list is present, not the whole target resource is pulled, but only individual subresources,
     * which are configured here.
     * If this resource list is not present/active, then the entire target resource is pulled with standard depth
     * @deprecated use {@link #individualPullConfigs()} instead, and {@link #pullConfig()} for a global configuration
     */
    @Deprecated
    ResourceList<RestPullConfig> pullConfigs();
    
}
