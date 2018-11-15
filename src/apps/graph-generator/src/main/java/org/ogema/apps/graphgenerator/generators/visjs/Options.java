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

public class Options {

	private HierarchicalLayout hierarchicalLayout = new HierarchicalLayout();
	// set this to true to get configuration table that helps to decide which parameters are best
	private boolean configurePhysics = false;
	private Physics physics = new Physics();
	private boolean smoothCurves = false;
	private String width = "100%";
	private String height = "800px";

	public HierarchicalLayout getHierarchicalLayout() {
		return hierarchicalLayout;
	}

	public void setHierarchicalLayout(HierarchicalLayout hierarchicalLayout) {
		this.hierarchicalLayout = hierarchicalLayout;
	}

	public boolean isSmoothCurves() {
		return smoothCurves;
	}

	public void setSmoothCurves(boolean smoothCurves) {
		this.smoothCurves = smoothCurves;
	}

	public Physics getPhysics() {
		return physics;
	}

	public void setPhysics(Physics physics) {
		this.physics = physics;
	}

	public boolean isConfigurePhysics() {
		return configurePhysics;
	}

	public void setConfigurePhysics(boolean configurePhysics) {
		this.configurePhysics = configurePhysics;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	class HierarchicalLayout {
		private boolean enabled = true;
		private int levelSeparation = 400;
		private int nodeSpacing = 500;
		private String direction = Direction.UP_DOWN.id;
		private String layout = "direction";

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public int getLevelSeparation() {
			return levelSeparation;
		}

		public void setLevelSeparation(int levelSeparation) {
			this.levelSeparation = levelSeparation;
		}

		public int getNodeSpacing() {
			return nodeSpacing;
		}

		public void setNodeSpacing(int nodeSpacing) {
			this.nodeSpacing = nodeSpacing;
		}

		public String getDirection() {
			return direction;
		}

		public void setDirection(String direction) {
			this.direction = direction;
		}

		public String getLayout() {
			return layout;
		}

		public void setLayout(String layout) {
			this.layout = layout;
		}
	}

	enum Direction {
		UP_DOWN("UD"), DOWN_UP("DU"), LEFT_RIGHT("LR"), RIGHT_LEFT("RL");

		private String id;

		private Direction(String id) {
			this.id = id;
		}
	}
}
