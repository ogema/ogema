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
package org.ogema.resourcemanager.impl.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.resourcemanager.impl.ResourceDBManager;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.ArrayTimeSeries;
import org.slf4j.Logger;

/**
 * Implementation for the Transaction interface. The implementation shall work
 * against the API as much as possible, not needing any methods defined in only
 * the implementation. A necessary exception to this is that the transaction
 * must be able to lock and unlock the resource graph for reading, writing,
 * activating and deactivating.
 *
 * FIXME since this also supports virtual resources, the casting to the target
 * type should also be checked at the time of reading/writing, and a suitable
 * error handling for this is required.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@SuppressWarnings("deprecation")
public class TransactionImpl implements org.ogema.core.resourcemanager.Transaction {

    @SuppressWarnings("unused")
    private final Logger m_logger;
    private final ResourceDBManager m_dbMan;
    private final ApplicationManager m_appMan;

    // Maps between resources containing a value and their to-write and last-read values.
    private final Map<String, RwPair<Boolean>> m_boolMap = new HashMap<>();
    private final Map<String, RwPair<Float>> m_floatMap = new HashMap<>();
    private final Map<String, RwPair<Integer>> m_intMap = new HashMap<>();
    private final Map<String, RwPair<String>> m_stringMap = new HashMap<>();
    private final Map<String, RwPair<Long>> m_timeMap = new HashMap<>();
    private final Map<String, RwPair<byte[]>> m_byteArrayMap = new HashMap<>();
    private final Map<String, RwPair<float[]>> m_floatArrayMap = new HashMap<>();
    private final Map<String, RwPair<int[]>> m_intArrayMap = new HashMap<>();
    private final Map<String, RwPair<boolean[]>> m_booleanArrayMap = new HashMap<>();
    private final Map<String, RwPair<String[]>> m_stringArrayMap = new HashMap<>();
    private final Map<String, RwPair<long[]>> m_longArrayMap = new HashMap<>();
    private final Map<String, RwPair<MemoryTimeSeries>> m_scheduleMap = new HashMap<>();

    // List of resources that do not contain a value (used in activation/de-activation)
    private final Set<String> m_complexResources = new HashSet<>();

    public TransactionImpl(ResourceDBManager dbMan, ApplicationManager appMan) {
        m_dbMan = dbMan;
        m_appMan = appMan;
        m_logger = org.slf4j.LoggerFactory.getLogger("Transaction-" + appMan.getAppID());

    }

    private Collection<String> getAllPathsWithValues() {
        final Collection<String> result = new ArrayList<>();
        result.addAll(m_boolMap.keySet());
        result.addAll(m_floatMap.keySet());
        result.addAll(m_intMap.keySet());
        result.addAll(m_stringMap.keySet());
        result.addAll(m_timeMap.keySet());
        result.addAll(m_byteArrayMap.keySet());
        result.addAll(m_floatArrayMap.keySet());
        result.addAll(m_intArrayMap.keySet());
        result.addAll(m_booleanArrayMap.keySet());
        result.addAll(m_stringArrayMap.keySet());
        result.addAll(m_longArrayMap.keySet());
        result.addAll(m_scheduleMap.keySet());
        return result;
    }

    private Collection<String> getAllPaths() {
        final Collection<String> result = getAllPathsWithValues();
        result.addAll(m_complexResources);
        return result;
    }

    @Override
    public void addTree(Resource rootResource, boolean addReferencedSubresources) {
        final List<Resource> resources;
        if (addReferencedSubresources) {
            resources = rootResource.getSubResources(true);
        } else {
            resources = rootResource.getDirectSubResources(true);
        }
        resources.add(rootResource);
        addResources(resources);
    }

    @Override
    public void addResource(Resource resource) {
        final Class<? extends Resource> resType = resource.getResourceType();
        final String path = resource.getPath();

        if (SingleValueResource.class.isAssignableFrom(resType)) {
            if (BooleanResource.class.isAssignableFrom(resType)) {
                m_boolMap.put(path, new RwPair<Boolean>());
                return;
            }
            if (FloatResource.class.isAssignableFrom(resType)) {
                m_floatMap.put(path, new RwPair<Float>());
                return;
            }
            if (IntegerResource.class.isAssignableFrom(resType)) {
                m_intMap.put(path, new RwPair<Integer>());
                return;
            }
            if (StringResource.class.isAssignableFrom(resType)) {
                m_stringMap.put(path, new RwPair<String>());
                return;
            }
            if (TimeResource.class.isAssignableFrom(resType)) {
                m_timeMap.put(path, new RwPair<Long>());
                return;
            }
            throw new UnsupportedOperationException("Cannot handle SingleValueResource of type " + resType.getCanonicalName());
        }

        if (ArrayResource.class.isAssignableFrom(resType)) {
            if (ByteArrayResource.class.isAssignableFrom(resType)) {
                m_byteArrayMap.put(path, new RwPair<byte[]>());
                return;
            }
            if (FloatArrayResource.class.isAssignableFrom(resType)) {
                m_floatArrayMap.put(path, new RwPair<float[]>());
                return;
            }
            if (IntegerArrayResource.class.isAssignableFrom(resType)) {
                m_intArrayMap.put(path, new RwPair<int[]>());
                return;
            }
            if (BooleanArrayResource.class.isAssignableFrom(resType)) {
                m_booleanArrayMap.put(path, new RwPair<boolean[]>());
                return;
            }
            if (StringArrayResource.class.isAssignableFrom(resType)) {
                m_stringArrayMap.put(path, new RwPair<String[]>());
                return;
            }
            if (TimeArrayResource.class.isAssignableFrom(resType)) {
                m_longArrayMap.put(path, new RwPair<long[]>());
                return;
            }

            // legacy support for deprecated types
            if (org.ogema.core.model.simple.OpaqueResource.class.isAssignableFrom(resType)) {
                m_byteArrayMap.put(path, new RwPair<byte[]>());
                return;
            }

            throw new UnsupportedOperationException("Cannot handle ArrayResource of type " + resType.getCanonicalName());
        }

        // schedules
        if (Schedule.class.isAssignableFrom(resType)) {
            m_scheduleMap.put(path, new RwPair<MemoryTimeSeries>());
            return;
        }

        // neither SingleValueResource, nor ArrayResource, not Schedule: Assume complex resource
        m_complexResources.add(path);
    }

    @Override
    public void addResources(Collection<Resource> resources) {
        for (Resource resource : resources) {
            addResource(resource);
        }
    }

    @Override
    public Collection<Resource> getResources() {
        final ResourceAccess resAcc = m_appMan.getResourceAccess();
        final Collection<String> paths = getAllPaths();
        final List<Resource> result = new ArrayList<>(paths.size());
        for (String path : paths) {
            result.add(resAcc.getResource(path));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> Collection<T> getResources(Class<T> resourceType) {
        final Collection<Resource> all = getResources();
        final Collection<T> result = new ArrayList<>();
        for (Resource resource : all) {
            if (resourceType.isAssignableFrom(resource.getResourceType())) {
                result.add((T) resource);
            }
        }
        return result;
    }

    /**
     * Remove all entries of resource from the map for which the paths fits.
     */
    private <T> void removeByPath(Resource resource, Map<String, RwPair<T>> map) {
        final ResourceAccess resAcc = m_appMan.getResourceAccess();
        final Set<String> paths = map.keySet();
        for (String path : paths) {
            final Resource entry = resAcc.getResource(path);
            if (resource.equalsPath(entry)) {
                map.remove(path);
            }
        }
    }

    /**
     * Remove all entries of resource from the map for which the location fits.
     */
    private <T> void removeByLocation(Resource resource, Map<String, RwPair<T>> map) {
        final ResourceAccess resAcc = m_appMan.getResourceAccess();
        final Set<String> paths = map.keySet();
        for (String path : paths) {
            final Resource entry = resAcc.getResource(path);
            if (resource.equalsLocation(entry)) {
                map.remove(path);
            }
        }
    }

    /**
     * Remove the resource from all maps if it has the same path as a map entry.
     */
    private void removeByPath(Resource resource) {
        removeByPath(resource, m_boolMap);
        removeByPath(resource, m_floatMap);
        removeByPath(resource, m_intMap);
        removeByPath(resource, m_byteArrayMap);
        removeByPath(resource, m_stringMap);
        removeByPath(resource, m_timeMap);
        removeByPath(resource, m_booleanArrayMap);
        removeByPath(resource, m_floatArrayMap);
        removeByPath(resource, m_intArrayMap);
        removeByPath(resource, m_stringArrayMap);
        removeByPath(resource, m_longArrayMap);
        removeByPath(resource, m_scheduleMap);
        m_complexResources.remove(resource.getPath());
    }

    /**
     * Remove the resource from all maps if it has the same path as a map entry.
     */
    private void removeByLocation(Resource resource) {
        removeByLocation(resource, m_boolMap);
        removeByLocation(resource, m_floatMap);
        removeByLocation(resource, m_intMap);
        removeByLocation(resource, m_byteArrayMap);
        removeByLocation(resource, m_stringMap);
        removeByLocation(resource, m_timeMap);
        removeByLocation(resource, m_booleanArrayMap);
        removeByLocation(resource, m_floatArrayMap);
        removeByLocation(resource, m_intArrayMap);
        removeByLocation(resource, m_stringArrayMap);
        removeByLocation(resource, m_longArrayMap);
        removeByLocation(resource, m_scheduleMap);

        // treatment for complex resources
        final ResourceAccess resAcc = m_appMan.getResourceAccess();
        final Iterator<String> iter = m_complexResources.iterator();
        while (iter.hasNext()) {
            final String path = iter.next();
            final Resource existingResource = resAcc.getResource(path);
            if (existingResource.equalsLocation(resource)) {
                iter.remove();
            }
        }
    }

    @Override
    public void removeResources(Collection<? extends Resource> resources) {
        for (Resource resource : resources) {
            removeByPath(resource);
        }
    }

    @Override
    public void removeResourcesByLocation(Collection<? extends Resource> resources) {
        for (Resource resource : resources) {
            removeByLocation(resource);
        }
    }

    // ---- Getters ----
    /**
     * Tries to find the entry for the resource in the map and returns its
     * associated read value, if possible. Throws a NoSuchResourcException, if
     * the resource could not be found in the application. This is the generic
     * version of the typed get<Type> methods.
     */
    private static <T> T getReadEntry(Resource resource, Map<String, RwPair<T>> map) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!map.containsKey(path)) {
            throw new NoSuchResourceException("Can not get a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it.");
        }
        final RwPair<T> pair = map.get(path);
        return pair.read;
    }

    @Override
    public Float getFloat(FloatResource resource) throws NoSuchResourceException {
        return getReadEntry(resource, m_floatMap);
    }

    @Override
    public Integer getInteger(IntegerResource resource) throws NoSuchResourceException {
        return getReadEntry(resource, m_intMap);
    }

    @Override
    public Boolean getBoolean(BooleanResource resource) throws NoSuchResourceException {
        return getReadEntry(resource, m_boolMap);
    }

    @Override
    public String getString(StringResource resource) throws NoSuchResourceException {
        return getReadEntry(resource, m_stringMap);
    }

    @Override
    public Long getTime(TimeResource resource) throws NoSuchResourceException {
        return getReadEntry(resource, m_timeMap);
    }

    @Override
    @Deprecated
    public byte[] getByteArray(org.ogema.core.model.simple.OpaqueResource resource) throws NoSuchResourceException {
        final byte[] values = getReadEntry(resource, m_byteArrayMap);
        return (values != null) ? values.clone() : null;
    }

    @Override
    public byte[] getByteArray(ByteArrayResource resource) throws NoSuchResourceException {
        final byte[] values = getReadEntry(resource, m_byteArrayMap);
        return (values != null) ? values.clone() : null;
    }

    @Override
    public float[] getFloatArray(FloatArrayResource resource) throws NoSuchResourceException {
        final float[] values = getReadEntry(resource, m_floatArrayMap);
        return (values != null) ? values.clone() : null;
    }

    @Override
    public int[] getIntegerArray(IntegerArrayResource resource) throws NoSuchResourceException {
        final int[] values = getReadEntry(resource, m_intArrayMap);
        return (values != null) ? values.clone() : null;
    }

    @Override
    public boolean[] getBooleanArray(BooleanArrayResource resource) throws NoSuchResourceException {
        final boolean[] values = getReadEntry(resource, m_booleanArrayMap);
        return (values != null) ? values.clone() : null;
    }

    @Override
    public long[] getTimeArray(TimeArrayResource resource) throws NoSuchResourceException {
        final long[] values = getReadEntry(resource, m_longArrayMap);
        return (values != null) ? values.clone() : null;
    }

    @Override
    public String[] getStringArray(StringArrayResource resource) throws NoSuchResourceException {
        final String[] values = getReadEntry(resource, m_stringArrayMap);
        return (values != null) ? values.clone() : null;
    }

    // ---- Setters ----
    @Override
    public void setFloat(FloatResource resource, float value) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_floatMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<Float> pair = m_floatMap.get(path);
        pair.write = value;
    }

    @Override
    public void setInteger(IntegerResource resource, int value) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_intMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<Integer> pair = m_intMap.get(path);
        pair.write = value;
    }

    @Override
    public void setBoolean(BooleanResource resource, boolean value) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_boolMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<Boolean> pair = m_boolMap.get(path);
        pair.write = value;
    }

    @Override
    public void setString(StringResource resource, String value) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_stringMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<String> pair = m_stringMap.get(path);
        pair.write = value;
    }

    @Override
    public void setTime(TimeResource resource, long value) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_timeMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<Long> pair = m_timeMap.get(path);
        pair.write = value;
    }

    @Override
    @Deprecated
    public void setByteArray(org.ogema.core.model.simple.OpaqueResource resource, byte[] values) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_byteArrayMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<byte[]> pair = m_byteArrayMap.get(path);
        pair.write = values;
    }

    @Override
    public void setByteArray(ByteArrayResource resource, byte[] values) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_byteArrayMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<byte[]> pair = m_byteArrayMap.get(path);
        pair.write = values;
    }

    @Override
    public void setFloatArray(FloatArrayResource resource, float[] values) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_floatArrayMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<float[]> pair = m_floatArrayMap.get(path);
        pair.write = values;
    }

    @Override
    public void setIntegerArray(IntegerArrayResource resource, int[] values) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_intArrayMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<int[]> pair = m_intArrayMap.get(path);
        pair.write = values;
    }

    @Override
    public void setBooleanArray(BooleanArrayResource resource, boolean[] values) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_booleanArrayMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<boolean[]> pair = m_booleanArrayMap.get(path);
        pair.write = values;
    }

    @Override
    public void setStringArray(StringArrayResource resource, String[] values) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_stringArrayMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<String[]> pair = m_stringArrayMap.get(path);
        pair.write = values;
    }

    @Override
    public void setTimeArray(TimeArrayResource resource, long[] values) throws NoSuchResourceException {
        final String path = resource.getPath();
        if (!m_longArrayMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for resource " + resource.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<long[]> pair = m_longArrayMap.get(path);
        pair.write = values;
    }

    // ---- Read and Write ----
    @Override
    public void read() {
        final ResourceAccess resAcc = m_appMan.getResourceAccess();
        // start transaction: lock resource graph.
        m_dbMan.lockStructureRead();
        m_dbMan.lockRead();
        try {
            for (String path : m_boolMap.keySet()) {
                final BooleanResource resource = (BooleanResource) resAcc.getResource(path);
                final Boolean value = (resource.exists()) ? resource.getValue() : null;
                final RwPair<Boolean> pair = m_boolMap.get(path);
                pair.read = value;
            }
            for (String path : m_floatMap.keySet()) {
                final FloatResource resource = (FloatResource) resAcc.getResource(path);
                final Float value = (resource.exists()) ? resource.getValue() : null;
                final RwPair<Float> pair = m_floatMap.get(path);
                pair.read = value;
            }
            for (String path : m_intMap.keySet()) {
                final IntegerResource resource = (IntegerResource) resAcc.getResource(path);
                final Integer value = (resource.exists()) ? resource.getValue() : null;
                final RwPair<Integer> pair = m_intMap.get(path);
                pair.read = value;
            }
            for (String path : m_byteArrayMap.keySet()) {
                final Resource untypedResource = resAcc.getResource(path);
                if (untypedResource instanceof ByteArrayResource) {
                    final ByteArrayResource resource = (ByteArrayResource) untypedResource;
                    final byte[] value = (resource.exists()) ? resource.getValues() : null;
                    final RwPair<byte[]> pair = m_byteArrayMap.get(path);
                    pair.read = value;
                } else if (untypedResource instanceof org.ogema.core.model.simple.OpaqueResource) { // case for deprecated OpaqueResources
                    final org.ogema.core.model.simple.OpaqueResource resource = (org.ogema.core.model.simple.OpaqueResource) untypedResource;
                    final byte[] value = (resource.exists()) ? resource.getValue() : null;
                    final RwPair<byte[]> pair = m_byteArrayMap.get(path);
                    pair.read = value;
                }
            }
            for (String path : m_stringMap.keySet()) {
                final StringResource resource = (StringResource) resAcc.getResource(path);
                final String value = (resource.exists()) ? resource.getValue() : null;
                final RwPair<String> pair = m_stringMap.get(path);
                pair.read = value;
            }
            for (String path : m_timeMap.keySet()) {
                final TimeResource resource = (TimeResource) resAcc.getResource(path);
                final Long value = (resource.exists()) ? resource.getValue() : null;
                final RwPair<Long> pair = m_timeMap.get(path);
                pair.read = value;
            }

            // arrays
            for (String path : m_floatArrayMap.keySet()) {
                final FloatArrayResource resource = (FloatArrayResource) resAcc.getResource(path);
                final float[] value = (resource.exists()) ? resource.getValues() : null;
                final RwPair<float[]> pair = m_floatArrayMap.get(path);
                pair.read = value;
            }
            for (String path : m_intArrayMap.keySet()) {
                final IntegerArrayResource resource = (IntegerArrayResource) resAcc.getResource(path);
                final int[] value = (resource.exists()) ? resource.getValues() : null;
                final RwPair<int[]> pair = m_intArrayMap.get(path);
                pair.read = value;
            }
            for (String path : m_booleanArrayMap.keySet()) {
                final BooleanArrayResource resource = (BooleanArrayResource) resAcc.getResource(path);
                final boolean[] value = (resource.exists()) ? resource.getValues() : null;
                final RwPair<boolean[]> pair = m_booleanArrayMap.get(path);
                pair.read = value;
            }
            for (String path : m_stringArrayMap.keySet()) {
                final StringArrayResource resource = (StringArrayResource) resAcc.getResource(path);
                final String[] value = (resource.exists()) ? resource.getValues() : null;
                final RwPair<String[]> pair = m_stringArrayMap.get(path);
                pair.read = value;
            }
            for (String path : m_longArrayMap.keySet()) {
                final TimeArrayResource resource = (TimeArrayResource) resAcc.getResource(path);
                final long[] value = (resource.exists()) ? resource.getValues() : null;
                final RwPair<long[]> pair = m_longArrayMap.get(path);
                pair.read = value;
            }

            readSchedules(resAcc);
        } finally {
            // transaction finished: unlock the graph.
            m_dbMan.unlockRead();
            m_dbMan.unlockStructureRead();
        }
    }

    @Override
    public void write() throws VirtualResourceException {
        final ResourceAccess resAcc = m_appMan.getResourceAccess();
        // start transaction: lock resource graph.
        m_dbMan.lockWrite();
        m_dbMan.startTransaction();
        try {
            // check that all resources exist, throw an exception if not.
            for (Resource resource : getResources()) {
                if (!resource.exists()) {
                    throw new VirtualResourceException("Could not perform write transaction: Target resource at " + resource.getPath() + " does not exist.");
                }
            }

            // simple resources
            for (String path : m_boolMap.keySet()) {
                final BooleanResource resource = (BooleanResource) resAcc.getResource(path);
                final RwPair<Boolean> pair = m_boolMap.get(path);
                final Boolean value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValue();
                } else {
                    resource.setValue(value);
                    pair.read = resource.getValue();
                }
            }
            for (String path : m_floatMap.keySet()) {
                final FloatResource resource = (FloatResource) resAcc.getResource(path);
                final RwPair<Float> pair = m_floatMap.get(path);
                final Float value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValue();
                } else {
                    resource.setValue(value);
                    pair.read = resource.getValue();
                }
            }
            for (String path : m_intMap.keySet()) {
                final IntegerResource resource = (IntegerResource) resAcc.getResource(path);
                final RwPair<Integer> pair = m_intMap.get(path);
                final Integer value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValue();
                } else {
                    resource.setValue(value);
                    pair.read = resource.getValue();
                }
            }
            for (String path : m_byteArrayMap.keySet()) {
                final Resource untypedResource = resAcc.getResource(path);
                if (untypedResource instanceof ByteArrayResource) {
                    final ByteArrayResource resource = (ByteArrayResource) untypedResource;
                    final RwPair<byte[]> pair = m_byteArrayMap.get(path);
                    final byte[] value = pair.write;
                    if (!resource.exists()) {
                        pair.read = null;
                    } else if (value == null) {
                        pair.read = resource.getValues();
                    } else {
                        resource.setValues(value);
                        pair.read = resource.getValues();
                    }
                } else if (untypedResource instanceof org.ogema.core.model.simple.OpaqueResource) {
                    final org.ogema.core.model.simple.OpaqueResource resource = (org.ogema.core.model.simple.OpaqueResource) untypedResource;
                    final RwPair<byte[]> pair = m_byteArrayMap.get(path);
                    final byte[] value = pair.write;
                    if (!resource.exists()) {
                        pair.read = null;
                    } else if (value == null) {
                        pair.read = resource.getValue();
                    } else {
                        resource.setValue(value);
                        pair.read = resource.getValue();
                    }
                }
            }
            for (String path : m_stringMap.keySet()) {
                final StringResource resource = (StringResource) resAcc.getResource(path);
                final RwPair<String> pair = m_stringMap.get(path);
                final String value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValue();
                } else {
                    resource.setValue(value);
                    pair.read = resource.getValue();
                }
            }
            for (String path : m_timeMap.keySet()) {
                final TimeResource resource = (TimeResource) resAcc.getResource(path);
                final RwPair<Long> pair = m_timeMap.get(path);
                final Long value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValue();
                } else {
                    resource.setValue(value);
                    pair.read = resource.getValue();
                }
            }

            // arrays
            for (String path : m_booleanArrayMap.keySet()) {
                final BooleanArrayResource resource = (BooleanArrayResource) resAcc.getResource(path);
                final RwPair<boolean[]> pair = m_booleanArrayMap.get(path);
                final boolean[] value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValues();
                } else {
                    resource.setValues(value);
                    pair.read = resource.getValues();
                }
            }
            for (String path : m_floatArrayMap.keySet()) {
                final FloatArrayResource resource = (FloatArrayResource) resAcc.getResource(path);
                final RwPair<float[]> pair = m_floatArrayMap.get(path);
                final float[] value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValues();
                } else {
                    resource.setValues(value);
                    pair.read = resource.getValues();
                }
            }
            for (String path : m_intArrayMap.keySet()) {
                final IntegerArrayResource resource = (IntegerArrayResource) resAcc.getResource(path);
                final RwPair<int[]> pair = m_intArrayMap.get(path);
                final int[] value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValues();
                } else {
                    resource.setValues(value);
                    pair.read = resource.getValues();
                }
            }
            for (String path : m_stringArrayMap.keySet()) {
                final StringArrayResource resource = (StringArrayResource) resAcc.getResource(path);
                final RwPair<String[]> pair = m_stringArrayMap.get(path);
                final String[] value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValues();
                } else {
                    resource.setValues(value);
                    pair.read = resource.getValues();
                }
            }
            for (String path : m_longArrayMap.keySet()) {
                final TimeArrayResource resource = (TimeArrayResource) resAcc.getResource(path);
                final RwPair<long[]> pair = m_longArrayMap.get(path);
                final long[] value = pair.write;
                if (!resource.exists()) {
                    pair.read = null;
                } else if (value == null) {
                    pair.read = resource.getValues();
                } else {
                    resource.setValues(value);
                    pair.read = resource.getValues();
                }
            }
            writeSchedules(resAcc);
        } finally {
        	m_dbMan.finishTransaction();
            // transaction finished: unlock resource graph
            m_dbMan.unlockWrite();
        }
    }

    @Override
    public void activate() {
        final Collection<Resource> resources = getResources();
        m_dbMan.lockStructureWrite();
        m_dbMan.startTransaction();
        try {
            for (Resource resource : resources) {
                resource.activate(false);
            }
        } finally {
        	m_dbMan.finishTransaction();
            m_dbMan.unlockStructureWrite();
        }
    }

    @Override
    public void deactivate() {
        final Collection<Resource> resources = getResources();
        m_dbMan.lockStructureWrite();
        m_dbMan.startTransaction();
        try {
            for (Resource resource : resources) {
                resource.deactivate(false);
            }
        } finally {
        	m_dbMan.finishTransaction();
            m_dbMan.unlockStructureWrite();
        }
    }

    /**
     * Gets the value type for a schedule by looking at its parent resource.
     */
    private static Class<? extends Value> getValueType(Schedule schedule) {
        final Resource parent = schedule.getParent();
        if (parent == null) {
            throw new RuntimeException("Schedule at path " + schedule.getPath() + " does not seem to have a parent. Cannot determine the type of elements. OGEMA schedules must always have a simple non-array parent resource.");
        }
        final Class<? extends Resource> resType = parent.getResourceType();
        if (FloatResource.class.isAssignableFrom(resType)) {
            return FloatValue.class;
        } else if (IntegerResource.class.isAssignableFrom(resType)) {
            return IntegerValue.class;
        } else if (BooleanResource.class.isAssignableFrom(resType)) {
            return BooleanValue.class;
        } else if (StringResource.class.isAssignableFrom(resType)) {
            return StringValue.class;
        } else if (TimeResource.class.isAssignableFrom(resType)) {
            return LongValue.class;
        } else {
            throw new RuntimeException("Parent of schedule at path " + schedule.getPath() + " is of type " + resType.getCanonicalName() + ", which is not a valid base type for a schedule resource (alternatively, if you see this message the OGEMA code may be bugged).");
        }
    }

    @Override
    public void setSchedule(Schedule schedule, ReadOnlyTimeSeries function) throws NoSuchResourceException {
        final String path = schedule.getPath();
        if (!m_scheduleMap.containsKey(path)) {
            throw new NoSuchResourceException("Can not set a value for schedule " + schedule.toString() + " in transaction: Resource had not been registered before. Be sure to register the resource to the transaction with addResource(...) before trying to set a value for it");
        }
        final RwPair<MemoryTimeSeries> pair = m_scheduleMap.get(path);
        final Class<? extends Value> valueType = getValueType(schedule);
        final MemoryTimeSeries state = new ArrayTimeSeries(function, valueType);
        pair.write = state;
    }

    @Override
    public ReadOnlyTimeSeries getSchedule(Schedule schedule) throws NoSuchResourceException {
        final MemoryTimeSeries data = getReadEntry(schedule, m_scheduleMap);
        return (data != null) ? data.clone() : null;
    }

    private void writeSchedules(ResourceAccess resAcc) {
        for (String path : m_scheduleMap.keySet()) {
            final Schedule schedule = (Schedule) resAcc.getResource(path);
            final RwPair<MemoryTimeSeries> pair = m_scheduleMap.get(path);
            final MemoryTimeSeries value = pair.write;
            if (!schedule.exists()) {
                pair.read = null;
            } else if (value == null) {
                if (pair.read == null) {
                    final Class<? extends Value> valueType = getValueType(schedule);
                    pair.read = new ArrayTimeSeries(valueType);
                }
                pair.read.read(schedule);
            } else {
                value.write(schedule);
                if (pair.read == null) {
                    final Class<? extends Value> valueType = getValueType(schedule);
                    pair.read = new ArrayTimeSeries(valueType);
                }
                pair.read.read(schedule);
            }
        }
    }

    private void readSchedules(ResourceAccess resAcc) {
        for (String path : m_scheduleMap.keySet()) {
            final Schedule schedule = (Schedule) resAcc.getResource(path);
            final RwPair<MemoryTimeSeries> pair = m_scheduleMap.get(path);
            if (!schedule.exists()) {
                pair.read = null;
                continue;
            }
            if (pair.read == null) {
                final Class<? extends Value> valueType = getValueType(schedule);
                pair.read = new ArrayTimeSeries(valueType);
            }
            pair.read.read(schedule);
        }
    }

}
