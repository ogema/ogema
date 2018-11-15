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
package org.ogema.apps.graphgenerator.generators.visjs;

public class Physics {

	private BarnesHut barnesHut = new BarnesHut();
	private Repulsion repulsion = new Repulsion();
	private HierarchicalRepulsion hierarchicalRepulsion = new HierarchicalRepulsion();

	public BarnesHut getBarnesHut() {
		return barnesHut;
	}

	public void setBarnesHut(BarnesHut barnesHut) {
		this.barnesHut = barnesHut;
	}

	public Repulsion getRepulsion() {
		return repulsion;
	}

	public void setRepulsion(Repulsion repulsion) {
		this.repulsion = repulsion;
	}

	public HierarchicalRepulsion getHierarchicalRepulsion() {
		return hierarchicalRepulsion;
	}

	public void setHierarchicalRepulsion(HierarchicalRepulsion hierarchicalRepulsion) {
		this.hierarchicalRepulsion = hierarchicalRepulsion;
	}

	class BarnesHut {
		private boolean enabled = true;
		private int gravitationalConstant = -2000;
		private float centralGravity = 0f;
		private int springLength = 95;
		private float springConstant = 0.04f;
		private float damping = 0f;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public int getGravitationalConstant() {
			return gravitationalConstant;
		}

		public void setGravitationalConstant(int gravitationalConstant) {
			this.gravitationalConstant = gravitationalConstant;
		}

		public float getCentralGravity() {
			return centralGravity;
		}

		public void setCentralGravity(float centralGravity) {
			this.centralGravity = centralGravity;
		}

		public int getSpringLength() {
			return springLength;
		}

		public void setSpringLength(int springLength) {
			this.springLength = springLength;
		}

		public float getSpringConstant() {
			return springConstant;
		}

		public void setSpringConstant(float springConstant) {
			this.springConstant = springConstant;
		}

		public float getDamping() {
			return damping;
		}

		public void setDamping(float damping) {
			this.damping = damping;
		}
	}

	class Repulsion {
		private float centralGravity = 0f;
		private int springLength = 50;
		private float springConstant = 0.05f;
		private int nodeDistance = 100;
		private float damping = 0f;

		public float getCentralGravity() {
			return centralGravity;
		}

		public void setCentralGravity(float centralGravity) {
			this.centralGravity = centralGravity;
		}

		public int getSpringLength() {
			return springLength;
		}

		public void setSpringLength(int springLength) {
			this.springLength = springLength;
		}

		public float getSpringConstant() {
			return springConstant;
		}

		public void setSpringConstant(float springConstant) {
			this.springConstant = springConstant;
		}

		public int getNodeDistance() {
			return nodeDistance;
		}

		public void setNodeDistance(int nodeDistance) {
			this.nodeDistance = nodeDistance;
		}

		public float getDamping() {
			return damping;
		}

		public void setDamping(float damping) {
			this.damping = damping;
		}
	}

	class HierarchicalRepulsion {
		private float centralGravity = 0f;
		private int springLength = 1;
		private float springConstant = 0f;
		private int nodeDistance = 180;
		private float damping = 0.3f;

		public float getCentralGravity() {
			return centralGravity;
		}

		public void setCentralGravity(float centralGravity) {
			this.centralGravity = centralGravity;
		}

		public int getSpringLength() {
			return springLength;
		}

		public void setSpringLength(int springLength) {
			this.springLength = springLength;
		}

		public float getSpringConstant() {
			return springConstant;
		}

		public void setSpringConstant(float springConstant) {
			this.springConstant = springConstant;
		}

		public int getNodeDistance() {
			return nodeDistance;
		}

		public void setNodeDistance(int nodeDistance) {
			this.nodeDistance = nodeDistance;
		}

		public float getDamping() {
			return damping;
		}

		public void setDamping(float damping) {
			this.damping = damping;
		}
	}
}
