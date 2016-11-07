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
package org.ogema.apps.graphgenerator.generators.visjs;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceGraphException;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.VirtualResourceException;

public class TopLevelDummyNode implements Resource {

	@Override
	public String getName() {
		return "Resources";
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public String getPath(String delimiter) {
		return null;
	}

	@Override
	public String getLocation() {
		return "Resources";
	}

	@Override
	public String getLocation(String delimiter) {
		return getLocation();
	}

	@Override
	public Class<? extends Resource> getResourceType() {
		return this.getClass();
	}

	@Override
	public void addValueListener(ResourceValueListener<?> listener, boolean callOnEveryUpdate) {
	}

	@Override
	public void addValueListener(ResourceValueListener<?> listener) {
	}

	@Override
	public boolean removeValueListener(ResourceValueListener<?> listener) {
		return false;
	}

	@Override
    @Deprecated
	public void addResourceListener(org.ogema.core.resourcemanager.ResourceListener listener, boolean recursive) {
	}

	@Override
    @Deprecated
	public boolean removeResourceListener(org.ogema.core.resourcemanager.ResourceListener listener) {
		return false;
	}

	@Override
	public void addAccessModeListener(AccessModeListener listener) {
	}

	@Override
	public boolean removeAccessModeListener(AccessModeListener listener) {
		return false;
	}

	@Override
	public void addStructureListener(ResourceStructureListener listener) {
	}

	@Override
	public boolean removeStructureListener(ResourceStructureListener listener) {
		return false;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean isTopLevel() {
		return false;
	}

	@Override
	public boolean isWriteable() {
		return false;
	}

	@Override
	public boolean isDecorator() {
		return false;
	}

	@Override
	public boolean requestAccessMode(AccessMode accessMode, AccessPriority priority) throws SecurityException {
		return false;
	}

	@Override
	public AccessPriority getAccessPriority() {
		return null;
	}

	@Override
	public AccessMode getAccessMode() {
		return null;
	}

	@Override
	public <T extends Resource> T getParent() {
		return null;
	}

	@Override
	public <T extends Resource> List<T> getReferencingResources(Class<T> parentType) {
		return null;
	}

	@Override
	public List<Resource> getSubResources(boolean recursive) {
		return null;
	}

	@Override
	public List<Resource> getDirectSubResources(boolean recursive) {
		return null;
	}

	@Override
	public boolean isReference(boolean recursive) {
		return false;
	}

	@Override
	public <T extends Resource> T getSubResource(String name) throws NoSuchResourceException {
		return null;
	}

	@Override
	public <T extends Resource> List<T> getSubResources(Class<T> resourceType, boolean recursive) {
		return null;
	}

	@Override
	public void activate(boolean recursive) throws SecurityException, VirtualResourceException {
	}

	@Override
	public void deactivate(boolean recursive) throws SecurityException {
	}

	@Override
	public void setOptionalElement(String name, Resource newElement) throws NoSuchResourceException,
			InvalidResourceTypeException, ResourceGraphException, VirtualResourceException {
	}

	@Override
	public Resource addOptionalElement(String name) throws NoSuchResourceException {
		return null;
	}

	@Override
	public <T extends Resource> T addDecorator(String name, Class<T> resourceType) throws NoSuchResourceException,
			ResourceAlreadyExistsException {
		return null;
	}

	@Override
	public <T extends Resource> T addDecorator(String name, T decoratingResource)
			throws ResourceAlreadyExistsException, NoSuchResourceException, ResourceGraphException,
			VirtualResourceException {
		return null;
	}

	@Override
	public void deleteElement(String name) {
	}

	@Override
	public boolean equalsLocation(Resource other) {
		return false;
	}

	@Override
	public boolean equalsPath(Resource other) {
		return false;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public <T extends Resource> T create() throws NoSuchResourceException {
		return null;
	}

	@Override
	public void delete() {
	}

	@Override
	public <T extends Resource> T setAsReference(T reference) throws NoSuchResourceException, ResourceGraphException,
			VirtualResourceException {
		return null;
	}

	@Override
	public <T extends Resource> T getSubResource(String name, Class<T> type) throws NoSuchResourceException {
		return null;
	}

	@Override
	public <T extends Resource> T getLocationResource() {
		return null;
	}

	@Override
	public List<Resource> getReferencingNodes(boolean transitive) {
		return null;
	}

}
