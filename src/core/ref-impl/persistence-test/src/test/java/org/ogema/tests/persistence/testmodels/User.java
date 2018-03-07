package org.ogema.tests.persistence.testmodels;


import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;

public interface User extends Resource {
	StringResource pwd();

	IntegerResource status();

	ResourceList<StringResource> gateways();

	StringArrayResource userGWIds();

	IntegerResource minmalCriticalityLevelAccepted();
}
