package de.fhg.iee.bacnet.tags;

import java.util.Collection;
import java.util.List;

public interface CompositeTagBase {
	int getOidInstanceNumber();
	int getOidType();
	default String description() {return null;}
	<T extends CompositeTagBase> Collection<T> getSubTags();
	default String getName() {return null;}
	default List<String> getStates() {return null;}
	default Boolean isSettable() {return null;}
	default Float minValue() {return null;}
	default Float maxValue() {return null;}
	default String unit() {return null;}
	default String objectTag() {return null;}
}
