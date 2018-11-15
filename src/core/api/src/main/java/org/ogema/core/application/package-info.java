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
/**
 * Generic information and tools relevant to an 
 * application that do not fit in a more specialized category. 
 * Its most important interfaces are the interface {@link Application}
 * that must be implemented by any OGEMA application class and the 
 * {@link ApplicationManager} that is passed to each application at startup. The
 * application can then reach all framework services via the ApplicationManager.
 */
package org.ogema.core.application;

