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
