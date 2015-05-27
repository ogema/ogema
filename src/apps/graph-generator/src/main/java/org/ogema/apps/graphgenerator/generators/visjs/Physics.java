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
