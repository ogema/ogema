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
package org.ogema.rest.patternmimic;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

// what's the point of this?
@XmlRegistry
public class ObjectFactory {

	public FakePattern createFakePattern() {
		return new FakePattern();
	}
	
	public  PatternMatch createPatternMatch() {
		return new PatternMatch();
	}
	
	public PatternMatchList createPatternMatchList() {
		return new PatternMatchList();
	}
	
	public ResourceProxy createResourceProxy() {
		return new ResourceProxy();
	}
	
	@XmlElementDecl(namespace = FakePattern.NS_OGEMA_REST_PATTERN, name = "pattern")
	public JAXBElement<FakePattern> createFakePattern(FakePattern value) {
		return new JAXBElement<FakePattern>( new QName(FakePattern.NS_OGEMA_REST_PATTERN, "pattern"), FakePattern.class, null, value);
	}
	
	@XmlElementDecl(namespace = FakePattern.NS_OGEMA_REST_PATTERN, name = "patternMatchList")
	public JAXBElement<PatternMatchList> createPatternMatchList(PatternMatchList value) {
		return new JAXBElement<PatternMatchList>(new QName(FakePattern.NS_OGEMA_REST_PATTERN, "patternMatchList"), PatternMatchList.class, null, value);
	}
	
	@XmlElementDecl(namespace = FakePattern.NS_OGEMA_REST_PATTERN, name = "match")
	public JAXBElement<PatternMatch> createPatternMatch(PatternMatch value) {
		return new JAXBElement<PatternMatch>(new QName(FakePattern.NS_OGEMA_REST_PATTERN, "match"), PatternMatch.class, null, value);
	}
	
}
