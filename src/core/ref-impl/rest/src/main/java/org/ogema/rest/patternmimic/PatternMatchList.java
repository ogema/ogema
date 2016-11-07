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
