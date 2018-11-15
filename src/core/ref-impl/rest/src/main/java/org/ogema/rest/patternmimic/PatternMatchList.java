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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatternMatchList", propOrder={ "nrMatches", "matchesStart", "matchesEnd", "matches" }, namespace = FakePattern.NS_OGEMA_REST_PATTERN )
@XmlSeeAlso( { PatternMatch.class} )
@XmlRootElement(name = "patternMatchList", namespace = FakePattern.NS_OGEMA_REST_PATTERN)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class PatternMatchList {

	@XmlElement(name="nrMatches")
	protected int nrMatches;
	
	@XmlElement(name="matchesStart")
	protected int matchesStart = -1;
	
	@XmlElement(name="matchesEnd")
	protected int matchesEnd = -1;
	
	//generates a <matches>...</matches> wrapper around matches; not required, though, since Jackson adds this wrapper
	// anyway for JSON, and for XML a wrapper is not necessary
//	@XmlElementWrapper(name="matches")   
	@XmlElements(value = {
	        @XmlElement(name = "match", type = PatternMatch.class)} )
	protected List<PatternMatch> matches;
	
	public PatternMatchList() {
		// TODO Auto-generated constructor stub
	}

	public List<PatternMatch> getMatches() {
		return matches;
	}

	public void setMatches(List<PatternMatch> matches) {
		this.matches = matches;
	}
	
	
	
}
