package org.ogema.tests.persistence.testmodels;


import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;
import org.ogema.tests.persistence.testmodels.User;

/**
 * Central data storage
 */
public interface DataCollection extends Configuration {
	ResourceList<StringResource> apps();

	ResourceList<StringResource> categoryList();

	ResourceList<User> users();

	ResourceList<StringResource> organizations();

	ResourceList<StringResource> appGroups();

	StringResource genericIconDirectory();
}
